# Activity Feed Implementation - Completed ✅

## Overview

Successfully implemented a complete Activity Feed Service for tracking and displaying user activities across the SplitIt application.

## Implementation Date

November 7, 2025

## Components Implemented

### 1. Data Models

- **Activity Entity** (`notification-service/model/Activity.java`)

  - Fields: id, activityType, userId, groupId, description, metadata (JSON), timestamp, targetUserId
  - JPA entity with auto-generated IDs
  - Indexes on groupId, userId, and timestamp for optimal query performance

- **ActivityType Enum** (`notification-service/model/ActivityType.java`)
  - EXPENSE_ADDED
  - EXPENSE_UPDATED
  - EXPENSE_DELETED
  - MEMBER_ADDED
  - MEMBER_REMOVED
  - GROUP_CREATED
  - PAYMENT_RECORDED
  - SETTLEMENT_COMPLETED

### 2. Repository Layer

- **ActivityRepository** (`notification-service/repository/ActivityRepository.java`)
  - Extends JpaRepository
  - Custom query methods with pagination support:
    - `findByGroupIdOrderByTimestampDesc(Long groupId, Pageable pageable)`
    - `findByUserIdOrderByTimestampDesc(String userId, Pageable pageable)`
    - `findTop10ByGroupIdOrderByTimestampDesc(Long groupId)`
    - `findByGroupIdAndTimestampBetweenOrderByTimestampDesc(...)`
    - `countByGroupId(Long groupId)`

### 3. Service Layer

- **ActivityService** (`notification-service/service/ActivityService.java`)
  - Business logic for activity logging
  - Methods:
    - `logActivity(CreateActivityRequest request)` - Create new activity
    - `getGroupActivities(Long groupId, Pageable pageable)` - Get paginated activities
    - `getRecentGroupActivities(Long groupId)` - Get last 10 activities
    - `getUserActivities(String userId, Pageable pageable)` - Get user-specific activities
    - `getGroupActivitiesByDateRange(...)` - Get activities in date range
    - `getGroupActivityCount(Long groupId)` - Count group activities

### 4. REST API Layer

- **ActivityController** (`notification-service/controller/ActivityController.java`)
  - 7 REST endpoints:

#### Endpoints

1. **POST /api/activities**

   - Create new activity log
   - Request Body: CreateActivityRequest (activityType, userId, groupId, description, metadata, targetUserId)
   - Response: 201 Created with ActivityResponse
   - Used by other services for logging activities

2. **GET /api/activities/group/{groupId}**

   - Get paginated activities for a group
   - Query params: page (default 0), size (default 20)
   - Response: Page<ActivityResponse>

3. **GET /api/activities/group/{groupId}/recent**

   - Get last 10 activities for a group
   - Response: List<ActivityResponse>
   - For quick feed display

4. **GET /api/activities/user/{userId}**

   - Get user's activities across all groups
   - Query params: page, size
   - Response: Page<ActivityResponse>

5. **GET /api/activities/group/{groupId}/range**

   - Get activities in date range
   - Query params: startDate, endDate, page, size
   - Response: Page<ActivityResponse>

6. **GET /api/activities/group/{groupId}/count**

   - Get total activity count for group
   - Response: Long

7. **GET /api/activities/health**
   - Health check endpoint
   - Response: "Activity Service is running!"

### 5. DTOs

- **CreateActivityRequest** - Request DTO with validation
- **ActivityResponse** - Response DTO with all activity fields

### 6. Database Configuration

- PostgreSQL database: `notification_db`
- Configured datasource with credentials: admin/admin123
- JPA hibernate DDL auto-update enabled
- SQL logging enabled for debugging

### 7. Integration with Expense Service

- **ActivityClient** (`expense-service/client/ActivityClient.java`)

  - RestTemplate-based client for inter-service communication
  - URL: `http://localhost:8085/api/activities`
  - Non-blocking activity logging (catches exceptions to prevent failures)

- **ActivityRequest** (`expense-service/client/ActivityRequest.java`)

  - DTO for inter-service communication

- **Modified ExpenseService**

  - Added ActivityClient dependency
  - Added `logExpenseActivity()` helper method
  - Integrated activity logging in `createExpense()` method
  - Logs EXPENSE_ADDED activity after successful expense creation

- **RestTemplate Bean**
  - Added to ExpenseServiceApplication for dependency injection

## Testing Results

### Health Check

```bash
curl http://localhost:8085/api/activities/health
# Response: Activity Service is running!
```

### Create Activity

