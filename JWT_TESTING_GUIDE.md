# JWT Authentication Testing Guide

## Overview

JWT (JSON Web Token) authentication has been successfully implemented in the SplitIt microservices platform. The API Gateway validates all incoming requests (except public endpoints) and forwards authenticated requests to downstream services with user context.

## Architecture

### Token Flow

```
Client → API Gateway (validates JWT) → Downstream Service (with X-User-Id header)
```

### Components

1. **user-service/JwtTokenProvider**: Generates and validates JWT tokens using HS512 algorithm
2. **api-gateway/JwtAuthenticationFilter**: Intercepts all requests, validates tokens, injects user headers
3. **Shared JWT Secret**: Configured via `JWT_SECRET` environment variable

## Public Endpoints (No Authentication Required)

- `POST /api/users/register` - Register new user
- `POST /api/users/login` - Login and get JWT token
- `/eureka/**` - Eureka dashboard

## Protected Endpoints (JWT Required)

All other endpoints require `Authorization: Bearer <token>` header:

- `GET /api/users/{id}` - Get user profile
- `PUT /api/users/{id}` - Update user profile
- `POST /api/users/{id}/friends` - Add friend
- `GET /api/users/{id}/friends` - List friends
- `GET /api/users/search` - Search users
- All group, expense, settlement, payment, analytics endpoints

## Testing JWT Authentication

### 1. Register a New User

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Smith",
    "email": "alice@example.com",
    "password": "securepass123"
  }'
```

**Expected Response:**

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWI...(long JWT token)",
  "type": "Bearer",
  "user": {
    "id": "690050fd8006473761a88fbd",
    "name": "Alice Smith",
    "email": "alice@example.com",
    ...
  }
}
```

**Save the token for subsequent requests!**

### 2. Login Existing User

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "securepass123"
  }'
```

### 3. Test Protected Endpoint WITHOUT Token (Should Fail)

```bash
curl -v http://localhost:8080/api/users/{userId}
```

**Expected Response:**

```
HTTP/1.1 401 Unauthorized
```

### 4. Test Protected Endpoint WITH Valid Token (Should Succeed)

```bash
TOKEN="your-jwt-token-here"

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users/{userId}
```

**Expected Response:**

```json
{
  "id": "690050fd8006473761a88fbd",
  "name": "Alice Smith",
  "email": "alice@example.com",
  ...
}
```

### 5. Test with Invalid Token (Should Fail)

```bash
curl -v -H "Authorization: Bearer invalid.token.here" \
  http://localhost:8080/api/users/{userId}
```

**Expected Response:**

```
HTTP/1.1 401 Unauthorized
```

### 6. Update User Profile (Authenticated)

```bash
TOKEN="your-jwt-token-here"

curl -X PUT http://localhost:8080/api/users/{userId} \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Updated",
    "phone": "+1234567890"
  }'
```

## JWT Token Details

### Algorithm

- **HS512** (HMAC with SHA-512)

### Claims

- `sub` (subject): User ID
- `email`: User email address
- `roles`: Comma-separated roles (e.g., "ROLE_USER")
- `iat` (issued at): Token creation timestamp
- `exp` (expiration): Token expiry timestamp (1 hour from creation)

### Token Lifetime

- **Default**: 1 hour (3600000 ms)
- Configurable via `jwt.expiration-ms` property

## Debugging

### Check API Gateway Logs

```bash
docker logs api-gateway -f
```

Look for:

- Token validation success/failure
- User context injection (X-User-Id, X-User-Email, X-User-Roles)

### Check User Service Logs

```bash
docker logs user-service -f
```

Look for:

- Token generation during login/register
- JWT signing operations

### Decode JWT Token (for debugging)

Visit https://jwt.io and paste your token to inspect claims (don't use sensitive tokens on public sites in production!)

## Security Considerations

### Current Implementation

✅ Tokens signed with HS512 algorithm  
✅ Secret key configurable via environment variable  
✅ Token expiration enforced (1 hour)  
✅ Invalid/expired tokens rejected with 401  
✅ Public endpoints whitelisted

### Production Recommendations

🔐 Use strong JWT_SECRET (min 256 bits for HS512)  
🔐 Store JWT_SECRET in secure vault (AWS Secrets Manager, HashiCorp Vault)  
🔐 Consider using RS256 (asymmetric) for better key management  
🔐 Implement token refresh mechanism  
🔐 Add token revocation/blacklist for logout  
🔐 Enable HTTPS in production  
🔐 Add rate limiting on login endpoint  
🔐 Implement audit logging for authentication events

## Environment Variables

### docker-compose.yml

```yaml
JWT_SECRET: ${JWT_SECRET:-splitwise-jwt-secret-key-please-change-in-production-min-256-bits-required-for-hs512}
```

### Override for Production

Create `.env` file:

```env
JWT_SECRET=your-super-secure-256-bit-secret-key-here
```

Or set environment variable:

```bash
export JWT_SECRET="your-super-secure-256-bit-secret-key-here"
docker compose up -d
```

## Common Issues

### Issue: "401 Unauthorized" on public endpoints

**Solution**: Check `JwtAuthenticationFilter.PUBLIC_PATHS` - ensure your endpoint is whitelisted

### Issue: Token always returns "dummy-jwt-token"

**Solution**: Rebuild Docker images with `docker compose up --build -d`

### Issue: "Invalid JWT signature"

**Solution**: Ensure both user-service and api-gateway use the same JWT_SECRET

### Issue: Token expired

**Solution**: Login again to get a fresh token. Tokens expire after 1 hour.

## Next Steps

1. **Implement Refresh Tokens**: Allow clients to refresh expired access tokens without re-login
2. **Add Token Revocation**: Store invalidated tokens in Redis for logout functionality
3. **Role-Based Access Control (RBAC)**: Check user roles for fine-grained authorization
4. **Audit Logging**: Log all authentication attempts and token usage
5. **Rate Limiting**: Prevent brute force attacks on login endpoint

## Testing Checklist

- [x] Register user returns valid JWT
- [x] Login returns valid JWT
- [x] Protected endpoint rejects request without token (401)
- [x] Protected endpoint accepts valid token (200)
- [x] Protected endpoint rejects invalid token (401)
- [x] Token contains correct claims (userId, email, roles)
- [x] Token expiration is enforced
- [x] Public endpoints accessible without token
- [ ] Refresh token flow (TODO)
- [ ] Token revocation on logout (TODO)
- [ ] Role-based authorization (TODO)

## Support

For issues or questions:

- Check logs: `docker logs api-gateway` or `docker logs user-service`
- Review JWT implementation in `user-service/security/JwtTokenProvider.java`
- Review filter logic in `api-gateway/security/JwtAuthenticationFilter.java`
- Open an issue on GitHub: https://github.com/Dhruv1030/SplitIt/issues
