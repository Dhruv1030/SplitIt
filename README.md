# SplitIt

A microservices-based expense sharing platform built with Spring Boot — split bills, track expenses, and settle debts.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.0-blue)
![CI](https://github.com/Dhruv1030/SplitIt/actions/workflows/ci.yml/badge.svg)
![CD](https://github.com/Dhruv1030/SplitIt/actions/workflows/cd.yml/badge.svg)
![License](https://img.shields.io/badge/license-MIT-green)

## Architecture

```
                         ┌──────────────┐
                         │  API Gateway │  :8080
                         └──────┬───────┘
                                │
                ┌───────────────┼───────────────┐
                │               │               │
        ┌───────┴───────┐ ┌────┴─────┐ ┌───────┴────────┐
        │ User Service  │ │  Group   │ │    Expense     │
        │    :8081      │ │  :8082   │ │     :8083      │
        │   MongoDB     │ │ Postgres │ │   Postgres     │
        └───────────────┘ └──────────┘ └────────────────┘
                │               │               │
        ┌───────┴───────┐ ┌────┴─────┐ ┌───────┴────────┐
        │  Settlement   │ │ Notif.   │ │    Payment     │
        │    :8084      │ │  :8085   │ │     :8086      │
        │   Postgres    │ │ Postgres │ │   Postgres     │
        └───────────────┘ └──────────┘ └────────────────┘
                                │
                        ┌───────┴────────┐
                        │   Analytics    │
                        │     :8087      │
                        │    MongoDB     │
                        └────────────────┘

        ┌────────────────────────────────────────────┐
        │          Discovery Server (Eureka) :8761   │
        └────────────────────────────────────────────┘
```

## Services

| Service | Port | Database | Description |
|---------|------|----------|-------------|
| Discovery Server | 8761 | — | Eureka service registry |
| API Gateway | 8080 | — | Routing, auth filter, circuit breakers |
| User Service | 8081 | MongoDB | Registration, JWT auth, friends |
| Group Service | 8082 | PostgreSQL | Groups, members, invitations |
| Expense Service | 8083 | PostgreSQL | Expenses, splits (equal/exact/percentage/shares) |
| Settlement Service | 8084 | PostgreSQL | Balances, debt simplification, payments |
| Notification Service | 8085 | PostgreSQL | Activity feed, email notifications |
| Payment Service | 8086 | PostgreSQL | Payment processing |
| Analytics Service | 8087 | MongoDB | Usage analytics |

## Tech Stack

- **Runtime**: Java 17, Spring Boot 3.2.0, Spring Cloud 2023.0.0
- **API Gateway**: Spring Cloud Gateway + Resilience4j circuit breakers
- **Service Discovery**: Netflix Eureka
- **Databases**: MongoDB (users, analytics), PostgreSQL (everything else)
- **Auth**: JWT tokens with refresh token rotation
- **Email**: Gmail SMTP via Thymeleaf HTML templates
- **API Docs**: SpringDoc OpenAPI / Swagger UI
- **CI/CD**: GitHub Actions → Azure Container Registry → Azure Container Apps
- **Containers**: Docker with multi-stage builds

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for local development)

### Local Development

```bash
# Clone
git clone https://github.com/Dhruv1030/SplitIt.git
cd SplitIt

# Build all modules
mvn clean package -DskipTests

# Start everything
docker compose up -d

# Verify — wait ~60s for Eureka registration
curl http://localhost:8761/eureka/apps
curl http://localhost:8080/actuator/health
```

### Environment Variables

Copy `.env.example` to `.env` and configure:

```bash
cp .env.example .env
```

Key variables: `MONGODB_URI`, `POSTGRES_*`, `JWT_SECRET`, `GMAIL_USERNAME`, `GMAIL_APP_PASSWORD`

## API Reference

All endpoints are accessible through the API Gateway at `http://localhost:8080` (local) or the Azure deployment URL.

### Authentication

```bash
# Register
POST /api/users/register
{ "name": "John", "email": "john@example.com", "password": "pass123", "phone": "+1234567890" }

# Login → returns JWT token
POST /api/users/login
{ "email": "john@example.com", "password": "pass123" }
```

All other endpoints require `Authorization: Bearer <token>` header.

### Users — `/api/users`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/{id}` | Get user profile |
| PUT | `/{id}` | Update profile |
| GET | `/search?query={term}` | Search users by name/email |
| POST | `/{id}/friends?friendId={fid}` | Add friend |
| DELETE | `/{id}/friends/{friendId}` | Remove friend |
| GET | `/{id}/friends` | List friends |

### Groups — `/api/groups`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create group |
| GET | `/{id}` | Get group with members |
| PUT | `/{id}` | Update group |
| DELETE | `/{id}` | Delete group |
| POST | `/{id}/members` | Add member (sends invite email) |
| DELETE | `/{id}/members/{memberId}` | Remove member |
| GET | `/user/{userId}` | List user's groups |

### Expenses — `/api/expenses`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create expense |
| GET | `/{id}` | Get expense |
| PUT | `/{id}` | Update expense |
| DELETE | `/{id}` | Soft delete expense |
| GET | `/group/{groupId}` | List group expenses |
| GET | `/user/{userId}` | List user expenses |
| GET | `/user/{userId}/balance` | Get user balance |

Split types: `EQUAL`, `EXACT`, `PERCENTAGE`, `SHARES`

### Settlements — `/api/settlements`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/group/{groupId}/suggestions` | Optimized payment plan |
| GET | `/group/{groupId}` | List group settlements |
| POST | `/record` | Record payment (sends confirmation email) |
| GET | `/user/{userId}` | List user settlements |
| POST | `/reminders/send` | Send payment reminders |

### Activity Feed — `/api/activities`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Log activity |
| GET | `/group/{groupId}` | Group activities (paginated) |
| GET | `/group/{groupId}/recent` | Last 10 activities |
| GET | `/user/{userId}` | User activities (paginated) |

### Swagger UI

Each service exposes interactive API docs:

```
http://localhost:{port}/swagger-ui/index.html
```

## CI/CD

Fully automated via GitHub Actions:

- **CI** ([ci.yml](.github/workflows/ci.yml)): Runs on every push/PR — builds all modules, runs unit tests, validates Docker builds
- **CD** ([cd.yml](.github/workflows/cd.yml)): Runs on push to `main` — builds Docker images, pushes to Azure Container Registry, deploys to Azure Container Apps, runs smoke tests

```
Push to main → Build & Test → Docker Build → Push to ACR → Deploy → Smoke Test
```

### Production

Live at: `https://api-gateway.delightfulfield-e71e7e6d.eastus.azurecontainerapps.io`

## Project Structure

```
SplitIt/
├── .github/workflows/      # CI/CD pipelines
├── api-gateway/             # Spring Cloud Gateway
├── discovery-server/        # Eureka Server
├── user-service/            # User management & auth
├── group-service/           # Group management
├── expense-service/         # Expense tracking & splitting
├── settlement-service/      # Balance & debt settlement
├── notification-service/    # Activity feed & emails
├── payment-service/         # Payment processing
├── analytics-service/       # Usage analytics
├── docker-compose.yml       # Local development stack
├── pom.xml                  # Parent POM (all modules)
└── .env.example             # Environment template
```

## License

[MIT](LICENSE)
