output "resource_group_name" {
  value = azurerm_resource_group.main.name
}

output "acr_login_server" {
  value = azurerm_container_registry.acr.login_server
}

output "container_apps_environment_domain" {
  value = azurerm_container_app_environment.env.default_domain
}

output "api_gateway_url" {
  value = "https://api-gateway.${azurerm_container_app_environment.env.default_domain}"
}

output "discovery_server_url" {
  value = "https://discovery-server.${azurerm_container_app_environment.env.default_domain}"
}

output "postgres_fqdn" {
  value = azurerm_postgresql_flexible_server.postgres.fqdn
}

output "cosmos_connection_string" {
  value     = azurerm_cosmosdb_account.cosmos.primary_mongodb_connection_string
  sensitive = true
}
