# Settlement Service - Complete Documentation 💸

## 📋 Overview

**Status**: ✅ PRODUCTION READY  
**Port**: 8084  
**Database**: PostgreSQL (shared with Expense and Group Services)  
**Purpose**: Debt simplification and settlement tracking

The Settlement Service handles **debt optimization** and **settlement recording**, providing:

- 🧮 **Debt Simplification Algorithm** (Minimizes transactions)
- 💡 **Smart Settlement Suggestions**
- 📝 **Settlement Recording & Tracking**
- ✅ **Payment Confirmation**
- 📊 **Settlement History**

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   Settlement Service                        │
│                     (Port 8084)                             │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  SettlementController (REST API)                     │  │
│  │  - GET /api/settlements/group/{groupId}/suggestions │  │
│  │  - POST /api/settlements                             │  │
│  │  - GET /api/settlements/group/{groupId}              │  │
│  │  - GET /api/settlements/my-settlements               │  │
│  │  - PUT /api/settlements/{id}/complete                │  │
│  └───────────────────┬──────────────────────────────────┘  │
│                      │                                      │
│  ┌───────────────────▼──────────────────────────────────┐  │
│  │  SettlementService (Business Logic)                  │  │
│  │  - Debt simplification algorithm                     │  │
│  │  - Fetch balances from Expense Service               │  │
│  │  - Fetch user names from User Service                │  │
│  │  - Settlement recording & tracking                   │  │
│  └───────────────────┬──────────────────────────────────┘  │
│                      │                                      │
│  ┌───────────────────▼──────────────────────────────────┐  │
│  │  SettlementRepository (JPA)                          │  │
│  │  - findByGroupId                                     │  │
│  │  - findByPayerId / findByPayeeId                     │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
   ┌────▼────┐     ┌────▼─────┐    ┌────▼────┐
   │Expense  │     │   User   │    │  Postgre│
   │Service  │     │  Service │    │   SQL   │
   │(8083)   │     │  (8081)  │    │  (5432) │
   └─────────┘     └──────────┘    └─────────┘
```

## 🧮 Debt Simplification Algorithm

### The Problem

When multiple users share expenses, the naive approach creates **N\*(N-1)** potential transactions. For a group of 5 people, that's 20 possible transactions!

### Our Solution: Greedy Algorithm

**Time Complexity**: O(n log n)  
**Space Complexity**: O(n)  
**Result**: Minimizes transactions to at most **(N-1)**

### Algorithm Steps:

```
1. Calculate net balance for each user in the group
   - Positive balance = Others owe this user
   - Negative balance = This user owes others

2. Separate users into two groups:
   - Creditors (positive balance, owed money)
   - Debtors (negative balance, owe money)

3. Sort both lists in descending order

4. Match largest debtor with largest creditor:
   - Transaction amount = MIN(debtor_amount, creditor_amount)
   - Update both balances
   - If balance becomes zero, move to next person

