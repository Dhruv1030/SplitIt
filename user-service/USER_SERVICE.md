# User Service - Complete Documentation 👤

## 📋 Overview
**Status**: ✅ PRODUCTION READY  
**Port**: 8081  
**Database**: MongoDB  
**Purpose**: User authentication, profile management, and JWT generation  

The User Service is the **authentication hub** of the SplitIt platform, handling:
- 🔐 **User Registration & Login**
- 🎫 **JWT Token Generation & Validation**
- 👤 **User Profile Management**
- 👥 **Friend Management**
- 🔍 **User Search**

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    User Service                         │
│                   (Port 8081)                           │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │  UserController (REST API)                       │  │
│  │  - /api/users/register                           │  │
│  │  - /api/users/login                              │  │
│  │  - /api/users/{id}                               │  │
│  │  - /api/users/{id}/friends                       │  │
│  │  - /api/users/search                             │  │
│  └───────────────────┬──────────────────────────────┘  │
│                      │                                  │
│  ┌───────────────────▼──────────────────────────────┐  │
│  │  UserService (Business Logic)                    │  │
│  │  - Registration with password hashing            │  │
│  │  - Login with credential validation              │  │
│  │  - Profile CRUD operations                       │  │
│  │  - Friend management                             │  │
│  └───────────────────┬──────────────────────────────┘  │
│                      │                                  │
│  ┌───────────────────▼──────────────────────────────┐  │
│  │  JwtTokenProvider                                │  │
│  │  - Generate JWT tokens (HS512)                   │  │
│  │  - Validate tokens                               │  │
│  │  - Extract user info from tokens                 │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │  UserRepository (MongoDB)                        │  │
│  │  - findByEmail, findById                         │  │
│  │  - existsByEmail                                 │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
                  ┌─────────────┐
                  │  MongoDB    │
                  │  (userdb)   │
                  │  Port 27017 │
                  └─────────────┘
```

## 🔑 Key Features

### 1. **User Registration** 📝

**Endpoint**: `POST /api/users/register`

**Request**:
```json
{
  "name": "Alice Smith",
  "email": "alice@example.com",
  "password": "securepass123",
  "phone": "+1-555-0100",
  "avatar": "https://example.com/avatar.jpg"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "user": {
    "id": "690050fd8006473761a88fbd",
    "name": "Alice Smith",
    "email": "alice@example.com",
    "phone": "+1-555-0100",
    "avatar": "https://example.com/avatar.jpg",
    "friendIds": [],
    "defaultCurrency": "USD",
    "createdAt": "2025-10-28T12:00:00",
    "emailVerified": false
  }
}
```

**Features**:
- ✅ Email uniqueness validation
- ✅ Password hashing with BCrypt
- ✅ Automatic JWT token generation
- ✅ Default currency set to USD
- ✅ Empty friends list initialization

### 2. **User Login** 🔐

**Endpoint**: `POST /api/users/login`

**Request**:
```json
{
  "email": "alice@example.com",
  "password": "securepass123"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "user": {
    "id": "690050fd8006473761a88fbd",
    "name": "Alice Smith",
    "email": "alice@example.com",
    ...
  }
}
```

**Security**:
- ✅ BCrypt password verification
- ✅ JWT token with 1-hour expiration
- ✅ User roles included in token
- ✅ Secure password storage (never returned)

### 3. **JWT Token Generation** 🎫

**Algorithm**: HS512 (HMAC with SHA-512)

**Token Structure**:
```json
{
  "header": {
    "alg": "HS512"
  },
  "payload": {
    "sub": "690050fd8006473761a88fbd",
    "email": "alice@example.com",
    "roles": "ROLE_USER",
    "iat": 1761668923,
    "exp": 1761672523
  },
  "signature": "..."
}
```

**Claims**:
- `sub`: User ID (subject)
- `email`: User email
- `roles`: User roles (ROLE_USER, ROLE_ADMIN)
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp (1 hour)

### 4. **Profile Management** 👤

**Get User Profile**:
```bash
GET /api/users/{id}
Authorization: Bearer <token>
```

**Update User Profile**:
```bash
PUT /api/users/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Alice Johnson",
  "phone": "+1-555-0200",
  "avatar": "https://example.com/new-avatar.jpg",
  "defaultCurrency": "EUR"
}
```

### 5. **Friend Management** 👥

**Add Friend**:
```bash
POST /api/users/{userId}/friends
Authorization: Bearer <token>
Content-Type: application/json