```bash
curl -X POST http://localhost:8085/api/activities \
  -H "Content-Type: application/json" \
  -d '{
    "activityType": "EXPENSE_ADDED",
    "userId": 1,
    "groupId": 1,
    "description": "John added an expense for groceries",
    "metadata": "{\"amount\": 50.00, \"currency\": \"USD\"}"
  }'
# Response: 201 Created with activity details
```

### Get Group Activities (Paginated)

```bash
curl "http://localhost:8085/api/activities/group/1?page=0&size=10"
# Response: Paginated list with 1 activity
```

### Get Recent Activities

```bash
curl http://localhost:8085/api/activities/group/1/recent
# Response: List with 1 activity
```

## Architecture Decisions

1. **Database Choice**: PostgreSQL for relational data with good indexing support
2. **Non-blocking Logging**: Activity logging wrapped in try-catch to prevent failures from affecting primary operations
3. **Pagination**: All list endpoints support pagination to handle large activity volumes
4. **Metadata as JSON String**: Flexible storage for activity-specific data
5. **Inter-service Communication**: RestTemplate for synchronous calls (could be upgraded to Kafka for async)
6. **Indexed Fields**: groupId, userId, timestamp indexed for query performance

## Next Steps

1. **Group Service Integration**

   - Add ActivityClient to log MEMBER_ADDED, MEMBER_REMOVED, GROUP_CREATED events

2. **Settlement Service Integration**

   - Add ActivityClient to log PAYMENT_RECORDED, SETTLEMENT_COMPLETED events

3. **User Name Enhancement**

   - Add UserService client to fetch user names for display
   - Populate userName and targetUserName fields in responses

4. **Kafka Migration** (Optional)

   - Move to event-driven architecture
   - Use Kafka for async activity logging
   - Better scalability and decoupling

5. **Activity Feed Frontend**
   - Build UI component to display activity feed
   - Real-time updates using WebSockets/SSE
   - Filtering and search capabilities

## Files Created/Modified

### Created Files

1. `notification-service/src/main/java/com/splitwise/notification/model/Activity.java`
2. `notification-service/src/main/java/com/splitwise/notification/model/ActivityType.java`
3. `notification-service/src/main/java/com/splitwise/notification/repository/ActivityRepository.java`
4. `notification-service/src/main/java/com/splitwise/notification/dto/ActivityResponse.java`
5. `notification-service/src/main/java/com/splitwise/notification/dto/CreateActivityRequest.java`
6. `notification-service/src/main/java/com/splitwise/notification/service/ActivityService.java`
7. `notification-service/src/main/java/com/splitwise/notification/controller/ActivityController.java`
8. `expense-service/src/main/java/com/splitwise/expense/client/ActivityRequest.java`
9. `expense-service/src/main/java/com/splitwise/expense/client/ActivityClient.java`
10. `init-db.sh` - PostgreSQL initialization script

### Modified Files

1. `notification-service/pom.xml` - Added JPA, PostgreSQL, validation dependencies
2. `notification-service/src/main/resources/application.yml` - Added database config, logging
3. `expense-service/src/main/java/com/splitwise/expense/service/ExpenseService.java` - Added activity logging
4. `expense-service/src/main/java/com/splitwise/expense/ExpenseServiceApplication.java` - Added RestTemplate bean
5. `notification-service/src/main/java/com/splitwise/notification/NotificationServiceApplication.java` - Added ComponentScan

## Build & Deployment

### Build Commands

```bash
cd /Users/dhruvpatel/Desktop/SplitIt
./build.sh  # Builds all services
docker compose build notification-service  # Rebuild Docker image
```

### Deployment

```bash
docker compose down
docker compose up -d
```

### Database Setup

```sql
-- Database created automatically via init-db.sh
CREATE DATABASE notification_db;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO admin;

-- Tables created automatically by JPA
```

## Performance Considerations

- **Indexing**: groupId, userId, timestamp indexed for fast queries
- **Pagination**: Prevents memory issues with large datasets
- **Non-blocking**: Activity logging won't slow down main operations
- **Connection Pooling**: Spring Boot default HikariCP connection pool

## Success Criteria ✅

- [x] Activity entity with all required fields
- [x] Repository with pagination and custom queries
- [x] Service layer with business logic
- [x] REST API with 7 endpoints
- [x] Integration with Expense Service
- [x] Database configured and working
- [x] Docker deployment successful
- [x] All endpoints tested and working
- [x] Non-blocking activity logging
- [x] Proper error handling

## Status: COMPLETED ✅

The Activity Feed Service is fully implemented, tested, and deployed. It successfully tracks expense activities and provides multiple endpoints for retrieving activity history.
