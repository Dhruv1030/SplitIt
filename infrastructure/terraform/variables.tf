variable "resource_group_name" {
  description = "Name of the Azure resource group"
  type        = string
  default     = "splitit-rg"
}

variable "location" {
  description = "Azure region for Container Apps and ACR"
  type        = string
  default     = "East US"
}

variable "db_location" {
  description = "Azure region for databases (PostgreSQL, Cosmos DB)"
  type        = string
  default     = "Canada Central"
}

variable "acr_name" {
  description = "Azure Container Registry name"
  type        = string
  default     = "splititacr"
}

variable "postgres_admin_username" {
  description = "PostgreSQL administrator username"
  type        = string
  sensitive   = true
}

variable "postgres_admin_password" {
  description = "PostgreSQL administrator password"
  type        = string
  sensitive   = true
}

variable "cosmos_db_account_name" {
  description = "Cosmos DB account name"
  type        = string
  default     = "splitit-cosmos"
}

variable "jwt_secret" {
  description = "JWT secret for authentication"
  type        = string
  sensitive   = true
}

variable "mail_username" {
  description = "SMTP mail username"
  type        = string
  default     = ""
}

variable "mail_password" {
  description = "SMTP mail password"
  type        = string
  sensitive   = true
  default     = ""
}

variable "container_app_environment_name" {
  description = "Container Apps environment name"
  type        = string
  default     = "splitit-env"
}

variable "services" {
  description = "List of backend microservices"
  type        = list(string)
  default = [
    "user-service",
    "group-service",
    "expense-service",
    "settlement-service",
    "notification-service",
    "payment-service",
    "analytics-service"
  ]
}

variable "service_ports" {
  description = "Port mapping for each service"
  type        = map(number)
  default = {
    "discovery-server"     = 8761
    "api-gateway"          = 8080
    "user-service"         = 8081
    "group-service"        = 8082
    "expense-service"      = 8083
    "settlement-service"   = 8084
    "notification-service" = 8085
    "payment-service"      = 8086
    "analytics-service"    = 8087
  }
}
