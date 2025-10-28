# Discovery Server - Complete Documentation 🔍

## 📋 Overview
**Status**: ✅ PRODUCTION READY  
**Port**: 8761  
**Type**: Netflix Eureka Server  
**Purpose**: Service discovery and registration for microservices  

The Discovery Server is the **service registry** for the SplitIt platform, providing:
- 🔍 **Service Discovery** (find services dynamically)
- 📝 **Service Registration** (auto-registration of services)
- ❤️ **Health Monitoring** (track service health)
- ⚖️ **Load Balancing** (distribute requests)
- 🔄 **High Availability** (failover support)

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Discovery Server                         │
│                 (Eureka Server - 8761)                      │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Service Registry                                    │  │
│  │                                                      │  │
│  │  ┌────────────────────────────────────────────────┐ │  │
│  │  │ API Gateway        │ Status: UP  │ Port: 8080 │ │  │
│  │  │ User Service       │ Status: UP  │ Port: 8081 │ │  │
│  │  │ Group Service      │ Status: UP  │ Port: 8082 │ │  │
│  │  │ Expense Service    │ Status: UP  │ Port: 8083 │ │  │
│  │  │ Settlement Service │ Status: UP  │ Port: 8084 │ │  │
│  │  │ Payment Service    │ Status: UP  │ Port: 8086 │ │  │
│  │  │ Analytics Service  │ Status: UP  │ Port: 8087 │ │  │
│  │  │ Notification Svc   │ Status: UP  │ Port: 8085 │ │  │
│  │  └────────────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Health Check System                                 │  │
│  │  - Heartbeat every 30 seconds                        │  │
│  │  - Remove if 3 heartbeats missed (90 seconds)        │  │
│  │  - Self-preservation mode if >85% down               │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────┬───────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┬─────────────┐
        │                 │                 │             │
        ▼                 ▼                 ▼             ▼
   [Services]        [Services]        [Services]    [Services]
  (register)        (discover)        (heartbeat)   (fetch)
```

## 🔑 Key Features

### 1. **Service Registration** 📝

**How it works**:
1. Service starts up
2. Service sends registration request to Eureka
3. Eureka stores service metadata
4. Service appears in registry

**Metadata stored**:
```json
{
  "instance": {
    "instanceId": "api-gateway:8080",
    "app": "API-GATEWAY",
    "ipAddr": "172.18.0.5",
    "status": "UP",
    "port": {
      "enabled": true,
      "$": 8080
    },
    "healthCheckUrl": "http://172.18.0.5:8080/actuator/health",
    "homePageUrl": "http://172.18.0.5:8080/",
    "metadata": {
      "management.port": "8080"
    }
  }
}
```

### 2. **Service Discovery** 🔍

**Client-side discovery**:
```java
// API Gateway discovers services
@Bean
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("user-service", r -> r
            .path("/api/users/**")
            .uri("lb://user-service"))  // lb:// = load balanced via Eureka
        .build();
}
```

**Benefits**:
- ✅ No hardcoded URLs
- ✅ Automatic service discovery
- ✅ Dynamic scaling support
- ✅ Failover to healthy instances

### 3. **Health Monitoring** ❤️

**Heartbeat mechanism**:
```
Service → Send heartbeat every 30s → Eureka
                                      ↓
                              Update last seen time
                                      ↓
                         If no heartbeat for 90s → Remove instance
