# API Gateway - Complete Documentation 🚪

## 📋 Overview
**Status**: ✅ PRODUCTION READY  
**Port**: 8080  
**Type**: Spring Cloud Gateway  
**Purpose**: Single entry point for all microservices  

The API Gateway acts as the **front door** to the SplitIt platform, providing:
- 🔐 **Centralized Authentication** (JWT validation)
- 🔀 **Intelligent Routing** (service discovery)
- 🛡️ **Circuit Breaker** (fault tolerance)
- 📊 **Distributed Tracing** (Zipkin integration)
- 🔒 **Security** (header injection, rate limiting ready)

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Client                              │
│                    (Mobile/Web/API)                         │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            │ HTTP/HTTPS
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                      API Gateway                            │
│                     (Port 8080)                             │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  JwtAuthenticationFilter (GlobalFilter)              │  │
│  │  - Validates JWT tokens                              │  │
│  │  - Extracts user info (userId, email, roles)         │  │
│  │  - Injects headers for downstream services           │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  GatewayConfig (RouteLocator)                        │  │
│  │  - Defines routing rules                             │  │
│  │  - Uses Eureka for service discovery                 │  │
│  │  - Circuit breaker configuration                     │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────┬───────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┬─────────────┐
        │                 │                 │             │
┌───────▼───────┐  ┌──────▼──────┐  ┌──────▼──────┐  ┌──▼────┐
│ User Service  │  │Group Service│  │Expense Svc  │  │  ...  │
│   (8081)      │  │   (8082)    │  │   (8083)    │  │       │
└───────────────┘  └─────────────┘  └─────────────┘  └───────┘
```

## 🔑 Key Components

### 1. **JwtAuthenticationFilter** 🔐

**Purpose**: Global filter that validates JWT tokens for all incoming requests

**Flow**:
```
Request → Check if public path? → Yes → Allow
                ↓
               No
                ↓
        Extract Authorization header
                ↓
        Validate JWT token
                ↓
        Extract claims (userId, email, roles)
                ↓
        Inject headers (X-User-Id, X-User-Email, X-User-Roles)
                ↓
        Forward to downstream service
