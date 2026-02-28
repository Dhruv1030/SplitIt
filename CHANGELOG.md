# Changelog

All notable progress on the SplitIt project is documented in this file.

## What We Have Done Till Date

### Microservices Architecture (9 Services)

Built a complete microservices-based expense-sharing platform using **Java 17**, **Spring Boot 3.2.0**, and **Spring Cloud 2023.0.0**.

#### 1. Discovery Server (port 8761)
- Netflix Eureka service registry
- All backend services register and discover each other automatically

#### 2. API Gateway (port 8080)
- Spring Cloud Gateway with path-based routing to all backend services
- JWT authentication filter — validates tokens and injects `X-User-Id` header
- Public routes for `/api/users/register` and `/api/users/login`
- Resilience4j circuit breakers for fault tolerance
- CORS configuration for frontend origins
- Eureka-based load balancing (`lb://service-name`)

#### 3. User Service (port 8081 · MongoDB)
- User registration with email uniqueness check and BCrypt password hashing
- Login with JWT access token + refresh token rotation
- Token refresh and logout endpoints
- User profile CRUD (get, update, get current user via `/me`)
- Friend management — add, remove, and list friends
- User search by name or email
- Custom exceptions: `ResourceNotFoundException`, `InvalidCredentialsException`, `UserAlreadyExistsException`
- Spring Security configuration
- **Unit tests**: `UserServiceTest`, `RefreshTokenServiceTest`

#### 4. Group Service (port 8082 · PostgreSQL)
- Create, read, update, and delete groups
- Group member management — add and remove members with role tracking
- List groups by user and groups created by user
- Cross-service calls to User Service (fetch user details via RestTemplate)
- Cross-service calls to Notification Service (send group invitation emails)
- Activity logging via Activity Client
- Custom exceptions: `ResourceNotFoundException`, `UnauthorizedException`, `BadRequestException`
- **Unit tests**: `GroupServiceTest`

#### 5. Expense Service (port 8083 · PostgreSQL)
- Full expense CRUD — create, read, update, and soft-delete expenses
- Four split types implemented via `SplitCalculatorService`:
  - **EQUAL** — split evenly among participants
  - **EXACT** — specify exact amounts per person
  - **PERCENTAGE** — specify percentage per person (validates sum = 100%)
  - **SHARES** — specify share count per person
- Per-user balance calculation across all groups
- Endpoints: group expenses, user expenses (`/my-expenses` and `/user`), user balance
- Activity logging for expense events
- **Unit tests**: `ExpenseServiceTest`

#### 6. Settlement Service (port 8084 · PostgreSQL)
- Greedy debt-simplification algorithm to minimize number of transactions
- Settlement suggestions per group (fetches balances from Expense Service via WebClient)
- Record payments between users
- List settlements per group and per user (`/my-settlements`)
- Send payment reminder and payment-received emails via Notification Service
- Activity logging for payment events

#### 7. Notification Service (port 8085 · PostgreSQL)
- Email sending via Gmail SMTP with Thymeleaf HTML templates:
  - Payment reminder emails
  - Payment received confirmation emails
  - Group invitation emails
  - Test email template
- Activity feed — log and query activities per group and per user
- Recent activity endpoint (last 10 per group)
- Paginated activity queries
- Weekly digest scheduler (`WeeklyDigestScheduler`)

#### 8. Payment Service (port 8086 · PostgreSQL)
- Application scaffold with Spring Boot, JPA, and Swagger/OpenAPI
- Extensible for future payment gateway integrations (Stripe, PayPal, etc.)

#### 9. Analytics Service (port 8087 · MongoDB)
- Application scaffold with Spring Boot and MongoDB
- Extensible for future usage analytics and reporting dashboards

### Infrastructure & DevOps

- **Docker**: Multi-stage Dockerfiles for every service (build + runtime layers)
- **Docker Compose**: Full local development stack including:
  - Zookeeper + Kafka 7.5.0 (event streaming)
  - MongoDB (users, analytics)
  - PostgreSQL 15 (groups, expenses, settlements, notifications, payments)
  - Zipkin (distributed tracing)
  - Custom bridge network (`splitwise-network`)
- **CI pipeline** (`ci.yml`): Triggers on push/PR to `main`, `develop`, and `feature/*` branches — Maven build, unit tests, Docker build validation, Surefire test reports
- **CD pipeline** (`cd.yml`): Triggers on push to `main` — builds Docker images in parallel, pushes to Azure Container Registry (`splititacr.azurecr.io`), deploys sequentially to Azure Container Apps, runs post-deploy smoke tests
- **Production deployment**: Live on Azure Container Apps

### Cross-Cutting Concerns

- **Authentication**: JWT tokens with HMAC-512 signing, refresh token rotation
- **API Documentation**: SpringDoc OpenAPI / Swagger UI on every service (`/swagger-ui/index.html`)
- **Error Handling**: Global exception handlers with consistent `ApiResponse<T>` wrappers
- **CORS**: Configured on gateway and individual services for frontend compatibility
- **Distributed Tracing**: Zipkin integration for request tracking across services
- **Environment Config**: `.env.example` template covering databases, JWT, SMTP, Kafka, AWS, feature flags, and more
- **License**: MIT
