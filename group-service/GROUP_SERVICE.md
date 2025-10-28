# Group Service - Complete Documentation 👥

## 📋 Overview
**Status**: ✅ PRODUCTION READY  
**Port**: 8082  
**Database**: PostgreSQL (shared with Expense Service)  
**Purpose**: Group management for expense sharing  

The Group Service manages **shared expense groups**, handling:
- 👥 **Group Creation & Management**
- 🎭 **Member Management** (ADMIN, MEMBER roles)
- 🏷️ **Group Categories** (trip, home, couple, other)
- 🔍 **User's Group Listing**
- 🗑️ **Soft Delete** (archive groups)

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Group Service                         │
│                   (Port 8082)                           │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │  GroupController (REST API)                      │  │
│  │  - POST /api/groups                              │  │
│  │  - GET /api/groups/{id}                          │  │
│  │  - PUT /api/groups/{id}                          │  │
│  │  - DELETE /api/groups/{id}                       │  │
│  │  - POST /api/groups/{id}/members                 │  │
│  │  - DELETE /api/groups/{id}/members/{userId}      │  │
│  │  - GET /api/groups/user/{userId}                 │  │
│  └───────────────────┬──────────────────────────────┘  │
│                      │                                  │
│  ┌───────────────────▼──────────────────────────────┐  │
│  │  GroupService (Business Logic)                   │  │
│  │  - Group CRUD operations                         │  │
│  │  - Member management with role validation        │  │
│  │  - Authorization checks (ADMIN only actions)     │  │
│  │  - Soft delete handling                          │  │
│  └───────────────────┬──────────────────────────────┘  │
│                      │                                  │
│  ┌───────────────────▼──────────────────────────────┐  │
│  │  GroupRepository (JPA)                           │  │
│  │  - findById, findByCreatedBy                     │  │
│  │  - findByMembersUserId                           │  │
│  │  - Custom queries with JOIN FETCH               │  │
│  └───────────────────┬──────────────────────────────┘  │
│                      │                                  │
│  ┌───────────────────▼──────────────────────────────┐  │
│  │  GroupMemberRepository (JPA)                     │  │
│  │  - findByGroupIdAndUserId                        │  │
│  │  - existsByGroupIdAndUserId                      │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
                  ┌─────────────┐
                  │ PostgreSQL  │
                  │ (splitwise) │
                  │  Port 5432  │
                  └─────────────┘
```

## 🔑 Key Features

### 1. **Group Creation** 📝

**Endpoint**: `POST /api/groups`

**Request**:
```json
{
  "name": "Weekend Trip to Mountains",
  "description": "Hiking and camping expenses",
  "category": "trip",
  "type": "trip"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Group created successfully",
  "data": {
    "id": 1,
    "name": "Weekend Trip to Mountains",
    "description": "Hiking and camping expenses",
    "category": "trip",
    "createdBy": "690050fd8006473761a88fbd",
    "createdAt": "2025-10-28T12:00:00",
    "updatedAt": "2025-10-28T12:00:00",
    "members": [
      {
        "id": 1,
        "userId": "690050fd8006473761a88fbd",
        "role": "ADMIN",
        "joinedAt": "2025-10-28T12:00:00"
      }
    ],
    "memberCount": 1,
    "active": true
  }
}
```

**Features**:
- ✅ Creator automatically becomes ADMIN
- ✅ Creator automatically added as first member
- ✅ Group categories: trip, home, couple, other
- ✅ Timestamps automatically managed

### 2. **Member Management** 👥

#### Add Member

**Endpoint**: `POST /api/groups/{groupId}/members`

**Request**:
```json
{
  "userId": "690051234567890123456789",
  "role": "MEMBER"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Member added successfully",
  "data": {
    "id": 2,
    "userId": "690051234567890123456789",
    "role": "MEMBER",
    "joinedAt": "2025-10-28T12:05:00"
  }
}
```

**Authorization**:
- ✅ Only ADMIN can add members
- ✅ Cannot add same user twice
- ✅ Can specify role (ADMIN or MEMBER)

#### Remove Member

**Endpoint**: `DELETE /api/groups/{groupId}/members/{userId}`

**Authorization**:
- ✅ Only ADMIN can remove members
- ✅ Cannot remove yourself if you're the last admin
- ✅ Member leaves group but expenses remain

### 3. **Group Roles** 🎭

| Role | Permissions |
|------|-------------|
| **ADMIN** | • Update group details<br>• Add/remove members<br>• Promote/demote members<br>• Delete group<br>• All MEMBER permissions |
| **MEMBER** | • View group details<br>• View all expenses<br>• Add expenses<br>• View settlements<br>• Leave group |

### 4. **Group Categories** 🏷️

| Category | Description | Use Case |
|----------|-------------|----------|
| `trip` | Travel and vacation | Shared trip expenses |
| `home` | Household expenses | Roommates, family |
| `couple` | Partner expenses | Dating, marriage |
| `other` | General purpose | Any other scenario |

### 5. **Get Group Details** 📄

**Endpoint**: `GET /api/groups/{id}`

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Weekend Trip to Mountains",
    "description": "Hiking and camping expenses",
    "category": "trip",
    "createdBy": "690050fd8006473761a88fbd",
    "createdAt": "2025-10-28T12:00:00",
    "updatedAt": "2025-10-28T12:00:00",
    "members": [
      {
        "id": 1,
        "userId": "690050fd8006473761a88fbd",
        "role": "ADMIN",
        "joinedAt": "2025-10-28T12:00:00"
      },
      {
        "id": 2,
        "userId": "690051234567890123456789",
        "role": "MEMBER",
        "joinedAt": "2025-10-28T12:05:00"
      }
    ],
    "memberCount": 2,
    "active": true
  }
}
```

