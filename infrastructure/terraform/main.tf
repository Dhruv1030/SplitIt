# ============================================================
# SplitIt Azure Infrastructure - Terraform
# Matches existing manually-provisioned resources
# ============================================================

# --- Resource Group ---
resource "azurerm_resource_group" "main" {
  name     = var.resource_group_name
  location = var.location
}

# --- Azure Container Registry ---
resource "azurerm_container_registry" "acr" {
  name                = var.acr_name
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "Basic"
  admin_enabled       = true
}

# --- Azure PostgreSQL Flexible Server ---
resource "azurerm_postgresql_flexible_server" "postgres" {
  name                          = "splitit-postgres"
  resource_group_name           = azurerm_resource_group.main.name
  location                      = var.db_location
  version                       = "15"
  administrator_login           = var.postgres_admin_username
  administrator_password        = var.postgres_admin_password
  storage_mb                    = 32768
  sku_name                      = "B_Standard_B1ms"
  backup_retention_days         = 7   # Free: up to 7 days PITR included
  geo_redundant_backup_enabled  = false
  public_network_access_enabled = true
  # Point-in-time recovery is enabled by default on Flexible Server
  # Backups are automated and included at no extra cost for up to 7 days
}

# Allow Azure services to access PostgreSQL
resource "azurerm_postgresql_flexible_server_firewall_rule" "allow_azure" {
  name             = "AllowAzureServices"
  server_id        = azurerm_postgresql_flexible_server.postgres.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}

# Create databases for each service
resource "azurerm_postgresql_flexible_server_database" "splitwise" {
  name      = "splitwise"
  server_id = azurerm_postgresql_flexible_server.postgres.id
  charset   = "UTF8"
  collation = "en_US.utf8"
}

resource "azurerm_postgresql_flexible_server_database" "notification_db" {
  name      = "notification_db"
  server_id = azurerm_postgresql_flexible_server.postgres.id
  charset   = "UTF8"
  collation = "en_US.utf8"
}

# --- Azure Cosmos DB (MongoDB API) ---
resource "azurerm_cosmosdb_account" "cosmos" {
  name                = var.cosmos_db_account_name
  resource_group_name = azurerm_resource_group.main.name
  location            = var.db_location
  offer_type          = "Standard"
  kind                = "MongoDB"

  capabilities {
    name = "EnableMongo"
  }

  capabilities {
    name = "EnableServerless"
  }

  # Continuous backup with PITR (free on serverless tier, 7-day retention)
  backup {
    type                = "Continuous"
    tier                = "Continuous7Days"
  }

  consistency_policy {
    consistency_level = "Session"
  }

  geo_location {
    location          = var.db_location
    failover_priority = 0
  }
}

resource "azurerm_cosmosdb_mongo_database" "userdb" {
  name                = "userdb"
  resource_group_name = azurerm_resource_group.main.name
  account_name        = azurerm_cosmosdb_account.cosmos.name
}

resource "azurerm_cosmosdb_mongo_database" "analyticsdb" {
  name                = "analyticsdb"
  resource_group_name = azurerm_resource_group.main.name
  account_name        = azurerm_cosmosdb_account.cosmos.name
}

# --- Log Analytics Workspace (required for Container Apps) ---
resource "azurerm_log_analytics_workspace" "logs" {
  name                = "splitit-logs"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "PerGB2018"
  retention_in_days   = 30
  daily_quota_gb      = 0.5 # Cap at 500MB/day to control costs
}

# --- Container Apps Environment ---
resource "azurerm_container_app_environment" "env" {
  name                       = var.container_app_environment_name
  resource_group_name        = azurerm_resource_group.main.name
  location                   = azurerm_resource_group.main.location
  log_analytics_workspace_id = azurerm_log_analytics_workspace.logs.id
}

