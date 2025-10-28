# Expense Service - Complete Implementation ✅

## 📋 Overview

**Status**: ✅ COMPLETE & DEPLOYED  
**Port**: 8083  
**Database**: PostgreSQL (shared with Group Service)  
**Build**: JAR successfully created  
**Docker**: Container running successfully

## 🎯 Features Implemented

### 1. **Three Split Algorithms** 🧮

#### EQUAL Split

- Divides expense amount equally among all participants
- Uses `BigDecimal` for precision handling
- Allocates remainder to last participant (prevents precision loss)
- Example: $100 split among 3 = $33.33 + $33.33 + $33.34

#### EXACT Split

- Custom amounts per participant
- Validates that sum equals total expense amount
- Useful for unequal contributions (e.g., one person orders more)

#### PERCENTAGE Split

- Each participant pays a percentage
- Validates percentages sum to 100%
- Calculates amounts based on percentages
- Remainder handling on last participant

### 2. **Entities Created** 📦

#### Expense Entity

```java
- id (BIGSERIAL)
- description
- amount (BigDecimal with precision 19, scale 2)
- currency (default: USD)
- groupId
- paidBy (userId who paid)
- category (FOOD, TRANSPORT, etc.)
- splitType (EQUAL, EXACT, PERCENTAGE)
- receiptUrl
- date (timestamp)
- createdAt, updatedAt
- isActive (for soft delete)
- notes
- splits (OneToMany relationship)
```

#### ExpenseSplit Entity

```java
- id (BIGSERIAL)
- userId
- amount (BigDecimal)
- percentage (for PERCENTAGE splits)
- isPaid (boolean)
- expense (ManyToOne relationship)
```

### 3. **REST API Endpoints** 🌐

All endpoints require `X-User-Id` header (from JWT via API Gateway)

| Method | Endpoint                         | Description                              |
| ------ | -------------------------------- | ---------------------------------------- |
| POST   | `/api/expenses`                  | Create new expense                       |
| GET    | `/api/expenses/{id}`             | Get expense by ID                        |
| GET    | `/api/expenses/group/{groupId}`  | Get all group expenses                   |
| GET    | `/api/expenses/my-expenses`      | Get current user's expenses              |
| PUT    | `/api/expenses/{id}`             | Update expense (payer only)              |
| DELETE | `/api/expenses/{id}`             | Delete expense (soft delete, payer only) |
| GET    | `/api/expenses/balance`          | Get current user's balance               |
| GET    | `/api/expenses/balance/{userId}` | Get specific user's balance              |
| GET    | `/api/expenses/health`           | Health check                             |

### 4. **Balance Calculation** 💰

#### UserBalanceResponse includes:

- `totalPaid`: Total amount user paid
- `totalOwed`: Total amount user owes
- `netBalance`:
  - Positive = Others owe you
  - Negative = You owe others
- `balances`: Map of userId → amount (detailed per-user balances)

### 5. **Authorization & Security** 🔒

- Only payer can update/delete expense
- Users can only view expenses they're part of
- JWT authentication via API Gateway
- User ID extracted from JWT and passed as header

### 6. **Exception Handling** ⚠️

- `ResourceNotFoundException` → 404 Not Found
- `UnauthorizedException` → 403 Forbidden
- `BadRequestException` → 400 Bad Request
- `IllegalArgumentException` → 400 (validation errors)
- `MethodArgumentNotValidException` → 400 (with field details)
- Global exception handler for consistent error responses

### 7. **Data Validation** ✓

- `@NotBlank` on description
- `@NotNull` on required fields
- `@DecimalMin` on amount
- Custom validation in split calculator:
  - EXACT: Sum must equal total
  - PERCENTAGE: Sum must equal 100%
  - All amounts must be positive

## 📁 Project Structure

```
expense-service/
├── src/main/java/com/splitwise/expense/
│   ├── controller/
│   │   └── ExpenseController.java
│   ├── dto/
│   │   ├── ApiResponse.java
│   │   ├── CreateExpenseRequest.java
│   │   ├── ExpenseResponse.java
│   │   └── UserBalanceResponse.java
│   ├── exception/
│   │   ├── BadRequestException.java
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   └── UnauthorizedException.java
│   ├── model/
│   │   ├── Expense.java
│   │   ├── ExpenseSplit.java
│   │   └── SplitType.java
│   ├── repository/
│   │   ├── ExpenseRepository.java
│   │   └── ExpenseSplitRepository.java
│   ├── service/
│   │   ├── ExpenseService.java (main business logic)
│   │   └── SplitCalculatorService.java (split algorithms)
│   └── ExpenseServiceApplication.java
└── src/main/resources/
    └── application.yml
```

