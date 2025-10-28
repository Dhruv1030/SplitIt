# 🚀 SplitIt - Expense Sharing Microservices Platform# 🎯 Splitwise Clone - Microservices Architecture



A production-ready microservices-based expense sharing application built with Spring Boot, inspired by Splitwise. Split bills, track expenses, and settle debts with friends and groups seamlessly.A production-ready expense sharing application built with Spring Boot microservices architecture.



![Java](https://img.shields.io/badge/Java-17-orange)## 📊 Architecture Overview

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)

![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.0-blue)```

![Docker](https://img.shields.io/badge/Docker-Enabled-blue)Client → API Gateway → Discovery Server → Microservices

![License](https://img.shields.io/badge/license-MIT-green)                                          ↓

                                    Kafka Events

## 📋 Table of Contents                                          ↓

                                    Notifications

- [Architecture](#architecture)```

- [Features](#features)

- [Tech Stack](#tech-stack)## 🏗️ Microservices

- [Prerequisites](#prerequisites)

- [Quick Start](#quick-start)1. **Discovery Server** (Port: 8761) - Eureka service registry

- [Services](#services)2. **API Gateway** (Port: 8080) - Request routing and load balancing

- [API Documentation](#api-documentation)3. **User Service** (Port: 8081) - User management, authentication

- [Testing](#testing)4. **Group Service** (Port: 8082) - Group creation and management

- [Monitoring](#monitoring)5. **Expense Service** (Port: 8083) - Expense tracking and splitting

- [Contributing](#contributing)6. **Settlement Service** (Port: 8084) - Balance calculation and debt optimization

7. **Notification Service** (Port: 8085) - Email and push notifications

## 🏗 Architecture8. **Payment Service** (Port: 8086) - Payment gateway integration

9. **Analytics Service** (Port: 8087) - Spending analytics and reports

SplitIt follows a microservices architecture with the following components:

## 🚀 Quick Start

```

┌─────────────────────────────────────────────────────────────┐### Prerequisites

│                     API Gateway (8080)                      │

│              Load Balancing & Circuit Breakers              │- Java 17+

└─────────────────────┬───────────────────────────────────────┘- Maven 3.8+

                      │- Docker & Docker Compose

┌─────────────────────┴───────────────────────────────────────┐- MongoDB

│              Discovery Server (Eureka - 8761)               │- PostgreSQL

└─────────────────────┬───────────────────────────────────────┘

                      │### Running with Docker Compose

        ┌─────────────┼─────────────┬──────────────┐

        │             │             │              │```bash

   ┌────▼───┐   ┌────▼───┐   ┌────▼───┐    ┌────▼────┐# Start all services

   │  User  │   │ Group  │   │Expense │    │Settlement│docker-compose up -d

   │Service │   │Service │   │Service │    │ Service  │

   │ (8081) │   │ (8082) │   │ (8083) │    │  (8084)  │# Check service status

   └────┬───┘   └────┬───┘   └────┬───┘    └────┬────┘docker-compose ps

        │            │            │              │

   ┌────▼───┐   ┌───▼────┐   ┌───▼─────┐  ┌────▼────┐# View logs

   │Payment │   │Notif.  │   │Analytics│  │ Zipkin  │docker-compose logs -f [service-name]

   │Service │   │Service │   │ Service │  │ (9411)  │

   │ (8086) │   │ (8085) │   │ (8087)  │  └─────────┘# Stop all services

   └────────┘   └───┬────┘   └────┬────┘docker-compose down

                    │             │```

              ┌─────▼─────┐  ┌───▼────┐

              │   Kafka   │  │MongoDB │### Running Locally

              │  (9092)   │  │(27017) │

              └───────────┘  └────────┘1. Start infrastructure services:

                    │

              ┌─────▼──────┐```bash

              │ PostgreSQL │docker-compose up -d mongodb postgres kafka zookeeper zipkin

              │   (5432)   │```

              └────────────┘

```2. Start Discovery Server:



## ✨ Features```bash

cd discovery-server

### Current Featuresmvn spring-boot:run

- ✅ **User Management**: Registration, authentication, profile management```

- ✅ **Service Discovery**: Automatic service registration with Eureka

- ✅ **API Gateway**: Single entry point with circuit breakers3. Start API Gateway:

- ✅ **Distributed Tracing**: Request tracing with Zipkin

- ✅ **Event-Driven**: Kafka for asynchronous communication```bash

- ✅ **Containerized**: Docker & Docker Compose supportcd api-gateway

- ✅ **Database Support**: MongoDB for users/analytics, PostgreSQL for transactional datamvn spring-boot:run

```

### Upcoming Features

- 🔄 Group management and member roles4. Start individual microservices:

- 🔄 Expense tracking with split calculations

- 🔄 Smart debt simplification algorithm```bash

- 🔄 Payment integrationscd [service-name]

- 🔄 Real-time notificationsmvn spring-boot:run

- 🔄 Analytics and insights```

- 🔄 JWT authentication & authorization

- 🔄 API documentation with Swagger## 📝 API Documentation



## 🛠 Tech Stack### User Service



### Backend- `POST /api/users/register` - Register new user

- **Java 17**: Modern Java features- `POST /api/users/login` - User login

- **Spring Boot 3.2.0**: Application framework- `GET /api/users/{id}` - Get user profile

- **Spring Cloud 2023.0.0**: Microservices patterns- `GET /api/users/{id}/friends` - Get user's friends

  - Eureka: Service discovery

  - Gateway: API routing & load balancing### Group Service

  - Circuit Breaker: Resilience4j

- `POST /api/groups` - Create new group

### Databases- `GET /api/groups/{id}` - Get group details

- **MongoDB**: User profiles, analytics data- `POST /api/groups/{id}/members` - Add group member

- **PostgreSQL**: Transactional data (groups, expenses, payments)- `GET /api/groups/user/{userId}` - Get user's groups



### Messaging & Streaming### Expense Service

- **Apache Kafka**: Event streaming

- **Zookeeper**: Kafka coordination- `POST /api/expenses` - Add new expense

- `GET /api/expenses/{id}` - Get expense details

### Observability- `GET /api/expenses/group/{groupId}` - Get group expenses

- **Zipkin**: Distributed tracing- `GET /api/expenses/user/{userId}/balance` - Get user balance

- **Spring Boot Actuator**: Health checks & metrics

### Settlement Service

### DevOps

- **Docker**: Containerization- `GET /api/settlements/group/{groupId}` - Get group settlements

- **Docker Compose**: Multi-container orchestration- `POST /api/settlements/record` - Record a settlement

- **Maven**: Build automation- `GET /api/settlements/optimize/{groupId}` - Get optimized payment plan



## 📦 Prerequisites## 🔧 Technology Stack



- **Java 17** or higher- **Backend**: Java 17, Spring Boot 3.2

- **Maven 3.8+**- **Cloud**: Spring Cloud (Eureka, Gateway, OpenFeign)

- **Docker** and **Docker Compose**- **Databases**: MongoDB, PostgreSQL

- **Git**- **Messaging**: Apache Kafka

- **Tracing**: Zipkin

## 🚀 Quick Start- **Containerization**: Docker

- **Security**: JWT, Spring Security

### 1. Clone the Repository

```bash## 📊 Database Schema

git clone https://github.com/Dhruv1030/SplitIt.git

cd SplitIt### MongoDB (User Service)

```

- users collection

### 2. Build All Services

```bash### PostgreSQL

./build.sh

```- groups, group_members (Group Service)

- expenses, expense_splits (Expense Service)

### 3. Start All Services- balances, settlements (Settlement Service)

```bash- payments (Payment Service)

docker compose up -d

```## 🎯 Key Features



### 4. Verify Services- ✅ User authentication with JWT

Wait 30-60 seconds for all services to start, then check:- ✅ Group creation and management

- ✅ Multiple split types (Equal, Exact, Percentage)

**Eureka Dashboard** (see registered services):- ✅ Multi-currency support

```bash- ✅ Debt simplification algorithm

open http://localhost:8761- ✅ Real-time notifications via Kafka

```- ✅ Payment gateway integration

- ✅ Spending analytics

**Service Health**:- ✅ Distributed tracing with Zipkin

```bash- ✅ Circuit breaker patterns

docker ps

```## 📈 Development Roadmap



### 5. Test the API### Phase 1: MVP ✅



**Register a new user**:- User registration/login

```bash- Basic group management

curl -X POST http://localhost:8080/api/users/register \- Simple expense splitting

  -H "Content-Type: application/json" \- Balance calculation

  -d '{

    "name": "John Doe",### Phase 2: Enhanced Features

    "email": "john@example.com",

    "password": "password123"- Unequal splits

  }'- Multi-currency support

```- Notification system

- Payment integration

**Login**:

```bash### Phase 3: Advanced Features

curl -X POST http://localhost:8080/api/users/login \

  -H "Content-Type: application/json" \- Analytics dashboard

  -d '{- Recurring expenses

    "email": "john@example.com",- Receipt uploads

    "password": "password123"- Export reports

  }'

```## 🧪 Testing



## 🎯 Services```bash

# Run all tests

| Service | Port | Database | Description |mvn test

|---------|------|----------|-------------|

| **Discovery Server** | 8761 | - | Eureka service registry |# Run specific service tests

| **API Gateway** | 8080 | - | Entry point, routing, circuit breakers |cd [service-name]

| **User Service** | 8081 | MongoDB | User auth & profile management |mvn test

| **Group Service** | 8082 | PostgreSQL | Group & member management |```

| **Expense Service** | 8083 | PostgreSQL | Expense tracking & splitting |

| **Settlement Service** | 8084 | PostgreSQL | Debt calculation & settlement |## 📦 Building

| **Notification Service** | 8085 | - | Event-driven notifications |

| **Payment Service** | 8086 | PostgreSQL | Payment processing |```bash

| **Analytics Service** | 8087 | MongoDB | Usage analytics & insights |# Build all services

mvn clean package

### Infrastructure Services

# Build specific service

| Service | Port | Description |cd [service-name]

|---------|------|-------------|mvn clean package

| **MongoDB** | 27017 | Document database |```

| **PostgreSQL** | 5432 | Relational database |

| **Kafka** | 9092 | Event streaming |## 🐛 Troubleshooting

| **Zookeeper** | 2181 | Kafka coordination |

| **Zipkin** | 9411 | Distributed tracing |### Services not registering with Eureka



## 📚 API Documentation- Check if discovery-server is running on port 8761

- Verify `eureka.client.service-url.defaultZone` in application.yml

### User Service API

### Database connection issues

#### Register User

```http- Ensure MongoDB/PostgreSQL are running

POST /api/users/register- Check connection strings in application.yml

Content-Type: application/json

### Kafka connection issues

{

  "name": "John Doe",- Verify Kafka and Zookeeper are running

  "email": "john@example.com",- Check `spring.kafka.bootstrap-servers` configuration

  "password": "password123",

  "phone": "+1234567890",## 📄 License

  "defaultCurrency": "USD"

}This project is licensed under the MIT License.

```

## 👥 Contributors

#### Login

```httpBuilt with ❤️ for learning microservices architecture

POST /api/users/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

#### Get User Profile
```http
GET /api/users/{userId}
Authorization: Bearer <token>
```

#### Update Profile
```http
PUT /api/users/{userId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "John Updated",
  "phone": "+9876543210"
}
```

#### Add Friend
```http
POST /api/users/{userId}/friends/{friendId}
Authorization: Bearer <token>
```

#### Search Users
```http
GET /api/users/search?query=john
Authorization: Bearer <token>
```

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Tests for Specific Service
```bash
cd user-service
mvn test
```

### Integration Testing
```bash
# Start services
docker compose up -d

# Run integration tests
./run-tests.sh
```

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
# Check all services
curl http://localhost:8080/actuator/health

# Check specific service (e.g., User Service)
curl http://localhost:8081/actuator/health
```

### View Logs
```bash
# View logs for all services
docker compose logs -f

# View logs for specific service
docker logs -f user-service
```

## 🔧 Development

### Project Structure
```
SplitIt/
├── discovery-server/        # Eureka service registry
├── api-gateway/             # Spring Cloud Gateway
├── user-service/            # User management
├── group-service/           # Group management
├── expense-service/         # Expense tracking
├── settlement-service/      # Debt settlement
├── notification-service/    # Notifications
├── payment-service/         # Payment processing
├── analytics-service/       # Analytics & insights
├── docker-compose.yml       # Multi-container setup
├── pom.xml                  # Parent POM
├── build.sh                 # Build script
└── start.sh                 # Startup script
```

### Adding a New Service

1. Create service module structure
2. Add module to parent `pom.xml`
3. Configure `application.yml` with Eureka client
4. Add route in API Gateway
5. Create Dockerfile
6. Add service to `docker-compose.yml`

### Environment Variables

Create `.env` file for local development:
```env
# Database
POSTGRES_USER=admin
POSTGRES_PASSWORD=REDACTED_PASSWORD
POSTGRES_DB=splitwise

# MongoDB
MONGO_ROOT_USERNAME=admin
MONGO_ROOT_PASSWORD=REDACTED_PASSWORD

# Kafka
KAFKA_BROKER=kafka:29092

# Eureka
EUREKA_SERVER=http://discovery-server:8761/eureka
```

## 🐳 Docker Commands

### Start all services
```bash
docker compose up -d
```

### Stop all services
```bash
docker compose down
```

### Rebuild and restart
```bash
docker compose up -d --build
```

### View logs
```bash
docker compose logs -f [service-name]
```

### Clean up volumes
```bash
docker compose down -v
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Java naming conventions
- Write meaningful commit messages
- Add unit tests for new features
- Update documentation as needed

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Dhruv Patel**
- GitHub: [@Dhruv1030](https://github.com/Dhruv1030)

## 🙏 Acknowledgments

- Inspired by [Splitwise](https://www.splitwise.com)
- Spring Boot & Spring Cloud communities
- All contributors and supporters

## 📧 Contact

For questions or support, please open an issue on GitHub.

---

**⭐ Star this repository if you find it helpful!**