```

**Public Paths** (No authentication required):
- `/api/users/register` - User registration
- `/api/users/login` - User login
- `/eureka/**` - Eureka dashboard

**Protected Paths** (JWT required):
- All other endpoints require `Authorization: Bearer <token>`

**Header Injection**:
```java
X-User-Id: "690050fd8006473761a88fbd"
X-User-Email: "alice@example.com"
X-User-Roles: "ROLE_USER"
```

**Key Features**:
- ✅ Uses HS512 algorithm with shared secret
- ✅ High priority (Order = -100) to run early
- ✅ Returns 401 Unauthorized for invalid tokens
- ✅ Reactive programming with Spring WebFlux

### 2. **GatewayConfig** 🔀

**Purpose**: Defines routing rules for all microservices

**Routing Strategy**:
```yaml
Path Pattern → Load Balancer → Service Discovery
```

**Routes Configuration**:

| Service | Path Pattern | Target URI | Port |
|---------|-------------|-----------|------|
| User Service | `/api/users/**` | `lb://user-service` | 8081 |
| Group Service | `/api/groups/**` | `lb://group-service` | 8082 |
| Expense Service | `/api/expenses/**` | `lb://expense-service` | 8083 |
| Settlement Service | `/api/settlements/**` | `lb://settlement-service` | 8084 |
| Payment Service | `/api/payments/**` | `lb://payment-service` | 8086 |
| Analytics Service | `/api/analytics/**` | `lb://analytics-service` | 8087 |

**Load Balancer** (`lb://`):
- Uses **Eureka Discovery** to find service instances
- Automatic **client-side load balancing**
- Routes to healthy instances only

### 3. **Circuit Breaker** 🛡️

**Purpose**: Fault tolerance and resilience

**Configuration**:
```yaml
Sliding Window Size: 10 requests
Minimum Calls: 5 (before calculating failure rate)
Failure Rate Threshold: 50%
Wait Duration in Open State: 5 seconds
Slow Call Threshold: 60 seconds
```

**Circuit States**:
1. **CLOSED** (Normal): All requests flow through
2. **OPEN** (Broken): Requests fail fast, fallback triggered
3. **HALF-OPEN** (Testing): Allows limited requests to test recovery

**Circuit Breakers per Service**:
- `userServiceCircuitBreaker`
- `groupServiceCircuitBreaker`
- `expenseServiceCircuitBreaker`
- `settlementServiceCircuitBreaker`
- `paymentServiceCircuitBreaker`
- `analyticsServiceCircuitBreaker`

**Fallback URIs**:
```
User Service → /fallback/users
Group Service → /fallback/groups
Expense Service → /fallback/expenses
```

### 4. **Service Discovery Integration** 🔍

**Eureka Client Configuration**:
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    fetch-registry: true
    register-with-eureka: true
```

**Benefits**:
- ✅ Dynamic service discovery (no hardcoded URLs)
- ✅ Automatic instance registration
- ✅ Health-based routing
- ✅ Load balancing across multiple instances

### 5. **Distributed Tracing** 📊

**Zipkin Integration**:
```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% sampling
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

**Features**:
- ✅ Request flow visualization
- ✅ Performance bottleneck identification
- ✅ Error tracking across services
- ✅ 100% trace sampling (configurable)

## 🔐 Authentication Flow

### Complete JWT Flow

```
┌─────────┐                                  ┌──────────────┐
│ Client  │                                  │ User Service │
└────┬────┘                                  └──────┬───────┘
     │                                              │
     │  1. POST /api/users/login                   │
     │  {email, password}                          │
     │────────────────────────────────────────────►│
     │                                              │
     │  2. Validate credentials                    │
     │     Generate JWT token                      │
     │◄────────────────────────────────────────────│
     │  {token: "eyJhbGci...", user: {...}}       │
     │                                              │
     
┌────▼────┐                  ┌──────────────┐                ┌──────────────┐
│ Client  │                  │ API Gateway  │                │Expense Service│
└────┬────┘                  └──────┬───────┘                └──────┬───────┘
     │                              │                               │
     │  3. GET /api/expenses/balance                               │
     │     Authorization: Bearer eyJhbGci...                        │
     │─────────────────────────────►│                               │
     │                               │                               │
     │                               │  4. Validate JWT             │
     │                               │     Extract claims           │
     │                               │                               │
     │                               │  5. Inject headers           │
     │                               │     X-User-Id: "690..."      │
     │                               │     X-User-Email: "user@..." │
     │                               │─────────────────────────────►│
     │                               │                               │
     │                               │  6. Process request          │
     │                               │     using X-User-Id          │
     │                               │◄─────────────────────────────│
     │  7. Return response           │  {balance: {...}}            │
     │◄─────────────────────────────│                               │
     │  {balance: {...}}             │                               │
```

### JWT Token Structure

**Header**:
```json
{
  "alg": "HS512"
}
```

**Payload** (Claims):
```json
{
  "sub": "690050fd8006473761a88fbd",
  "email": "alice@example.com",
  "roles": "ROLE_USER",
  "iat": 1761668923,
  "exp": 1761672523
}
```

**Signature**:
```
HMACSHA512(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  JWT_SECRET
)
```

## 📡 API Endpoints (Gateway Routes)

### User Service Routes

| Method | Endpoint | Authentication | Description |
|--------|----------|----------------|-------------|
| POST | `/api/users/register` | ❌ Public | Register new user |
| POST | `/api/users/login` | ❌ Public | Login and get JWT |
| GET | `/api/users/{id}` | ✅ Required | Get user profile |
| PUT | `/api/users/{id}` | ✅ Required | Update user profile |
| POST | `/api/users/{id}/friends` | ✅ Required | Add friend |
| GET | `/api/users/{id}/friends` | ✅ Required | List friends |
| GET | `/api/users/search` | ✅ Required | Search users |

### Group Service Routes

| Method | Endpoint | Authentication | Description |
|--------|----------|----------------|-------------|
| POST | `/api/groups` | ✅ Required | Create group |
| GET | `/api/groups/{id}` | ✅ Required | Get group details |
| PUT | `/api/groups/{id}` | ✅ Required | Update group |
| DELETE | `/api/groups/{id}` | ✅ Required | Delete group |
| POST | `/api/groups/{id}/members` | ✅ Required | Add member |
| DELETE | `/api/groups/{id}/members/{userId}` | ✅ Required | Remove member |
| GET | `/api/groups/user/{userId}` | ✅ Required | Get user's groups |

### Expense Service Routes

| Method | Endpoint | Authentication | Description |
|--------|----------|----------------|-------------|
| POST | `/api/expenses` | ✅ Required | Create expense |
| GET | `/api/expenses/{id}` | ✅ Required | Get expense |
| PUT | `/api/expenses/{id}` | ✅ Required | Update expense |
| DELETE | `/api/expenses/{id}` | ✅ Required | Delete expense |
| GET | `/api/expenses/group/{groupId}` | ✅ Required | Get group expenses |
| GET | `/api/expenses/my-expenses` | ✅ Required | Get user expenses |
| GET | `/api/expenses/balance` | ✅ Required | Get user balance |

### Settlement Service Routes

| Method | Endpoint | Authentication | Description |
|--------|----------|----------------|-------------|
| GET | `/api/settlements/group/{groupId}` | ✅ Required | Calculate settlements |
| POST | `/api/settlements/settle` | ✅ Required | Mark settlement complete |
| GET | `/api/settlements/user/{userId}` | ✅ Required | Get user settlements |

### Payment Service Routes

| Method | Endpoint | Authentication | Description |
|--------|----------|----------------|-------------|
| POST | `/api/payments/initiate` | ✅ Required | Initiate payment |
| GET | `/api/payments/{id}` | ✅ Required | Get payment status |
| POST | `/api/payments/webhook` | ❌ Public | Payment gateway webhook |

### Analytics Service Routes

| Method | Endpoint | Authentication | Description |
|--------|----------|----------------|-------------|
| GET | `/api/analytics/user/{userId}/summary` | ✅ Required | User spending summary |
| GET | `/api/analytics/group/{groupId}/summary` | ✅ Required | Group analytics |
| GET | `/api/analytics/category-breakdown` | ✅ Required | Category analysis |

## 🧪 Testing the API Gateway

### 1. **Health Check**

```bash
curl http://localhost:8080/actuator/health
```

**Expected Response**:
```json
{
  "status": "UP"
}
```

### 2. **Test Public Endpoint (No JWT)**

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "test123"
  }'
```

**Expected**: 200 OK with JWT token

### 3. **Test Protected Endpoint WITHOUT JWT (Should Fail)**

```bash
curl -v http://localhost:8080/api/expenses/balance
```

**Expected**: 401 Unauthorized

### 4. **Test Protected Endpoint WITH JWT (Should Succeed)**

```bash
TOKEN="your-jwt-token-here"

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/expenses/balance
```

**Expected**: 200 OK with user balance data

### 5. **Verify Header Injection**

Check downstream service logs to see injected headers:
```bash
docker compose logs expense-service | grep "X-User-Id"
```

You should see: `X-User-Id: 690050fd8006473761a88fbd`

### 6. **Test Circuit Breaker**

Stop a service and make requests:
```bash
# Stop expense service
docker compose stop expense-service

# Try to access expense endpoint
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/expenses/balance
```

**Expected**: Fallback response or 503 Service Unavailable

### 7. **View Gateway Routes**

```bash
curl http://localhost:8080/actuator/gateway/routes | jq
```

**Expected**: List of all configured routes

## 📊 Monitoring & Observability

### Management Endpoints

```yaml
/actuator/health          # Health status
/actuator/info            # Application info
/actuator/gateway/routes  # All routes
/actuator/metrics         # Application metrics
```

### Zipkin Tracing

**Access Zipkin UI**:
```
http://localhost:9411
```

**Features**:
- View request traces across all services
- Identify slow endpoints
- Track errors and failures
- Analyze service dependencies

### Eureka Dashboard

**Access Eureka**:
```
http://localhost:8761
```

**View**:
- All registered service instances
- Service health status
- Instance metadata

## 🔒 Security Features

### 1. **JWT Validation**
- ✅ Validates signature using shared secret
- ✅ Checks token expiration
- ✅ Prevents token tampering

### 2. **Header Injection**
- ✅ Downstream services trust headers (no re-validation needed)
- ✅ Consistent user context across services
- ✅ Prevents header spoofing (clients can't inject these headers)

### 3. **CORS** (Ready to configure)
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "http://localhost:3000"
            allowedMethods: "*"
            allowedHeaders: "*"
```

### 4. **Rate Limiting** (Ready to add)
```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10
      redis-rate-limiter.burstCapacity: 20
```

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Gateway port | 8080 |
| `JWT_SECRET` | JWT signing key | Must be 256+ bits |
| `EUREKA_URL` | Eureka server URL | http://localhost:8761/eureka |
| `ZIPKIN_URL` | Zipkin tracing URL | http://localhost:9411 |

### Docker Profile

```yaml
spring:
  config:
    activate:
      on-profile: docker

eureka:
  client:
    service-url:
      defaultZone: http://discovery-server:8761/eureka/
```

## 🚀 Deployment

### Build

```bash
cd api-gateway
mvn clean package -DskipTests
```

### Run Locally

```bash
java -jar target/api-gateway-1.0.0.jar
```

### Docker

```bash
docker compose up -d api-gateway
```

### Verify Deployment

```bash
# Check if gateway is running
curl http://localhost:8080/actuator/health

# Check Eureka registration
curl http://localhost:8761/eureka/apps/api-gateway

# View logs
docker compose logs -f api-gateway
```

## 🐛 Troubleshooting

### Issue: 401 Unauthorized on all requests

**Cause**: JWT secret mismatch between user-service and api-gateway

**Solution**:
```bash
# Ensure both services use same JWT_SECRET
export JWT_SECRET="your-256-bit-secret"
docker compose restart user-service api-gateway
```

### Issue: 503 Service Unavailable

**Cause**: Service not registered with Eureka or circuit breaker open

**Solution**:
```bash
# Check Eureka dashboard
curl http://localhost:8761

# Check service logs
docker compose logs <service-name>

# Restart services
docker compose restart
```

### Issue: Headers not reaching downstream services

**Cause**: Filter order or configuration issue

**Solution**:
```bash
# Check gateway logs
docker compose logs api-gateway | grep "X-User-Id"

# Verify filter is registered
curl http://localhost:8080/actuator/gateway/filters
```

## 📈 Performance Considerations

### Reactive Programming
- ✅ Non-blocking I/O with Spring WebFlux
- ✅ Handles high concurrency efficiently
- ✅ Low memory footprint

### Load Balancing
- ✅ Client-side load balancing with Ribbon
- ✅ Automatic health-based routing
- ✅ Distributes load across instances

### Caching (Future Enhancement)
```yaml
filters:
  - name: LocalResponseCache
    args:
      size: 100MB
      timeToLive: 5m
```

## 🎯 Best Practices Implemented

1. ✅ **Single Entry Point**: All external requests go through gateway
2. ✅ **Centralized Authentication**: JWT validation in one place
3. ✅ **Service Discovery**: No hardcoded service URLs
4. ✅ **Circuit Breaker**: Fault tolerance for downstream failures
5. ✅ **Distributed Tracing**: Request flow visibility
6. ✅ **Header Injection**: Consistent user context
7. ✅ **Public/Protected Routes**: Clear security boundaries
8. ✅ **High Priority Filter**: Authentication runs early
9. ✅ **Reactive Architecture**: Non-blocking, high performance
10. ✅ **Docker Ready**: Easy deployment and scaling

## 📝 Future Enhancements

- [ ] Rate limiting with Redis
- [ ] Request/Response logging
- [ ] CORS configuration for web apps
- [ ] Request transformation filters
- [ ] Response caching for read-heavy endpoints
- [ ] API versioning support
- [ ] Swagger/OpenAPI documentation aggregation
- [ ] WebSocket routing support
- [ ] GraphQL gateway integration

---

**Created**: October 28, 2025  
**Status**: ✅ Production Ready  
**Port**: 8080  
**Dependencies**: Eureka Discovery Server, All microservices  

**Key Achievement**: Single entry point providing authentication, routing, and resilience for the entire SplitIt platform! 🚀
