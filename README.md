# 🚀 SplitIt - Expense Sharing Microservices Platform

A production-ready microservices-based expense sharing application built with Spring Boot, inspired by Splitwise. Split bills, track expenses, and settle debts with friends and groups seamlessly.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.0-blue)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
![License](https://img.shields.io/badge/license-MIT-green)

## 📋 Table of Contents

- [Architecture](#-architecture)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Services](#-services)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Monitoring](#-monitoring)
- [Contributing](#-contributing)
- [Roadmap](#-development-roadmap)

## 🏗 Architecture

## ✨ Features

### ✅ Implemented Features

- **User Management**

  - User registration and authentication with JWT
  - Profile management (name, email, phone, currency preferences)
  - Friend search by name or email (`/api/users/search`)
  - Add/remove friends functionality

- **Group Management**

  - Create and manage expense groups
  - Add/remove group members with automatic email invitations
  - View group details with member names and emails
  - User groups listing
  - Activity logging for all group operations

- **Expense Tracking**

  - Add expenses with multiple split types (Equal, Exact, Percentage, Shares)
  - Track who paid and who owes
  - Expense history per group
  - User balance calculation with null-safe arithmetic
  - Automatic activity feed updates for expense operations

- **Settlement & Balance**

  - Smart debt simplification algorithm
  - Real-time balance calculation
  - Settlement suggestions (optimized payment plans)
  - Record settlements with automatic email confirmations
  - Group balance overview
  - Payment reminder system (manual + automated weekly digest)

- **Activity Feed** ✨ NEW

  - Complete timeline of all group and user activities
  - Real-time activity logging across all services
  - Paginated activity history with filters
  - Activity enrichment with user and group names
  - 7 REST endpoints for activity management
  - Integration with Expense, Group, and Settlement services

- **Email Notifications** ✨ NEW

  - Production-ready Gmail SMTP integration
  - **Payment Reminders** - Remind debtors to settle outstanding balances
  - **Payment Confirmations** - Notify creditors when payments are received
  - **Group Invitations** - Welcome emails when added to groups
  - Professional HTML email templates with gradient styling
  - Automatic emails on settlement recording
  - Automatic emails on member addition
  - Weekly Digest Scheduler (Monday 9 AM payment reminders)

- **Microservices Architecture**

  - Service discovery with Eureka
  - API Gateway with load balancing
  - Inter-service communication via RestTemplate/Feign
  - Circuit breaker patterns (Resilience4j)
  - Distributed tracing with Zipkin
  - Activity logging integration across services
  - Email notification integration across services

- **API Documentation** ✨ NEW

  - Comprehensive Swagger/OpenAPI docs for all 6 microservices
  - Interactive API testing with Swagger UI
  - JWT Bearer authentication in Swagger
  - Request/response schemas with examples
  - Available at `/swagger-ui/index.html` on each service

- **DevOps & Deployment**
  - Docker containerization for all services
  - Docker Compose orchestration
  - Health checks and monitoring
  - Automated build scripts
  - PostgreSQL for transactional data
  - MongoDB for user profiles

### 🔄 In Progress

- Unit & Integration Tests (targeting 80%+ coverage)
- Security Enhancements (refresh tokens, RBAC, rate limiting)
- Performance Optimization (Redis caching, query optimization)
- Frontend Development (Angular/React UI)

### 🚀 Planned Features

See our [Development Roadmap](#-development-roadmap) for upcoming features including:

- Payment gateway integration (Stripe/PayPal)
- Real-time notifications via Kafka/WebSocket
- Analytics dashboard and insights
- File attachments for receipts
- Recurring expenses
- Multi-currency support with conversion
- Mobile app support
  │
  ┌─────▼──────┐
  │ PostgreSQL │
  │ (5432) │
  └────────────┘

````

## 🎯 Services

| Service | Port | Database | Status | Description |
|---------|------|----------|--------|-------------|
| **Discovery Server** | 8761 | - | ✅ Running | Eureka service registry |
| **API Gateway** | 8080 | - | ✅ Running | Entry point, routing, circuit breakers |
| **User Service** | 8081 | MongoDB | ✅ Running | User auth, profile, friend management |
| **Group Service** | 8082 | PostgreSQL | ✅ Running | Group & member management, activity logging, email invites |
| **Expense Service** | 8083 | PostgreSQL | ✅ Running | Expense tracking & splitting, activity logging |
| **Settlement Service** | 8084 | PostgreSQL | ✅ Running | Balance calculation, debt optimization, email notifications |
| **Notification Service** | 8085 | PostgreSQL | ✅ Running | Activity feed (7 endpoints), Email notifications (4 types), Weekly digest |
| **Payment Service** | 8086 | PostgreSQL | 🔄 In Progress | Payment processing |
| **Analytics Service** | 8087 | MongoDB | 🔄 In Progress | Usage analytics & insights |

### Infrastructure Services

| Service | Port | Status | Description |
|---------|------|--------|-------------|
| **MongoDB** | 27017 | ✅ Running | Document database |
| **PostgreSQL** | 5432 | ✅ Running | Relational database |
| **Kafka** | 9093 | ✅ Running | Event streaming |
| **Zookeeper** | 2181 | ✅ Running | Kafka coordination |
| **Zipkin** | 9411 | ✅ Running | Distributed tracing |

## 🛠 Tech Stack

### Backend
- **Java 17**: Modern Java features with records and pattern matching
- **Spring Boot 3.2.0**: Application framework with auto-configuration
- **Spring Cloud 2023.0.0**: Microservices patterns
  - **Eureka**: Service discovery and registration
  - **Gateway**: API routing, load balancing, and circuit breakers
  - **OpenFeign**: Declarative REST clients
  - **Resilience4j**: Circuit breaker and fault tolerance
- **Thymeleaf**: HTML email template engine
- **JavaMailSender**: SMTP email integration with Gmail
- **SpringDoc OpenAPI**: Interactive API documentation (Swagger)
- **Spring Scheduling**: Cron-based task scheduling (@Scheduled)

### Databases
- **MongoDB**: User profiles, analytics data, document storage
- **PostgreSQL**: Transactional data (groups, expenses, payments, settlements, activities)

### Messaging & Events
- **Apache Kafka**: Event streaming and asynchronous communication
- **Zookeeper**: Kafka cluster coordination

### Observability
- **Zipkin**: Distributed tracing across microservices
- **Spring Boot Actuator**: Health checks, metrics, and monitoring endpoints
- **Docker Logs**: Centralized logging

### DevOps & Tools
- **Docker**: Container runtime
- **Docker Compose**: Multi-container orchestration
- **Maven 3.9+**: Build automation and dependency management
- **Git**: Version control

### Security (Planned)
- **JWT**: Token-based authentication
- **Spring Security**: Authorization and security filters

## 📦 Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **Docker** and **Docker Compose**
- **Git**

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/Dhruv1030/SplitIt.git
cd SplitIt
````

### 2. Build All Services

```bash
./build.sh
```

### 3. Start All Services

```bash
docker-compose up -d
```

### 4. Verify Services

Wait 30-60 seconds for all services to register with Eureka, then check:

**Eureka Dashboard** (view registered services):

```bash
open http://localhost:8761
```

**Service Health Checks**:

```bash
# Check all services via Gateway
curl http://localhost:8080/actuator/health

# Check individual service
curl http://localhost:8081/actuator/health
```

### 5. Test the API

**Register a new user**:

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "phone": "+1234567890",
    "defaultCurrency": "USD"
  }'
```

**Search for users**:

```bash
curl -X GET "http://localhost:8080/api/users/search?query=john" \
  -H "Authorization: Bearer <token>"
```

**Create a group**:

```bash
curl -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "name": "Weekend Trip",
    "description": "Beach vacation expenses",
    "currency": "USD",
    "createdBy": "user-id-here"
  }'
```

**Add an expense**:

```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "groupId": 1,
    "description": "Hotel booking",
    "amount": 300.00,
    "currency": "USD",
    "paidBy": "user-id-here",
    "splitType": "EQUAL",
    "participants": ["user-id-1", "user-id-2"]
  }'
```

**Get settlement suggestions**:

```bash
curl -X GET http://localhost:8080/api/settlements/group/1/suggestions \
  -H "Authorization: Bearer <token>"
```

**View activity feed**:

```bash
curl http://localhost:8080/api/activities/group/1/recent
```

**Test email notifications**:

```bash
curl -X POST http://localhost:8080/api/notifications/test \
  -H "Content-Type: application/json" \
  -d '{
    "to": "test@example.com",
    "subject": "Test Email",
    "body": "Testing email notifications"
  }'
```

**Access Swagger UI** for interactive API testing:

```
http://localhost:8081/swagger-ui/index.html  # User Service
http://localhost:8082/swagger-ui/index.html  # Group Service
http://localhost:8083/swagger-ui/index.html  # Expense Service
http://localhost:8084/swagger-ui/index.html  # Settlement Service
http://localhost:8085/swagger-ui/index.html  # Notification Service
```

## 📚 API Documentation

### Complete API Reference

For detailed API documentation including all endpoints, request/response formats, and examples, see:

- **[FRONTEND_API_REFERENCE.md](docs/FRONTEND_API_REFERENCE.md)** - Comprehensive API guide for frontend integration
- **[ACTIVITY_FEED_IMPLEMENTATION.md](docs/ACTIVITY_FEED_IMPLEMENTATION.md)** - Activity Feed API documentation
- **[EMAIL_INTEGRATION_COMPLETED.md](docs/EMAIL_INTEGRATION_COMPLETED.md)** - Email Notifications API guide
- **Swagger UI**: Interactive API docs at `http://localhost:{port}/swagger-ui/index.html` for each service

### Quick Reference

#### User Service (`/api/users`)

- `POST /register` - Register new user
- `POST /login` - User authentication
- `GET /{id}` - Get user profile
- `PUT /{id}` - Update profile
- `GET /search?query={term}` - Search users by name or email
- `POST /{id}/friends?friendId={friendId}` - Add friend
- `DELETE /{id}/friends/{friendId}` - Remove friend
- `GET /{id}/friends` - Get user's friends

#### Group Service (`/api/groups`)

- `POST /` - Create new group
- `GET /{id}` - Get group details (includes member names)
- `PUT /{id}` - Update group
- `DELETE /{id}` - Delete group
- `POST /{id}/members` - Add member to group (sends invitation email)
- `DELETE /{id}/members/{memberId}` - Remove member
- `GET /user/{userId}` - Get user's groups

#### Expense Service (`/api/expenses`)

- `POST /` - Create expense (logs activity automatically)
- `GET /{id}` - Get expense details
- `PUT /{id}` - Update expense
- `DELETE /{id}` - Delete expense
- `GET /group/{groupId}` - Get group expenses
- `GET /user/{userId}` - Get user's expenses
- `GET /user/{userId}/balance` - Get user balance

#### Settlement Service (`/api/settlements`)

- `GET /group/{groupId}/suggestions` - Get optimized payment plan
- `GET /group/{groupId}` - Get all settlements
- `POST /record` - Record a settlement payment (sends confirmation email)
- `GET /user/{userId}` - Get user's settlements
- `GET /outstanding` - Get all outstanding settlements (for weekly digest)
- `POST /reminders/send` - Manually send payment reminders

#### Activity Feed (`/api/activities`) ✨ NEW

- `POST /` - Log new activity
- `GET /group/{groupId}` - Get group activities (paginated)
- `GET /group/{groupId}/recent` - Get last 10 activities
- `GET /user/{userId}` - Get user activities (paginated)
- `GET /group/{groupId}/range` - Get activities by date range
- `GET /group/{groupId}/count` - Get activity count
- `GET /health` - Health check

#### Email Notifications (`/api/notifications`) ✨ NEW

- `POST /payment-reminder` - Send payment reminder email
- `POST /payment-received` - Send payment confirmation email
- `POST /group-invitation` - Send group invitation email
- `POST /test` - Test email configuration

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Tests for Specific Service

```bash
cd [service-name]
mvn test
```

### Integration Testing

```bash
# Start services
docker-compose up -d

# Wait for services to be ready (30-60 seconds)
sleep 60

# Run integration tests
./run-tests.sh
```

### Manual API Testing

Use the API examples in the [Quick Start](#-quick-start) section or import our Postman collection:

- See `docs/FRONTEND_API_REFERENCE.md` for detailed request/response examples

## 📊 Monitoring

### Eureka Dashboard

View all registered services and their health status:

```
http://localhost:8761
```

### Zipkin Tracing

View distributed traces across services:

```
http://localhost:9411
```

### Service Health Checks

```bash
# Check all services via Gateway
curl http://localhost:8080/actuator/health

# Check specific service (e.g., User Service)
curl http://localhost:8081/actuator/health

# Check service info
curl http://localhost:8081/actuator/info
```

### View Logs

```bash
# View logs for all services
docker-compose logs -f

# View logs for specific service
docker-compose logs -f user-service

# View recent logs
docker-compose logs --tail=100 expense-service
```

## 🔧 Development

### Project Structure

```
SplitIt/
├── discovery-server/        # Eureka service registry
├── api-gateway/             # Spring Cloud Gateway
├── user-service/            # User management & authentication
├── group-service/           # Group management
├── expense-service/         # Expense tracking
├── settlement-service/      # Debt settlement & balance calculation
├── notification-service/    # Notifications (email, push)
├── payment-service/         # Payment processing
├── analytics-service/       # Analytics & insights
├── docs/                    # Documentation
│   ├── FRONTEND_API_REFERENCE.md
│   ├── API_DOCUMENTATION.md
│   └── ...
├── docker-compose.yml       # Multi-container setup
├── pom.xml                  # Parent POM
├── build.sh                 # Build script
├── start.sh                 # Startup script
├── CONTRIBUTING.md          # Contribution guidelines
└── CODE_OF_CONDUCT.md       # Code of conduct
```

### Running Locally (Without Docker)

1. Start infrastructure services:

```bash
docker-compose up -d mongodb postgres kafka zookeeper zipkin
```

2. Start Discovery Server:

```bash
cd discovery-server
mvn spring-boot:run
```

3. Start API Gateway:

```bash
cd api-gateway
mvn spring-boot:run
```

4. Start individual microservices:

```bash
cd [service-name]
mvn spring-boot:run
```

### Adding a New Service

1. Create service module structure
2. Add module to parent `pom.xml`
3. Configure `application.yml` with Eureka client
4. Add route in API Gateway configuration
5. Create Dockerfile
6. Add service to `docker-compose.yml`
7. Update documentation

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

## 📈 Development Roadmap

### ✅ Phase 1: Core MVP (Completed)

- [x] User registration and authentication
- [x] Group creation and management
- [x] Expense tracking with equal splits
- [x] Balance calculation
- [x] Settlement suggestions with debt optimization
- [x] Service discovery and API Gateway
- [x] Docker containerization

### ✅ Phase 2: Enhanced Features (Completed) ✨ NEW

- [x] **Activity Feed Service** - Complete timeline with 7 REST endpoints
- [x] **Activity Integration** - Logging across Expense, Group, Settlement services
- [x] **Email Notifications** - Gmail SMTP with 4 email types
- [x] **Payment Confirmations** - Automatic emails on settlement recording
- [x] **Group Invitations** - Automatic emails when members are added
- [x] **Weekly Digest Scheduler** - Automated Monday 9 AM payment reminders
- [x] **Swagger/OpenAPI Documentation** - Interactive API docs for all 6 services
- [x] **User/Group Name Enrichment** - Activity feed shows names instead of IDs

### 🔄 Phase 3: Testing & Quality (In Progress)

- [ ] **Unit & Integration Tests** - 80%+ code coverage across all services
- [ ] **Security Enhancements** - Refresh tokens, RBAC, rate limiting
- [ ] **Performance Optimization** - Redis caching, query optimization, monitoring
- [ ] **Frontend Development** - Angular/React UI with all backend integrations

### 🚀 Phase 4: Advanced Features (Planned)

- [ ] **Multi-Currency Support** - Handle expenses in different currencies with conversion
- [ ] **Unequal Splits** - Enhanced split by percentage, shares, or exact amounts
- [ ] **Receipt Uploads** - Attach receipts/bills to expenses with file storage
- [ ] **Recurring Expenses** - Automatic recurring bill creation with schedules
- [ ] **Analytics Dashboard** - Spending insights, trends, and visualizations
- [ ] **Export Reports** - CSV/PDF export of expenses and settlements
- [ ] **Mobile App** - iOS and Android applications
- [ ] **Real-time Notifications** - WebSocket/Kafka for instant updates
- [ ] **Payment Gateway Integration** - Stripe/PayPal integration for actual payments
- [ ] **Group Roles & Permissions** - Admin/member role management
- [ ] **Activity Comments** - Discussion threads on activities
- [ ] **Budget Tracking** - Set and monitor group/category budgets

See our [todo list](TODO.md) for detailed task tracking.

## 🐳 Docker Commands

### Start all services

```bash
docker-compose up -d
```

### Stop all services

```bash
docker-compose down
```

### Rebuild and restart

```bash
docker-compose up -d --build
```

### View logs

```bash
docker-compose logs -f [service-name]
```

### Clean up volumes

```bash
docker-compose down -v
```

### Check service status

```bash
docker-compose ps
```

## 🐛 Troubleshooting

### Services not registering with Eureka

- Check if discovery-server is running on port 8761
- Verify `eureka.client.service-url.defaultZone` in application.yml
- Wait 30-60 seconds for services to register after startup
- Check service logs: `docker-compose logs -f [service-name]`

### Database connection issues

- Ensure MongoDB and PostgreSQL are running: `docker-compose ps`
- Check connection strings in `application.yml`
- Verify database containers are healthy: `docker inspect [container-name]`
- Check database logs: `docker-compose logs -f mongodb postgres`

### Kafka connection issues

- Verify Kafka and Zookeeper are running
- Check `spring.kafka.bootstrap-servers` configuration
- Ensure Kafka topics are created
- View Kafka logs: `docker-compose logs -f kafka`

### Port conflicts

- Check if ports are already in use: `lsof -i :[port]`
- Stop conflicting services or change ports in `docker-compose.yml`
- Default ports: 8080 (gateway), 8761 (eureka), 27017 (mongo), 5432 (postgres)

### Service startup delays

- Services may take 30-60 seconds to fully start and register
- Check Eureka dashboard at http://localhost:8761
- Use health check endpoints to verify status
- Increase startup wait time if needed

## 🤝 Contributing

We welcome contributions from the community! Whether you're fixing bugs, adding features, or improving documentation, your help is appreciated.

### How to Contribute

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Make your changes** following our coding standards
4. **Write tests** for new features
5. **Commit your changes** (`git commit -m 'Add amazing feature'`)
6. **Push to the branch** (`git push origin feature/amazing-feature`)
7. **Open a Pull Request**

### Guidelines

Please read our [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines on:

- Development setup
- Coding standards
- Commit message conventions
- Pull request process
- Testing requirements

### Code of Conduct

This project adheres to a [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Dhruv Patel**

- GitHub: [@Dhruv1030](https://github.com/Dhruv1030)
- Email: dhruv03.work@gmail.com

## 🙏 Acknowledgments

- Inspired by [Splitwise](https://www.splitwise.com)
- Spring Boot & Spring Cloud communities
- All contributors and supporters

## 📧 Contact

For questions or support:

- **Open an issue**: [GitHub Issues](https://github.com/Dhruv1030/SplitIt/issues)
- **Email**: dhruv03.work@gmail.com
- **Discussions**: [GitHub Discussions](https://github.com/Dhruv1030/SplitIt/discussions)

---

**⭐ Star this repository if you find it helpful!**

**🤝 Contributions are welcome! Check out our [roadmap](#-development-roadmap) for ideas.**

```

### Upcoming Features
```
