# 📚 SplitIt API Documentation Guide

## Overview

This guide provides information about accessing and using the SplitIt API documentation through Swagger UI.

## 🌐 Swagger UI Access Points

### Via API Gateway (Recommended for Frontend)
The API Gateway aggregates all service APIs at:
- **URL**: `http://localhost:8080`
- **Access individual service docs through their paths**

### Individual Service Documentation

#### User Service
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs
- **Base Path**: `/api/users`
- **Description**: User registration, authentication, profile management, and friends

#### Group Service
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8082/v3/api-docs
- **Base Path**: `/api/groups`
- **Description**: Group creation, member management, and group settings

#### Expense Service
- **Swagger UI**: http://localhost:8083/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8083/v3/api-docs
- **Base Path**: `/api/expenses`
- **Description**: Expense creation, splitting, and tracking

#### Settlement Service
- **Swagger UI**: http://localhost:8084/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8084/v3/api-docs
- **Base Path**: `/api/settlements`
- **Description**: Balance calculation, debt optimization, and settlement

#### Notification Service
- **Swagger UI**: http://localhost:8085/swagger-ui.html
- **Base Path**: `/api/notifications`
- **Description**: Email and push notifications

#### Payment Service
- **Swagger UI**: http://localhost:8086/swagger-ui.html
- **Base Path**: `/api/payments`
- **Description**: Payment gateway integration and transaction handling

#### Analytics Service
- **Swagger UI**: http://localhost:8087/swagger-ui.html
- **Base Path**: `/api/analytics`
- **Description**: Spending analytics and reports

## 🔐 Authentication

Most endpoints require JWT authentication. Here's how to authenticate:

### 1. Register a New User
```bash
POST http://localhost:8080/api/users/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe",
  "phoneNumber": "+1234567890"
}
```

### 2. Login to Get JWT Token
```bash
POST http://localhost:8080/api/users/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "507f1f77bcf86cd799439011",
  "email": "user@example.com",
  "name": "John Doe"
}
```

### 3. Use Token in Requests

#### In Swagger UI:
1. Click the **"Authorize"** button (🔒) at the top
2. Enter: `Bearer <your-token-here>`
3. Click **"Authorize"**
4. All subsequent requests will include the token

#### In API Calls:
```bash
curl -X GET http://localhost:8080/api/users/{id} \
  -H "Authorization: Bearer <your-token-here>"
```

## 🌍 CORS Configuration

The API is configured to accept requests from the following origins:
- `http://localhost:3000` (React default)
- `http://localhost:4200` (Angular default)
- `http://localhost:5173` (Vite default)
- `http://localhost:8100` (Ionic default)

### Allowed Methods
- GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD

### Allowed Headers
- All headers (`*`)

### Exposed Headers
- `Authorization`
- `Content-Type`
- `X-Total-Count`
- `X-Request-Id`

## 📝 Common API Patterns

### Response Format
All successful responses follow this structure:
```json
{
  "id": "string",
  "timestamp": "2025-10-29T10:00:00Z",
  "data": { }
}
```

### Error Response Format
```json
{
  "timestamp": "2025-10-29T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/users/register"
}
```

### HTTP Status Codes
- `200 OK` - Successful GET, PUT, or PATCH request
- `201 Created` - Successful POST request (resource created)
- `204 No Content` - Successful DELETE request
- `400 Bad Request` - Invalid input data
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource already exists
- `500 Internal Server Error` - Server error

## 🔄 Typical User Journey

### 1. User Registration & Authentication
```
POST /api/users/register → POST /api/users/login
```

### 2. Create a Group
```
POST /api/groups
```

### 3. Add Members to Group
```
POST /api/groups/{groupId}/members
```

### 4. Create an Expense
```
POST /api/expenses
```

### 5. View Balances
```
GET /api/settlements/user/{userId}/balances
```

### 6. Settle Up
```
POST /api/settlements/optimize
POST /api/payments
```

## 🛠 Testing with Postman

### Import OpenAPI Specification
1. Open Postman
2. Click **Import**
3. Enter URL: `http://localhost:8081/v3/api-docs` (for each service)
4. Postman will create a collection with all endpoints

### Environment Variables
Create a Postman environment with:
```json
{
  "api_gateway": "http://localhost:8080",
  "jwt_token": "<set-after-login>",
  "user_id": "<set-after-login>"
}
```

## 📊 Monitoring & Health Checks

### Service Health
```bash
GET http://localhost:8080/actuator/health
GET http://localhost:8081/actuator/health  # Individual services
```

### Service Discovery (Eureka)
- **Dashboard**: http://localhost:8761
- View all registered services and their instances

### Distributed Tracing (Zipkin)
- **Dashboard**: http://localhost:9411
- Trace requests across multiple services

## 🚀 Frontend Integration

### Example: React with Axios

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add token to requests
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// Example: Register user
const registerUser = async (userData) => {
  try {
    const response = await api.post('/api/users/register', userData);
    localStorage.setItem('jwt_token', response.data.token);
    return response.data;
  } catch (error) {
    console.error('Registration failed:', error.response.data);
    throw error;
  }
};

// Example: Create group
const createGroup = async (groupData) => {
  try {
    const response = await api.post('/api/groups', groupData);
    return response.data;
  } catch (error) {
    console.error('Group creation failed:', error.response.data);
    throw error;
  }
};
```

### Example: Angular Service

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseURL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('jwt_token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }

  register(userData: any): Observable<any> {
    return this.http.post(
      `${this.baseURL}/api/users/register`,
      userData,
      { headers: this.getHeaders() }
    );
  }

  createGroup(groupData: any): Observable<any> {
    return this.http.post(
      `${this.baseURL}/api/groups`,
      groupData,
      { headers: this.getHeaders() }
    );
  }
}
```

## 📱 Mobile Integration

### Example: React Native with Fetch

```javascript
const API_BASE_URL = 'http://localhost:8080';

class ApiClient {
  constructor() {
    this.token = null;
  }

  setToken(token) {
    this.token = token;
  }

  async request(endpoint, options = {}) {
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`;
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.status}`);
    }

    return response.json();
  }

  async registerUser(userData) {
    return this.request('/api/users/register', {
      method: 'POST',
      body: JSON.stringify(userData),
    });
  }
}

export default new ApiClient();
```

## 🔍 Troubleshooting

### CORS Issues
If you encounter CORS errors:
1. Check that your frontend origin is in the allowed list
2. Verify requests go through API Gateway (port 8080)
3. Ensure `withCredentials: true` in your API client

### Authentication Issues
1. Verify JWT token format: `Bearer <token>`
2. Check token expiration (default: 1 hour)
3. Ensure token is included in Authorization header

### Service Unavailable
1. Check service health: `http://localhost:8080/actuator/health`
2. View Eureka dashboard: `http://localhost:8761`
3. Check Docker container status: `docker compose ps`

## 📞 Support

For issues or questions:
- **GitHub Issues**: https://github.com/Dhruv1030/SplitIt/issues
- **Documentation**: See individual service MD files in each service directory

## 🎯 Next Steps

1. ✅ Access Swagger UI for each service
2. ✅ Test authentication flow
3. ✅ Test complete user journey
4. 🔄 Create Postman collection
5. 🔄 Implement frontend integration
6. 🔄 Set up automated API testing