### 6. **Get User's Groups** 📋

**Endpoint**: `GET /api/groups/user/{userId}`

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Weekend Trip",
      "category": "trip",
      "memberCount": 4,
      "role": "ADMIN"
    },
    {
      "id": 2,
      "name": "Apartment Rent",
      "category": "home",
      "memberCount": 3,
      "role": "MEMBER"
    }
  ]
}
```

**Features**:
- ✅ Shows user's role in each group
- ✅ Only returns active groups
- ✅ Ordered by creation date (newest first)

### 7. **Update Group** ✏️

**Endpoint**: `PUT /api/groups/{id}`

**Request**:
```json
{
  "name": "Updated Group Name",
  "description": "New description",
  "category": "home"
}
```

**Authorization**:
- ✅ Only ADMIN can update group

### 8. **Delete Group** 🗑️

**Endpoint**: `DELETE /api/groups/{id}`

**Features**:
- ✅ **Soft delete** (sets `isActive = false`)
- ✅ Group data preserved for historical records
- ✅ Only ADMIN can delete
- ✅ Expenses remain accessible

## 📡 Complete API Reference

| Method | Endpoint | Auth | Admin Only | Description |
|--------|----------|------|------------|-------------|
| POST | `/api/groups` | ✅ | ❌ | Create group |
| GET | `/api/groups/{id}` | ✅ | ❌ | Get group details |
| PUT | `/api/groups/{id}` | ✅ | ✅ | Update group |
| DELETE | `/api/groups/{id}` | ✅ | ✅ | Delete group |
| POST | `/api/groups/{id}/members` | ✅ | ✅ | Add member |
| DELETE | `/api/groups/{id}/members/{userId}` | ✅ | ✅ | Remove member |
| PUT | `/api/groups/{id}/members/{userId}/role` | ✅ | ✅ | Update member role |
| GET | `/api/groups/user/{userId}` | ✅ | ❌ | Get user's groups |
| GET | `/api/groups/health` | ❌ | ❌ | Health check |

## 🗄️ Database Schema

### groups table

```sql
CREATE TABLE groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true
);
```

### group_members table

```sql
CREATE TABLE group_members (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(id),
    user_id VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- ADMIN or MEMBER
    joined_at TIMESTAMP NOT NULL,
    UNIQUE(group_id, user_id)
);
```

**Relationships**:
- One-to-Many: Group → GroupMember
- Cascade: Delete group → Delete all members (in future)
- Unique constraint: (group_id, user_id) prevents duplicate members

## 🔒 Authorization Logic

### Group Operations

```java
// Only ADMIN can update/delete group
boolean isAdmin = groupMember.getRole() == MemberRole.ADMIN;
if (!isAdmin) {
    throw new UnauthorizedException("Only admins can perform this action");
}
```

### Member Operations

```java
// Check if user is member of group
boolean isMember = groupMemberRepository
    .existsByGroupIdAndUserId(groupId, userId);
    