# --- Discovery Server (always-on, 1 replica) ---
resource "azurerm_container_app" "discovery_server" {
  name                         = "discovery-server"
  container_app_environment_id = azurerm_container_app_environment.env.id
  resource_group_name          = azurerm_resource_group.main.name
  revision_mode                = "Single"

  template {
    min_replicas = 1
    max_replicas = 1

    container {
      name   = "discovery-server"
      image  = "${azurerm_container_registry.acr.login_server}/discovery-server:latest"
      cpu    = 0.25
      memory = "0.5Gi"

      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "docker"
      }

      liveness_probe {
        path             = "/actuator/health"
        port             = 8761
        transport        = "HTTP"
        initial_delay    = 30
        interval_seconds = 30
      }
    }
  }

  ingress {
    external_enabled = true
    target_port      = 8761
    transport        = "http"

    traffic_weight {
      latest_revision = true
      percentage      = 100
    }
  }

  registry {
    server               = azurerm_container_registry.acr.login_server
    username             = azurerm_container_registry.acr.admin_username
    password_secret_name = "acr-password"
  }

  secret {
    name  = "acr-password"
    value = azurerm_container_registry.acr.admin_password
  }
}

# --- API Gateway (always-on, 1 replica) ---
resource "azurerm_container_app" "api_gateway" {
  name                         = "api-gateway"
  container_app_environment_id = azurerm_container_app_environment.env.id
  resource_group_name          = azurerm_resource_group.main.name
  revision_mode                = "Single"

  depends_on = [azurerm_container_app.discovery_server]

  template {
    min_replicas = 1
    max_replicas = 1

    container {
      name   = "api-gateway"
      image  = "${azurerm_container_registry.acr.login_server}/api-gateway:latest"
      cpu    = 0.25
      memory = "0.5Gi"

      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "docker"
      }

      env {
        name        = "JWT_SECRET"
        secret_name = "jwt-secret"
      }

      dynamic "env" {
        for_each = var.services
        content {
          name  = "SERVICES_${upper(replace(split("-", env.value)[0], "-", "_"))}_URL"
          value = "https://${env.value}.${azurerm_container_app_environment.env.default_domain}"
        }
      }

      liveness_probe {
        path             = "/actuator/health"
        port             = 8080
        transport        = "HTTP"
        initial_delay    = 30
        interval_seconds = 30
      }
    }
  }

  ingress {
    external_enabled = true
    target_port      = 8080
    transport        = "http"

    traffic_weight {
      latest_revision = true
      percentage      = 100
    }
  }

  registry {
    server               = azurerm_container_registry.acr.login_server
    username             = azurerm_container_registry.acr.admin_username
    password_secret_name = "acr-password"
  }

  secret {
    name  = "acr-password"
    value = azurerm_container_registry.acr.admin_password
  }

  secret {
    name  = "jwt-secret"
    value = var.jwt_secret
  }
}

# --- Backend Services (scale-to-zero) ---
resource "azurerm_container_app" "backend_services" {
  for_each = toset(var.services)

  name                         = each.value
  container_app_environment_id = azurerm_container_app_environment.env.id
  resource_group_name          = azurerm_resource_group.main.name
  revision_mode                = "Single"

  depends_on = [azurerm_container_app.discovery_server]

  template {
    min_replicas = 0
    max_replicas = 2

    container {
      name   = each.value
      image  = "${azurerm_container_registry.acr.login_server}/${each.value}:latest"
      cpu    = 0.25
      memory = "0.5Gi"

      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "docker"
      }

      env {
        name  = "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE"
        value = "https://discovery-server.${azurerm_container_app_environment.env.default_domain}/eureka/"
      }

      liveness_probe {
        path             = "/actuator/health"
        port             = var.service_ports[each.value]
        transport        = "HTTP"
        initial_delay    = 30
        interval_seconds = 30
      }
    }
  }

  ingress {
    external_enabled = true
    target_port      = var.service_ports[each.value]
    transport        = "http"

    traffic_weight {
      latest_revision = true
      percentage      = 100
    }
  }

  registry {
    server               = azurerm_container_registry.acr.login_server
    username             = azurerm_container_registry.acr.admin_username
    password_secret_name = "acr-password"
  }

  secret {
    name  = "acr-password"
    value = azurerm_container_registry.acr.admin_password
  }
}