```

**Health check**:
- Endpoint: `/actuator/health`
- Frequency: Every 30 seconds
- Timeout: 90 seconds (3 missed heartbeats)
- Action: Remove unhealthy instances

### 4. **Load Balancing** ⚖️

**Ribbon load balancer**:
```
Client request → Eureka (fetch instances) → Ribbon (pick instance) → Service
```

**Strategies**:
- **Round Robin** (default): Rotate through instances
- **Random**: Random selection
- **Weighted**: Based on response time
- **Availability**: Prefer instances with lower load

### 5. **Self-Preservation Mode** 🛡️

**Purpose**: Prevent cascading failures during network issues

**Trigger**:
- When >85% of services miss heartbeats
- Possible network partition

**Behavior**:
- ✅ Stop removing instances
- ✅ Keep "dead" instances in registry
- ✅ Wait for network recovery
- ✅ Prefer availability over consistency

**Warning message**:
```
EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT.
RENEWALS ARE LESSER THAN THRESHOLD AND HENCE THE INSTANCES ARE NOT BEING EXPIRED.
```

## 📡 Eureka Dashboard

### Access

```
http://localhost:8761
```

### Dashboard Sections

#### 1. **System Status**
```
Environment: test
Data center: default
Current time: 2025-10-28 12:00:00
Uptime: 1 hour 23 minutes
Lease expiration: enabled
Renews threshold: 2 per minute
Renews (last min): 8
```

#### 2. **Instances Currently Registered**
```
Application         AMIs    Availability Zones    Status
API-GATEWAY         1       1                     UP (1) - api-gateway:8080
USER-SERVICE        1       1                     UP (1) - user-service:8081
GROUP-SERVICE       1       1                     UP (1) - group-service:8082
EXPENSE-SERVICE     1       1                     UP (1) - expense-service:8083
SETTLEMENT-SERVICE  1       1                     UP (1) - settlement-service:8084
NOTIFICATION-SERVICE 1      1                     UP (1) - notification-service:8085
PAYMENT-SERVICE     1       1                     UP (1) - payment-service:8086
ANALYTICS-SERVICE   1       1                     UP (1) - analytics-service:8087
```

#### 3. **General Info**
```
total-avail-memory: 8192mb
environment: test
num-of-cpus: 8
current-memory-usage: 512mb (6%)
server-uptime: 1h 23m
registered-replicas: N/A
unavailable-replicas: N/A
available-replicas: N/A
```

## 🔧 Configuration

### application.yml

```yaml
server:
  port: 8761

spring:
  application:
    name: discovery-server

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false   # Don't register itself
    fetch-registry: false          # Don't fetch registry (it IS the registry)
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  server:
    enable-self-preservation: true  # Enable self-preservation mode
    eviction-interval-timer-in-ms: 60000  # Check for expired instances every 60s
```

### Docker Configuration

```yaml
spring:
  config:
    activate:
      on-profile: docker

eureka:
  instance:
    hostname: discovery-server
    prefer-ip-address: true
  client:
    service-url:
      defaultZone: http://discovery-server:8761/eureka/
```

## 🧪 Testing Eureka

### 1. **Check Eureka Status**

```bash
curl http://localhost:8761/eureka/apps | jq
```

**Response**: XML with all registered applications

### 2. **Check Specific Service**

```bash
curl http://localhost:8761/eureka/apps/user-service | jq
```

### 3. **Get All Services (JSON)**

```bash
curl -H "Accept: application/json" \
  http://localhost:8761/eureka/apps
```

### 4. **Health Check**

```bash
curl http://localhost:8761/actuator/health
```

**Response**:
```json
{
  "status": "UP"
}
```

### 5. **Verify Service Registration**

```bash
# Register a service
curl -X POST http://localhost:8761/eureka/apps/TEST-SERVICE \
  -H "Content-Type: application/json" \
  -d '{
    "instance": {
      "instanceId": "test:8090",
      "app": "TEST-SERVICE",
      "ipAddr": "127.0.0.1",
      "port": {"$": 8090, "@enabled": true}
    }
  }'

# Verify registration
curl http://localhost:8761/eureka/apps/TEST-SERVICE
```

## 🔄 Service Lifecycle

### 1. **Service Startup**

```
Service starts
     ↓
Configure Eureka client
     ↓
Connect to Eureka server
     ↓
Send registration request
     ↓
Start heartbeat (30s interval)
     ↓
Service available in registry
```

### 2. **Service Running**

```
Every 30 seconds:
Service → Send heartbeat → Eureka
                            ↓
                    Update last renewal time
                            ↓
                    Keep instance active
```

### 3. **Service Shutdown**

```
Graceful shutdown initiated
     ↓
Send deregistration request
     ↓
Remove from registry
     ↓
Stop heartbeats
     ↓
Service unavailable
```

### 4. **Service Crash (No Graceful Shutdown)**

```
Service crashes
     ↓
Stop sending heartbeats
     ↓
90 seconds pass (3 missed heartbeats)
     ↓
Eureka removes instance
     ↓