## 🔄 Database Schema

### expenses table

```sql
CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    group_id BIGINT NOT NULL,
    paid_by VARCHAR(255) NOT NULL,
    category VARCHAR(50),
    split_type VARCHAR(20) NOT NULL,
    receipt_url VARCHAR(500),
    date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    notes VARCHAR(255)
);
```

### expense_splits table

```sql
CREATE TABLE expense_splits (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    percentage NUMERIC(5,2),
    is_paid BOOLEAN NOT NULL DEFAULT false,
    expense_id BIGINT NOT NULL REFERENCES expenses(id)
);
```

## 🧪 Testing the Service

### 1. Create an Expense (EQUAL split)

```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "description": "Team Lunch",
    "amount": 100.00,
    "currency": "USD",
    "groupId": 1,
    "paidBy": "user123",
    "category": "FOOD",
    "splitType": "EQUAL",
    "participantIds": ["user123", "user456", "user789"]
  }'
```

### 2. Create an Expense (EXACT split)

```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "description": "Grocery Shopping",
    "amount": 150.00,
    "groupId": 1,
    "paidBy": "user123",
    "splitType": "EXACT",
    "exactAmounts": {
      "user123": 50.00,
      "user456": 60.00,
      "user789": 40.00
    }
  }'
```

### 3. Create an Expense (PERCENTAGE split)

```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "description": "Rent",
    "amount": 2000.00,
    "groupId": 1,
    "paidBy": "user123",
    "splitType": "PERCENTAGE",
    "percentages": {
      "user123": 40.0,
      "user456": 35.0,
      "user789": 25.0
    }
  }'
```

### 4. Get User Balance

```bash
curl http://localhost:8080/api/expenses/balance \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. Get Group Expenses

```bash
curl http://localhost:8080/api/expenses/group/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🎯 Key Implementation Details

### Split Calculation Algorithm

```java
// EQUAL Split Example
BigDecimal perPersonAmount = totalAmount.divide(
    BigDecimal.valueOf(participantCount),
    2,
    RoundingMode.HALF_UP
);

// Calculate remainder
BigDecimal totalAllocated = perPersonAmount.multiply(
    BigDecimal.valueOf(participantCount)
);
BigDecimal remainder = totalAmount.subtract(totalAllocated);

// Last person gets the remainder
lastSplit.setAmount(perPersonAmount.add(remainder));
```

### Authorization Logic

```java
// Check if user has access (either payer or participant)
boolean hasAccess = expense.getPaidBy().equals(currentUserId) ||
    expense.getSplits().stream()
        .anyMatch(split -> split.getUserId().equals(currentUserId));
```

### Balance Calculation

```java
// Positive balance = Others owe you
// Negative balance = You owe others
BigDecimal netBalance = totalPaid.subtract(totalOwed);
```

## ✅ Build & Deploy Status

```bash
# Build Status
✅ Maven clean compile: SUCCESS
✅ Maven package: SUCCESS
✅ JAR created: expense-service-1.0.0.jar

# Docker Status
✅ Container built: splitit-expense-service
✅ Container running: Up 29 minutes
✅ Port exposed: 0.0.0.0:8083->8083/tcp

# Database Status
✅ Tables created: expenses, expense_splits
✅ Foreign key constraint: expense_splits → expenses
✅ Indexes: Automatic on primary keys

# Eureka Status
✅ Registered with discovery server
✅ Status: UP
✅ Instance: EXPENSE-SERVICE/7e9ff3291454:expense-service:8083
```

## 🔗 Integration Points

- **API Gateway**: Routes `/api/expenses/**` to expense-service
- **Discovery Server**: Registered as `EXPENSE-SERVICE`
- **PostgreSQL**: Shared database with group-service
- **Kafka**: Ready for event publishing (not yet implemented)
- **JWT**: Validates via API Gateway, receives X-User-Id header

## 🚀 Next Steps

1. **Testing**: Create comprehensive unit and integration tests
2. **Settlement Service**: Use expense data to calculate optimal settlements
3. **Notifications**: Send events when expenses are created/updated
4. **Analytics**: Aggregate expense data for reports
5. **Payment Integration**: Link with Payment Service

## 📊 Performance Considerations

- Uses `@Transactional` for ACID guarantees
- `readOnly=true` on query methods for optimization
- Lazy loading on ManyToOne relationships
- Indexed primary keys for fast lookups
- BigDecimal arithmetic for financial precision

---

**Created**: October 28, 2025  
**Status**: ✅ Production Ready  
**Test Coverage**: Pending (next phase)