{
  "friendId": "690051234567890123456789"
}
```

**Get Friends List**:
```bash
GET /api/users/{userId}/friends
Authorization: Bearer <token>
```

**Response**:
```json
{
  "userId": "690050fd8006473761a88fbd",
  "friends": [
    {
      "id": "690051234567890123456789",
      "name": "Bob Jones",
      "email": "bob@example.com",
      "avatar": "https://example.com/bob.jpg"
    },
    ...
  ]
}
```

### 6. **User Search** 🔍

**Search by Email**:
```bash
GET /api/users/search?email=alice@example.com
Authorization: Bearer <token>
```

**Search by Name**:
```bash
GET /api/users/search?name=Alice
Authorization: Bearer <token>
```

**Response**:
```json
{
  "users": [
    {
      "id": "690050fd8006473761a88fbd",
      "name": "Alice Smith",
      "email": "alice@example.com",
      "avatar": "https://example.com/avatar.jpg"
    },
    ...
  ]
}
```

## 📡 Complete API Reference

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/users/register` | ❌ | Register new user |
| POST | `/api/users/login` | ❌ | Login and get JWT |
| GET | `/api/users/{id}` | ✅ | Get user profile |
| PUT | `/api/users/{id}` | ✅ | Update profile |
| DELETE | `/api/users/{id}` | ✅ | Delete account |
| POST | `/api/users/{id}/friends` | ✅ | Add friend |
| DELETE | `/api/users/{id}/friends/{friendId}` | ✅ | Remove friend |
| GET | `/api/users/{id}/friends` | ✅ | Get friends list |
| GET | `/api/users/search` | ✅ | Search users |
| GET | `/api/users/health` | ❌ | Health check |

## 🗄️ Database Schema

### User Document (MongoDB)

```javascript
{
  "_id": ObjectId("690050fd8006473761a88fbd"),
  "name": "Alice Smith",
  "email": "alice@example.com",
  "password": "$2a$10$...", // BCrypt hash
  "phone": "+1-555-0100",
  "avatar": "https://example.com/avatar.jpg",
  "friendIds": [
    "690051234567890123456789",
    "690051234567890123456790"
  ],
  "defaultCurrency": "USD",
  "createdAt": ISODate("2025-10-28T12:00:00Z"),
  "emailVerified": false
}
```

**Indexes**:
- `email`: Unique index for fast lookup
- `_id`: Default primary key

## 🔒 Security Features

### 1. **Password Security**
- ✅ BCrypt hashing (strength factor: 10)
- ✅ Never return password in responses
- ✅ Secure password comparison

### 2. **JWT Security**
- ✅ HS512 algorithm (512-bit key)
- ✅ 1-hour token expiration
- ✅ Signed with shared secret
- ✅ Tamper-proof tokens

### 3. **Input Validation**
- ✅ Email format validation
- ✅ Required field validation
- ✅ Password strength requirements (future)

### 4. **Authorization**
- ✅ User can only update their own profile
- ✅ JWT validation via API Gateway
- ✅ Role-based access control ready

## 🧪 Testing Examples

### 1. **Register a New User**

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "test123"
  }'
```

### 2. **Login**

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123"
  }'
```

### 3. **Get User Profile**

```bash
TOKEN="your-jwt-token-here"

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users/{userId}
```

### 4. **Update Profile**

```bash
curl -X PUT http://localhost:8080/api/users/{userId} \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Name",
    "phone": "+1-555-9999"
  }'
```

### 5. **Add Friend**

```bash
curl -X POST http://localhost:8080/api/users/{userId}/friends \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "friendId": "690051234567890123456789"
  }'
```

### 6. **Search Users**

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/users/search?email=alice@example.com"
```

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Service port | 8081 |
| `MONGODB_URI` | MongoDB connection string | mongodb://admin:admin123@localhost:27017/userdb |
| `JWT_SECRET` | JWT signing key | Must be 256+ bits |
| `JWT_EXPIRATION_MS` | Token expiration time | 3600000 (1 hour) |
| `EUREKA_URL` | Eureka server URL | http://localhost:8761/eureka |

### Docker Configuration

```yaml
spring:
  config:
    activate:
      on-profile: docker
  data:
    mongodb:
      uri: mongodb://admin:admin123@mongodb:27017/userdb?authSource=admin
```

## 🚀 Deployment

### Build

```bash
cd user-service
mvn clean package -DskipTests
```

### Run Locally

```bash
java -jar target/user-service-1.0.0.jar
```

### Docker

```bash
docker compose up -d user-service mongodb
```

### Verify

```bash
# Health check
curl http://localhost:8081/actuator/health

# Check Eureka registration
curl http://localhost:8761/eureka/apps/user-service
```

## 📊 Integration Points

| Service | Purpose | Communication |
|---------|---------|---------------|
| **API Gateway** | Routes all requests | HTTP via Eureka |
| **MongoDB** | Data persistence | MongoDB driver |
| **Eureka** | Service discovery | REST API |
| **Zipkin** | Distributed tracing | HTTP spans |
| **Kafka** | Future events | Not yet implemented |

## 🎯 Future Enhancements

- [ ] Email verification workflow
- [ ] Password reset functionality
- [ ] OAuth2 integration (Google, Facebook)
- [ ] Two-factor authentication (2FA)
- [ ] Profile picture upload to S3
- [ ] User preferences and settings
- [ ] Account activity logging
- [ ] Password strength validation
- [ ] Rate limiting on login attempts
- [ ] User analytics and statistics

---

**Created**: October 28, 2025  
**Status**: ✅ Production Ready  
**Database**: MongoDB (userdb)  
**Port**: 8081  

**Key Achievement**: Secure authentication and user management with JWT token generation for the entire platform! 🔐