5. Repeat until all debts are settled
```

### Example:

**Input Balances:**

- Alice: +$150 (others owe her)
- Bob: -$50 (he owes)
- Charlie: +$30 (others owe him)
- David: -$80 (he owes)
- Eve: -$50 (she owes)

**Naive Approach:** 10-20 transactions

**Our Algorithm Output:** 3 transactions

1. David pays Alice $80
2. Alice pays Charlie $30 (from what she received)
3. Bob pays Alice $50
4. Eve pays Alice $50

**Result:** Only 4 optimized transactions!

## 🔑 Key Features

### 1. **Get Settlement Suggestions** 💡

Calculate the minimum number of transactions needed to settle all group debts.

**Endpoint**: `GET /api/settlements/group/{groupId}/suggestions`

**Response**:

```json
{
  "groupId": 1,
  "suggestions": [
    {
      "payerId": "user456",
      "payerName": "Bob Smith",
      "payeeId": "user123",
      "payeeName": "Alice Johnson",
      "amount": 75.5,
      "currency": "USD",
      "description": "Bob Smith pays Alice Johnson USD 75.50"
    },
    {
      "payerId": "user789",
      "payerName": "Charlie Brown",
      "payeeId": "user123",
      "payeeName": "Alice Johnson",
      "amount": 24.5,
      "currency": "USD",
      "description": "Charlie Brown pays Alice Johnson USD 24.50"
    }
  ],
  "totalTransactions": 2,
  "totalAmount": 100.0,
  "currency": "USD",
  "message": "2 transaction(s) needed to settle all debts"
}
```

**Features**:

- ✅ Minimizes number of transactions
- ✅ Includes user names for readability
- ✅ Clear descriptions
- ✅ Summary statistics

---

### 2. **Record Settlement** 📝

Record when a user actually makes a payment.

**Endpoint**: `POST /api/settlements`

**Headers**:

```
X-User-Id: user123
```

**Request**:

```json
{
  "groupId": 1,
  "payerId": "user456",
  "payeeId": "user123",
  "amount": 75.5,
  "currency": "USD",
  "paymentMethod": "UPI",
  "transactionId": "TXN123456789",
  "notes": "Paid via Google Pay"
}
```

**Response**:

```json
{
  "id": 101,
  "groupId": 1,
  "payerId": "user456",
  "payeeId": "user123",
  "amount": 75.5,
  "currency": "USD",
  "status": "COMPLETED",
  "paymentMethod": "UPI",
  "transactionId": "TXN123456789",
  "notes": "Paid via Google Pay",
  "settledAt": "2025-10-29T15:30:00",
  "createdAt": "2025-10-29T15:30:00"
}
```

**Payment Methods**:

- `CASH` - Cash payment
- `UPI` - UPI payment (India)
- `BANK_TRANSFER` - Bank transfer
- `CREDIT_CARD` - Credit card
- `PAYPAL` - PayPal
- `VENMO` - Venmo

**Validations**:

- ✅ Only the payer can record the settlement
- ✅ Amount must be positive
- ✅ Group must exist

---

### 3. **Get Group Settlements** 📊

Retrieve all settlements for a group.

**Endpoint**: `GET /api/settlements/group/{groupId}`

**Response**:

```json
[
  {
    "id": 101,
    "groupId": 1,
    "payerId": "user456",
    "payeeId": "user123",
    "amount": 75.5,
    "currency": "USD",
    "status": "COMPLETED",
    "paymentMethod": "UPI",
    "settledAt": "2025-10-29T15:30:00",
    "createdAt": "2025-10-29T15:30:00"
  },
  {
    "id": 102,
    "groupId": 1,
    "payerId": "user789",
    "payeeId": "user123",
    "amount": 24.5,
    "currency": "USD",
    "status": "PENDING",
    "paymentMethod": null,
    "settledAt": null,
    "createdAt": "2025-10-29T15:25:00"
  }
]
```

---

### 4. **Get My Settlements** 👤

Retrieve settlements where the current user is involved (as payer or payee).

**Endpoint**: `GET /api/settlements/my-settlements`

**Headers**:

```
X-User-Id: user123
```

**Response**: Array of settlements (sorted by date, newest first)

---

### 5. **Complete Settlement** ✅

Mark a settlement as completed (payee confirmation).

**Endpoint**: `PUT /api/settlements/{id}/complete`

**Headers**:

```
X-User-Id: user123
```

**Response**:

```json
{
  "id": 102,
  "groupId": 1,
  "payerId": "user789",
  "payeeId": "user123",
  "amount": 24.5,
  "currency": "USD",
  "status": "COMPLETED",
  "settledAt": "2025-10-29T15:35:00",
  "createdAt": "2025-10-29T15:25:00"
}
```

**Validation**:

- ✅ Only the payee can confirm completion

---

## 🗄️ Database Schema

### settlements Table

```sql
CREATE TABLE settlements (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL,
    payer_id VARCHAR(255) NOT NULL,    -- User who pays
    payee_id VARCHAR(255) NOT NULL,    -- User who receives
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(20) NOT NULL,        -- PENDING, COMPLETED, CANCELLED
    payment_method VARCHAR(50),
    transaction_id VARCHAR(255),
    notes TEXT,
    settled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_settlement_group ON settlements(group_id);
CREATE INDEX idx_settlement_payer ON settlements(payer_id);
CREATE INDEX idx_settlement_payee ON settlements(payee_id);
CREATE INDEX idx_settlement_status ON settlements(status);
```

## 🔄 Integration with Other Services

### Expense Service Integration

Settlement Service calls Expense Service to get balance data:

**Endpoint Used**: `GET /api/expenses/group/{groupId}/balances`

**Returns**: `Map<userId, netBalance>`

```json
{
  "user123": 100.0,
  "user456": -75.5,
  "user789": -24.5
}
```

### User Service Integration

Settlement Service calls User Service to get user names:

**Endpoint Used**: `GET /api/users/{userId}`

**Returns**: User details including name

## 🧪 Testing Examples

### Test Complete Flow

#### 1. Create expenses in a group

```bash
# Alice pays $150 for dinner (split equally among 3)
curl -X POST http://localhost:8080/api/expenses \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Dinner",
    "amount": 150.00,
    "groupId": 1,
    "splitType": "EQUAL",
    "participants": ["user123", "user456", "user789"]
  }'