No longer routed to crashed instance
```

## 📊 REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/eureka/apps` | Get all registered applications |
| GET | `/eureka/apps/{appName}` | Get specific application |
| GET | `/eureka/apps/{appName}/{instanceId}` | Get specific instance |
| POST | `/eureka/apps/{appName}` | Register new instance |
| DELETE | `/eureka/apps/{appName}/{instanceId}` | Deregister instance |
| PUT | `/eureka/apps/{appName}/{instanceId}` | Send heartbeat |
| PUT | `/eureka/apps/{appName}/{instanceId}/status` | Update status |
| DELETE | `/eureka/apps/{appName}/{instanceId}/status` | Remove status override |
| GET | `/actuator/health` | Health check |
| GET | `/` | Eureka dashboard (HTML) |

## 🚀 Deployment

### Build

```bash
cd discovery-server
mvn clean package -DskipTests
```

### Run Locally

```bash
java -jar target/discovery-server-1.0.0.jar
```

### Docker

```bash
docker compose up -d discovery-server
```

### Verify Deployment

```bash
# Check if running
curl http://localhost:8761/actuator/health

# View dashboard
open http://localhost:8761

# Check logs
docker compose logs -f discovery-server
```

## 🐛 Troubleshooting

### Issue: Services not registering

**Cause**: Incorrect Eureka URL or network issues

**Solution**:
```bash
# Check service logs
docker compose logs <service-name> | grep "eureka"

# Verify Eureka is running
curl http://localhost:8761/actuator/health

# Check service configuration
cat <service>/src/main/resources/application.yml | grep eureka
```

### Issue: Self-preservation mode activated

**Cause**: Network issues or too many services down

**Message**:
```
EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP
```

**Solution**:
```bash
# Check if services are actually down
docker compose ps

# Restart unhealthy services
docker compose restart <service-name>

# Wait for self-preservation to disable (when >85% are healthy)
```

### Issue: Stale instances in registry

**Cause**: Services crashed without deregistering

**Solution**:
```bash
# Wait 90 seconds for auto-removal
sleep 90

# Or manually remove
curl -X DELETE http://localhost:8761/eureka/apps/SERVICE-NAME/instance-id

# Or disable self-preservation temporarily
eureka:
  server:
    enable-self-preservation: false
```

## 🎯 Best Practices

### 1. **Client Configuration**

```yaml
eureka:
  client:
    fetch-registry: true           # Enable registry caching
    register-with-eureka: true     # Enable registration
    registry-fetch-interval-seconds: 30  # Refresh cache every 30s
  instance:
    lease-renewal-interval-in-seconds: 30    # Send heartbeat every 30s
    lease-expiration-duration-in-seconds: 90  # Remove if 90s no heartbeat
```

### 2. **Health Check Endpoint**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

### 3. **Service ID Naming**

- Use lowercase
- Use hyphens (not underscores)
- Be descriptive
- Examples: `user-service`, `api-gateway`, `payment-service`

### 4. **High Availability Setup**

```yaml
# Multiple Eureka servers (peer-to-peer)
eureka:
  client:
    service-url:
      defaultZone: http://eureka1:8761/eureka/,http://eureka2:8762/eureka/
```

## 📈 Monitoring

### Metrics to Track

1. **Registration Rate**: New services per minute
2. **Heartbeat Rate**: Renewals per minute
3. **Instance Count**: Total registered instances
4. **Eviction Rate**: Instances removed per minute
5. **Self-Preservation Status**: Enabled/disabled

### Log Levels

```yaml
logging:
  level:
    com.netflix.eureka: DEBUG
    com.netflix.discovery: DEBUG
```

## 🔗 Integration with Services

### Service Configuration Example

```yaml
# user-service/application.yml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    fetch-registry: true
    register-with-eureka: true
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${server.port}
```

### Service Code

```java
@SpringBootApplication
@EnableDiscoveryClient  // Enable Eureka client
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

## 🎯 Future Enhancements

- [ ] Eureka server clustering (high availability)
- [ ] Custom health check strategy
- [ ] Service metadata tags
- [ ] Region and zone awareness
- [ ] Dynamic service configuration
- [ ] Service mesh integration
- [ ] Kubernetes native discovery
- [ ] Service dependency visualization

---

**Created**: October 28, 2025  
**Status**: ✅ Production Ready  
**Port**: 8761  
**Type**: Netflix Eureka Server  

**Key Achievement**: Central service discovery enabling dynamic routing and load balancing for all microservices! 🔍