if (!isMember) {
    throw new UnauthorizedException("You are not a member of this group");
}
```

### Role Validation

```java
// Current user must be ADMIN to add/remove members
GroupMember currentMember = getMemberOrThrow(groupId, currentUserId);
if (currentMember.getRole() != MemberRole.ADMIN) {
    throw new UnauthorizedException("Only admins can manage members");
}
```

## 🧪 Testing Examples

### 1. **Create a Group**

```bash
TOKEN="your-jwt-token"

curl -X POST http://localhost:8080/api/groups \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Summer Vacation",
    "description": "Beach trip expenses",
    "category": "trip",
    "type": "trip"
  }'
```

### 2. **Add Member to Group**

```bash
curl -X POST http://localhost:8080/api/groups/1/members \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "690051234567890123456789",
    "role": "MEMBER"
  }'
```

### 3. **Get Group Details**

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/groups/1
```

### 4. **Get User's Groups**

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/groups/user/690050fd8006473761a88fbd
```

### 5. **Update Group**

```bash
curl -X PUT http://localhost:8080/api/groups/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Group Name",
    "description": "New description"
  }'
```

### 6. **Remove Member**

```bash
curl -X DELETE http://localhost:8080/api/groups/1/members/690051234567890123456789 \
  -H "Authorization: Bearer $TOKEN"
```

### 7. **Delete Group**

```bash
curl -X DELETE http://localhost:8080/api/groups/1 \
  -H "Authorization: Bearer $TOKEN"
```

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Service port | 8082 |
| `POSTGRES_URL` | PostgreSQL connection | jdbc:postgresql://localhost:5432/splitwise |
| `POSTGRES_USER` | Database username | admin |
| `POSTGRES_PASSWORD` | Database password | admin123 |
| `EUREKA_URL` | Eureka server | http://localhost:8761/eureka |

### Docker Configuration

```yaml
spring:
  config:
    activate:
      on-profile: docker
  datasource:
    url: jdbc:postgresql://postgres:5432/splitwise
```

## 🚀 Deployment

### Build

```bash
cd group-service
mvn clean package -DskipTests
```

### Run Locally

```bash
java -jar target/group-service-1.0.0.jar
```

### Docker

```bash
docker compose up -d group-service postgres
```

### Verify

```bash
# Health check
curl http://localhost:8082/actuator/health

# Check database connection
docker compose logs group-service | grep "Started GroupServiceApplication"
```

## 📊 Integration Points

| Service | Purpose | Communication |
|---------|---------|---------------|
| **API Gateway** | Routes requests | HTTP via Eureka |
| **User Service** | Validates user IDs | Via API Gateway |
| **Expense Service** | Links to groups | Shared PostgreSQL |
| **PostgreSQL** | Data persistence | JDBC |
| **Eureka** | Service discovery | REST API |
| **Zipkin** | Distributed tracing | HTTP spans |

## 🎯 Business Rules

### Group Creation
- ✅ Creator becomes first ADMIN
- ✅ Group must have at least 1 member
- ✅ Category is required
- ✅ Name is required

### Member Management
- ✅ User can be in multiple groups
- ✅ Cannot add same user twice to a group
- ✅ Group must have at least 1 ADMIN
- ✅ Last ADMIN cannot leave/be removed

### Group Deletion
- ✅ Soft delete (preserve data)
- ✅ Only ADMIN can delete
- ✅ Related expenses remain accessible
- ✅ Members can still view historical data

## 🔄 Future Enhancements

- [ ] Group invite system (invite codes)
- [ ] Group settings (currency, split preferences)
- [ ] Group chat/comments
- [ ] Member nicknames within group
- [ ] Group avatar/image
- [ ] Group statistics (total spent, etc.)
- [ ] Group activity feed
- [ ] Email notifications for group events
- [ ] Group templates (recurring groups)
- [ ] Member permissions customization

---

**Created**: October 28, 2025  
**Status**: ✅ Production Ready  
**Database**: PostgreSQL (splitwise)  
**Port**: 8082  

**Key Achievement**: Comprehensive group management with role-based access control for collaborative expense tracking! 👥