# Bob pays $90 for movie tickets
curl -X POST http://localhost:8080/api/expenses \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Movie",
    "amount": 90.00,
    "groupId": 1,
    "splitType": "EQUAL",
    "participants": ["user123", "user456", "user789"]
  }'
```

#### 2. Get settlement suggestions

```bash
curl http://localhost:8080/api/settlements/group/1/suggestions
```

#### 3. Record a settlement

```bash
curl -X POST http://localhost:8080/api/settlements \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": 1,
    "payerId": "user456",
    "payeeId": "user123",
    "amount": 75.50,
    "paymentMethod": "UPI",
    "transactionId": "TXN123456"
  }'
```

## ⚡ Performance Considerations

1. **Caching**: User names are fetched per calculation (consider caching for production)
2. **Database Queries**: Efficient use of indexes on group_id and user_ids
3. **Algorithm**: O(n log n) complexity handles groups of any size efficiently
4. **Service Calls**: Uses WebClient with load balancing

## 🔒 Security

- **Authentication**: JWT validation via API Gateway
- **Authorization**:
  - Only payer can record settlements
  - Only payee can confirm settlements
- **Data Validation**: All amounts validated as positive
- **Input Sanitization**: Spring validation on all requests

## 🚀 Deployment

### Environment Variables

```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/splitwise
SPRING_DATASOURCE_USERNAME: admin
SPRING_DATASOURCE_PASSWORD: admin123
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://discovery-server:8761/eureka/
```

### Docker

```bash
# Build
docker build -t settlement-service .

# Run
docker run -p 8084:8084 settlement-service
```

## 📊 Monitoring

- **Health Check**: `GET /api/settlements/health`
- **Eureka Dashboard**: http://localhost:8761
- **Zipkin Tracing**: http://localhost:9411

## 🎯 Future Enhancements

- [ ] Settlement reminders (send notifications)
- [ ] Recurring settlements (monthly rent, etc.)
- [ ] Multi-currency support
- [ ] Settlement disputes and resolutions
- [ ] Integration with payment gateways (Stripe, PayPal)
- [ ] QR code generation for easy payments
- [ ] Settlement split across multiple payees
- [ ] Analytics dashboard for settlement trends

## 📝 Example Use Cases

### Use Case 1: Weekend Trip

**Scenario**: 4 friends go on a weekend trip

- Alice pays for hotel: $400
- Bob pays for gas: $80
- Charlie pays for groceries: $120
- David pays for activities: $100

**Balance**:

- Alice: +$225 (paid $400, owes $175)
- Bob: -$95 (paid $80, owes $175)
- Charlie: -$55 (paid $120, owes $175)
- David: -$75 (paid $100, owes $175)

**Settlement Suggestions** (2 transactions):

1. Bob pays Alice $95
2. Charlie pays Alice $55
3. David pays Alice $75

### Use Case 2: Roommates

**Scenario**: 3 roommates sharing expenses

- Rent: $1500 (paid by Alice)
- Utilities: $150 (paid by Bob)
- Groceries: $300 (paid by Charlie)

**Balance per person**: $650 each

**Settlement Suggestions** (2 transactions):

1. Bob pays Alice $500
2. Charlie pays Alice $350

## 🤝 Contributing

When extending Settlement Service:

1. Maintain the debt simplification algorithm
2. Add comprehensive tests
3. Update this documentation
4. Follow existing code patterns
5. Consider edge cases (zero balances, single user, etc.)

## 📞 Support

For issues or questions:

- Check logs: `docker compose logs settlement-service`
- Verify Eureka registration: http://localhost:8761
- Test direct endpoint: http://localhost:8084/api/settlements/health

---

**Last Updated**: October 29, 2025  
**Version**: 1.0.0  
**Status**: ✅ Production Ready
