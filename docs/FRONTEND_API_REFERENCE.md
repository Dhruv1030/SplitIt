# 🚀 SplitIt Frontend API Reference Guide

## 📋 Table of Contents

1. [Base URLs & Authentication](#base-urls--authentication)
2. [User Service APIs](#user-service-apis)
3. [Group Service APIs](#group-service-apis)
4. [Expense Service APIs](#expense-service-apis)
5. [Settlement Service APIs](#settlement-service-apis)
6. [Common Response Formats](#common-response-formats)
7. [Error Handling](#error-handling)
8. [Integration Flow](#integration-flow)
9. [⚠️ Known Issues & Backend Fixes Needed](#️-known-issues--backend-fixes-needed)

---

## 🌐 Base URLs & Authentication

### Base URL

All API requests should be made through the **API Gateway**:

```
Base URL: http://localhost:8080
```

### Authentication

Most endpoints require JWT authentication. Include the token in the `Authorization` header:

```javascript
headers: {
  'Authorization': 'Bearer <your-jwt-token>',
  'Content-Type': 'application/json'
}
```

### Getting JWT Token

1. **Register** or **Login** to get a JWT token
2. Store the token securely (localStorage/sessionStorage)
3. Include it in all authenticated requests

---

## 👤 User Service APIs

### Base Path: `/api/users`

#### 1. Register New User

**POST** `/api/users/register`

**Request Body:**

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "+1234567890",
  "defaultCurrency": "USD"
}
```

**Response (201 Created):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "507f1f77bcf86cd799439011",
  "email": "john@example.com",
  "name": "John Doe"
}
```

**When to Use:**

- User registration page
- Sign-up flow

---

#### 2. User Login

**POST** `/api/users/login`

**Request Body:**

```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "507f1f77bcf86cd799439011",
  "email": "john@example.com",
  "name": "John Doe"
}
```

**When to Use:**

- Login page
- Authentication flow

---

#### 3. Get User by ID

**GET** `/api/users/{id}`

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**

```json
{
  "id": "507f1f77bcf86cd799439011",
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "defaultCurrency": "USD",
  "friendIds": ["user2", "user3"]
}
```

**When to Use:**

- User profile page
- Display user details
- Member information in groups

---

#### 4. Update User Profile

**PUT** `/api/users/{id}`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**

```json
{
  "name": "John Updated",
  "email": "john.new@example.com",
  "password": "newpassword123",
  "phone": "+1234567890",
  "defaultCurrency": "EUR"
}
```

**Response (200 OK):**

```json
{
  "id": "507f1f77bcf86cd799439011",
  "name": "John Updated",
  "email": "john.new@example.com",
  "phone": "+1234567890",
  "defaultCurrency": "EUR"
}
```

**When to Use:**

- Settings page
- Profile edit page

---

#### 5. Get User's Friends

**GET** `/api/users/{id}/friends`

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**

```json
[
  {
    "id": "user2",
    "name": "Jane Smith",
    "email": "jane@example.com"
  },
  {
    "id": "user3",
    "name": "Bob Wilson",
    "email": "bob@example.com"
  }
]
```

**When to Use:**

- Friends list page
- Adding members to groups
- Selecting expense participants

---

#### 6. Add Friend

**POST** `/api/users/{id}/friends?friendId={friendId}`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**

- `friendId`: ID of the user to add as friend

**Response (200 OK):** Empty response

**When to Use:**

- Add friend button
- Friend request feature

---

#### 7. Remove Friend

**DELETE** `/api/users/{id}/friends/{friendId}`

**Headers:** `Authorization: Bearer <token>`

**Response (204 No Content):** Empty response

**When to Use:**

- Remove friend button
- Unfriend feature

---

#### 8. Search Users

**GET** `/api/users/search?query={searchQuery}`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**

- `query`: Search string (name or email)

**Response (200 OK):**

```json
[
  {
    "id": "user4",
    "name": "Alice Johnson",
    "email": "alice@example.com"
  }
]
```

**When to Use:**

- Search bar for adding friends
- Finding users to add to groups
- Member search

---

## 👥 Group Service APIs

### Base Path: `/api/groups`

#### 1. Create Group

**POST** `/api/groups`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>` (automatically set by API Gateway)

**Request Body:**

```json
{
  "name": "Weekend Trip",
  "description": "Beach house weekend",
  "category": "TRIP",
  "memberIds": ["user2", "user3", "user4"]
}
```

**Category Options:** `TRIP`, `HOME`, `COUPLE`, `OTHER`

**Response (201 Created):**

```json
{
  "success": true,
  "message": "Group created successfully",
  "data": {
    "id": 1,
    "name": "Weekend Trip",
    "description": "Beach house weekend",
    "category": "TRIP",
    "createdBy": "507f1f77bcf86cd799439011",
    "members": [
      {
        "userId": "507f1f77bcf86cd799439011",
        "name": "John Doe",
        "role": "ADMIN",
        "joinedAt": "2024-01-15T10:30:00"
      }
    ],
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

**When to Use:**

- Create new group page
- Quick create group modal

---

#### 2. Get Group by ID

**GET** `/api/groups/{groupId}`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Weekend Trip",
    "description": "Beach house weekend",
    "category": "TRIP",
    "createdBy": "507f1f77bcf86cd799439011",
    "members": [
      {
        "userId": "507f1f77bcf86cd799439011",
        "name": "John Doe",
        "role": "ADMIN",
        "joinedAt": "2024-01-15T10:30:00"
      },
      {
        "userId": "user2",
        "name": "Jane Smith",
        "role": "MEMBER",
        "joinedAt": "2024-01-15T10:31:00"
      }
    ],
    "totalExpenses": 1500.0,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

**When to Use:**

- Group details page
- Display group information
- Before adding expenses

---

#### 3. Get All User's Groups

**GET** `/api/groups`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Response (200 OK):**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Weekend Trip",
      "description": "Beach house weekend",
      "category": "TRIP",
      "memberCount": 4,
      "totalExpenses": 1500.0
    },
    {
      "id": 2,
      "name": "Apartment Expenses",
      "description": "Monthly bills",
      "category": "HOME",
      "memberCount": 3,
      "totalExpenses": 2300.0
    }
  ]
}
```

**When to Use:**

- Dashboard/Home page
- Group list view
- Navigation sidebar

---

#### 4. Get Groups Created by User

**GET** `/api/groups/created`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Response (200 OK):** Same format as "Get All User's Groups"

**When to Use:**

- "My Groups" tab
- Admin panel
- Filter for created groups

---

#### 5. Update Group

**PUT** `/api/groups/{groupId}`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Request Body:**

```json
{
  "name": "Updated Trip Name",
  "description": "Updated description",
  "category": "TRIP"
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Group updated successfully",
  "data": {
    "id": 1,
    "name": "Updated Trip Name",
    "description": "Updated description",
    "category": "TRIP"
  }
}
```

**When to Use:**

- Edit group page
- Group settings

---

#### 6. Delete Group

**DELETE** `/api/groups/{groupId}`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Group deleted successfully",
  "data": null
}
```

**When to Use:**

- Delete group button
- Group settings (admin only)

---

#### 7. Add Member to Group

**POST** `/api/groups/{groupId}/members`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Request Body:**

```json
{
  "userId": "user5"
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Member added successfully",
  "data": {
    "id": 1,
    "name": "Weekend Trip",
    "members": [...]
  }
}
```

**When to Use:**

- Add member button
- Invite users to group

---

#### 8. Remove Member from Group

**DELETE** `/api/groups/{groupId}/members/{memberId}`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Member removed successfully",
  "data": {
    "id": 1,
    "name": "Weekend Trip",
    "members": [...]
  }
}
```

**When to Use:**

- Remove member button
- Leave group feature

---

#### 9. Update Member Role

**PATCH** `/api/groups/{groupId}/members/{memberId}/role?role={role}`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Query Parameters:**

- `role`: `ADMIN` or `MEMBER`

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Member role updated successfully",
  "data": {
    "id": 1,
    "members": [...]
  }
}
```

**When to Use:**

- Member management page
- Promote/demote members

---

## 💰 Expense Service APIs

### Base Path: `/api/expenses`

#### 1. Create Expense

**POST** `/api/expenses`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Request Body (Equal Split):**

```json
{
  "description": "Dinner at restaurant",
  "amount": 150.0,
  "currency": "USD",
  "groupId": 1,
  "paidBy": "507f1f77bcf86cd799439011",
  "category": "FOOD",
  "splitType": "EQUAL",
  "participantIds": ["507f1f77bcf86cd799439011", "user2", "user3"],
  "receiptUrl": "https://example.com/receipt.jpg",
  "notes": "Pizza and drinks"
}
```

**Request Body (Exact Split):**

```json
{
  "description": "Shopping",
  "amount": 200.0,
  "currency": "USD",
  "groupId": 1,
  "paidBy": "507f1f77bcf86cd799439011",
  "category": "SHOPPING",
  "splitType": "EXACT",
  "exactAmounts": {
    "507f1f77bcf86cd799439011": 80.0,
    "user2": 70.0,
    "user3": 50.0
  }
}
```

**Request Body (Percentage Split):**

```json
{
  "description": "Rent",
  "amount": 1500.0,
  "currency": "USD",
  "groupId": 1,
  "paidBy": "507f1f77bcf86cd799439011",
  "category": "HOUSING",
  "splitType": "PERCENTAGE",
  "percentages": {
    "507f1f77bcf86cd799439011": 40,
    "user2": 30,
    "user3": 30
  }
}
```

**Split Types:** `EQUAL`, `EXACT`, `PERCENTAGE`

**Categories:** `FOOD`, `TRAVEL`, `ENTERTAINMENT`, `SHOPPING`, `HOUSING`, `UTILITIES`, `OTHER`

**Response (201 Created):**

```json
{
  "success": true,
  "message": "Expense created successfully",
  "data": {
    "id": 1,
    "description": "Dinner at restaurant",
    "amount": 150.0,
    "currency": "USD",
    "groupId": 1,
    "paidBy": "507f1f77bcf86cd799439011",
    "paidByName": "John Doe",
    "category": "FOOD",
    "splitType": "EQUAL",
    "splits": [
      {
        "userId": "507f1f77bcf86cd799439011",
        "userName": "John Doe",
        "amount": 50.0,
        "percentage": 33.33
      },
      {
        "userId": "user2",
        "userName": "Jane Smith",
        "amount": 50.0,
        "percentage": 33.33
      },
      {
        "userId": "user3",
        "userName": "Bob Wilson",
        "amount": 50.0,
        "percentage": 33.34
      }
    ],
    "createdAt": "2024-01-15T18:30:00"
  }
}
```

**When to Use:**

- Add expense page
- Quick add expense modal
- After group creation

---

#### 2. Get Expense by ID

**GET** `/api/expenses/{id}`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "description": "Dinner at restaurant",
    "amount": 150.00,
    "currency": "USD",
    "groupId": 1,
    "groupName": "Weekend Trip",
    "paidBy": "507f1f77bcf86cd799439011",
    "paidByName": "John Doe",
    "category": "FOOD",
    "splitType": "EQUAL",
    "splits": [...],
    "receiptUrl": "https://example.com/receipt.jpg",
    "notes": "Pizza and drinks",
    "createdAt": "2024-01-15T18:30:00"
  }
}
```

**When to Use:**

- Expense detail page
- View full expense information

---

#### 3. Get Group Expenses

**GET** `/api/expenses/group/{groupId}`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Response (200 OK):**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "description": "Dinner at restaurant",
      "amount": 150.0,
      "paidByName": "John Doe",
      "category": "FOOD",
      "createdAt": "2024-01-15T18:30:00"
    },
    {
      "id": 2,
      "description": "Gas for trip",
      "amount": 80.0,
      "paidByName": "Jane Smith",
      "category": "TRAVEL",
      "createdAt": "2024-01-16T09:00:00"
    }
  ]
}
```

**When to Use:**

- Group details page
- Expense list for specific group
- Transaction history

---

#### 4. Get My Expenses

**GET** `/api/expenses/my-expenses`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Response (200 OK):** Same format as "Get Group Expenses"

**When to Use:**

- Dashboard
- "My Expenses" tab
- Personal expense history

---

#### 5. Update Expense

**PUT** `/api/expenses/{id}`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Request Body:** Same as Create Expense

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Expense updated successfully",
  "data": {
    "id": 1,
    "description": "Updated dinner",
    "amount": 160.0
  }
}
```

**When to Use:**

- Edit expense page
- Modify expense details

---

#### 6. Delete Expense

**DELETE** `/api/expenses/{id}`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Expense deleted successfully",
  "data": null
}
```

**When to Use:**

- Delete expense button
- Remove incorrect expenses

---

#### 7. Get User Balance Summary

**GET** `/api/expenses/balance`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "userId": "507f1f77bcf86cd799439011",
    "totalOwed": 250.0,
    "totalOwing": 180.0,
    "netBalance": 70.0,
    "currency": "USD",
    "balanceWithUsers": [
      {
        "userId": "user2",
        "userName": "Jane Smith",
        "amount": 50.0,
        "type": "OWED"
      },
      {
        "userId": "user3",
        "userName": "Bob Wilson",
        "amount": -30.0,
        "type": "OWING"
      }
    ]
  }
}
```

**When to Use:**

- Dashboard overview
- Balance summary card
- "Who Owes You" section

---

#### 8. Get Balance for Specific User

**GET** `/api/expenses/balance/{userId}`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Response (200 OK):** Same format as "Get User Balance Summary"

**When to Use:**

- View another user's balance (if admin)
- Group member balance details

---

#### 9. Get Group Balances

**GET** `/api/expenses/group/{groupId}/balances`

**Headers:**

- `Authorization: Bearer <token>`

**Response (200 OK):**

```json
{
  "507f1f77bcf86cd799439011": 150.0,
  "user2": -80.0,
  "user3": -70.0
}
```

**When to Use:**

- Settlement calculations
- Group balance overview
- Before creating settlements

---

## 💳 Settlement Service APIs

### Base Path: `/api/settlements`

#### 1. Get Settlement Suggestions

**GET** `/api/settlements/group/{groupId}/suggestions`

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**

```json
{
  "groupId": 1,
  "suggestions": [
    {
      "payerId": "user2",
      "payerName": "Jane Smith",
      "payeeId": "507f1f77bcf86cd799439011",
      "payeeName": "John Doe",
      "amount": 80.0,
      "currency": "USD"
    },
    {
      "payerId": "user3",
      "payerName": "Bob Wilson",
      "payeeId": "507f1f77bcf86cd799439011",
      "payeeName": "John Doe",
      "amount": 70.0,
      "currency": "USD"
    }
  ],
  "totalSettlements": 2,
  "calculatedAt": "2024-01-20T10:00:00"
}
```

**When to Use:**

- "Settle Up" page
- Show who needs to pay whom
- Settlement recommendations

---

#### 2. Record Settlement

**POST** `/api/settlements`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Request Body:**

```json
{
  "groupId": 1,
  "payerId": "user2",
  "payeeId": "507f1f77bcf86cd799439011",
  "amount": 80.0,
  "currency": "USD",
  "paymentMethod": "UPI",
  "transactionId": "TXN123456789",
  "notes": "Settled via PhonePe"
}
```

**Payment Methods:** `CASH`, `UPI`, `BANK_TRANSFER`, `CREDIT_CARD`

**Response (201 Created):**

```json
{
  "id": 1,
  "groupId": 1,
  "payerId": "user2",
  "payerName": "Jane Smith",
  "payeeId": "507f1f77bcf86cd799439011",
  "payeeName": "John Doe",
  "amount": 80.0,
  "currency": "USD",
  "paymentMethod": "UPI",
  "transactionId": "TXN123456789",
  "status": "PENDING",
  "notes": "Settled via PhonePe",
  "recordedAt": "2024-01-20T11:00:00"
}
```

**When to Use:**

- After payment is made
- "Record Payment" button
- Settlement confirmation

---

#### 3. Get Group Settlements

**GET** `/api/settlements/group/{groupId}`

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "groupId": 1,
    "payerId": "user2",
    "payerName": "Jane Smith",
    "payeeId": "507f1f77bcf86cd799439011",
    "payeeName": "John Doe",
    "amount": 80.0,
    "currency": "USD",
    "paymentMethod": "UPI",
    "status": "COMPLETED",
    "recordedAt": "2024-01-20T11:00:00",
    "completedAt": "2024-01-20T11:30:00"
  }
]
```

**Settlement Status:** `PENDING`, `COMPLETED`

**When to Use:**

- Settlement history page
- Group transaction log
- Payment tracking

---

#### 4. Get My Settlements

**GET** `/api/settlements/my-settlements`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Response (200 OK):** Same format as "Get Group Settlements"

**When to Use:**

- Personal settlement history
- "My Payments" tab
- Payment status tracking

---

#### 5. Complete Settlement

**PUT** `/api/settlements/{id}/complete`

**Headers:**

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>`

**Response (200 OK):**

```json
{
  "id": 1,
  "groupId": 1,
  "status": "COMPLETED",
  "completedAt": "2024-01-20T11:30:00"
}
```

**When to Use:**

- Confirm payment received
- Payee confirmation button
- Mark settlement as complete

---

## 📦 Common Response Formats

### Success Response

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

### Error Response

```json
{
  "success": false,
  "message": "Error message",
  "error": "Detailed error description",
  "timestamp": "2024-01-20T10:00:00"
}
```

---

## ⚠️ Error Handling

### HTTP Status Codes

| Status Code | Meaning      | Action                                          |
| ----------- | ------------ | ----------------------------------------------- |
| 200         | Success      | Process the response data                       |
| 201         | Created      | Resource created successfully                   |
| 204         | No Content   | Operation successful, no data returned          |
| 400         | Bad Request  | Check request body/parameters                   |
| 401         | Unauthorized | Token missing or invalid - redirect to login    |
| 403         | Forbidden    | User doesn't have permission                    |
| 404         | Not Found    | Resource doesn't exist                          |
| 409         | Conflict     | Resource already exists (e.g., duplicate email) |
| 500         | Server Error | Show generic error message, retry               |

### Common Error Scenarios

#### 1. Token Expired

```json
{
  "error": "Token expired",
  "message": "Please login again"
}
```

**Action:** Clear token, redirect to login page

#### 2. Validation Error

```json
{
  "error": "Validation failed",
  "message": "Invalid input data",
  "errors": {
    "email": "Invalid email format",
    "amount": "Amount must be greater than 0"
  }
}
```

**Action:** Display field-specific errors to user

#### 3. Permission Denied

```json
{
  "error": "Forbidden",
  "message": "You don't have permission to perform this action"
}
```

**Action:** Show permission error, disable action buttons

---

## 🔄 Integration Flow

### 1. Initial Setup Flow

```
1. Register/Login → Get JWT Token
2. Store token in localStorage/sessionStorage
3. Set up axios/fetch interceptor to include token
4. Fetch user's groups → Display on dashboard
```

### 2. Create Group & Add Expense Flow

```
1. POST /api/groups → Create new group
2. POST /api/groups/{groupId}/members → Add members
3. POST /api/expenses → Create expense
4. GET /api/expenses/group/{groupId} → Refresh expense list
```

### 3. Settlement Flow

```
1. GET /api/settlements/group/{groupId}/suggestions → Get who owes whom
2. Display settlement suggestions to user
3. User makes payment offline
4. POST /api/settlements → Record the payment
5. Payee confirms: PUT /api/settlements/{id}/complete
6. GET /api/expenses/balance → Refresh balance
```

### 4. Dashboard Refresh Flow

```
1. GET /api/groups → Get all groups
2. GET /api/expenses/my-expenses → Get user's expenses
3. GET /api/expenses/balance → Get balance summary
4. Display dashboard with all information
```

---

## 💡 Frontend Implementation Tips

### 1. Authentication

```javascript
// Store token after login
localStorage.setItem("token", response.token);
localStorage.setItem("userId", response.userId);

// Create axios instance with interceptor
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### 2. API Service Example

```javascript
// userService.js
export const userService = {
  register: (userData) => api.post("/api/users/register", userData),
  login: (credentials) => api.post("/api/users/login", credentials),
  getProfile: (userId) => api.get(`/api/users/${userId}`),
  searchUsers: (query) => api.get(`/api/users/search?query=${query}`),
};

// groupService.js
export const groupService = {
  createGroup: (groupData) => api.post("/api/groups", groupData),
  getGroups: () => api.get("/api/groups"),
  getGroupById: (groupId) => api.get(`/api/groups/${groupId}`),
  addMember: (groupId, userId) =>
    api.post(`/api/groups/${groupId}/members`, { userId }),
};

// expenseService.js
export const expenseService = {
  createExpense: (expenseData) => api.post("/api/expenses", expenseData),
  getGroupExpenses: (groupId) => api.get(`/api/expenses/group/${groupId}`),
  getMyBalance: () => api.get("/api/expenses/balance"),
};

// settlementService.js
export const settlementService = {
  getSuggestions: (groupId) =>
    api.get(`/api/settlements/group/${groupId}/suggestions`),
  recordSettlement: (settlementData) =>
    api.post("/api/settlements", settlementData),
  completeSettlement: (settlementId) =>
    api.put(`/api/settlements/${settlementId}/complete`),
};
```

### 3. Error Handling

```javascript
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem("token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);
```

### 4. State Management (Example with Context API)

```javascript
// AuthContext.js
const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem("token"));

  const login = async (credentials) => {
    const response = await userService.login(credentials);
    setToken(response.token);
    setUser(response);
    localStorage.setItem("token", response.token);
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem("token");
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
```

---

## 🎯 Key Pages to Implement

### 1. Authentication Pages

- **Login Page** - POST `/api/users/login`
- **Register Page** - POST `/api/users/register`

### 2. Dashboard

- **Groups List** - GET `/api/groups`
- **Balance Summary** - GET `/api/expenses/balance`
- **Recent Expenses** - GET `/api/expenses/my-expenses`

### 3. Group Management

- **Create Group** - POST `/api/groups`
- **Group Details** - GET `/api/groups/{id}`
- **Add Members** - POST `/api/groups/{id}/members`
- **Group Settings** - PUT `/api/groups/{id}`, DELETE `/api/groups/{id}`

### 4. Expense Management

- **Add Expense** - POST `/api/expenses`
- **View Expenses** - GET `/api/expenses/group/{groupId}`
- **Edit Expense** - PUT `/api/expenses/{id}`
- **Delete Expense** - DELETE `/api/expenses/{id}`

### 5. Settlement

- **View Settlements** - GET `/api/settlements/group/{groupId}/suggestions`
- **Record Payment** - POST `/api/settlements`
- **Settlement History** - GET `/api/settlements/group/{groupId}`

### 6. User Profile

- **View Profile** - GET `/api/users/{id}`
- **Edit Profile** - PUT `/api/users/{id}`
- **Friends List** - GET `/api/users/{id}/friends`
- **Add/Remove Friends** - POST/DELETE `/api/users/{id}/friends`

---

## 📝 Notes

1. **CORS**: API Gateway has CORS enabled for frontend development
2. **X-User-Id Header**: Automatically extracted from JWT by API Gateway
3. **Rate Limiting**: Currently no rate limiting implemented
4. **File Upload**: Receipt URLs should be uploaded to a separate file storage service
5. **Real-time Updates**: Consider implementing WebSocket for real-time expense/settlement notifications
6. **Pagination**: Not currently implemented, consider adding for large datasets
7. **Currency**: Default currency is USD, but supports multiple currencies

---

## ⚠️ Known Issues & Backend Fixes Needed

### Issue #1: Missing `/api/expenses/user` Endpoint

**Status**: � Frontend Fix Available (No Backend Change Needed)  
**Severity**: High  
**Date Identified**: November 1, 2025

> **Quick Fix**: Use existing endpoint `/api/expenses/my-expenses` instead of `/api/expenses/user`. Both serve the same purpose.

#### Problem Summary

The frontend expects `GET /api/expenses/user` to return all expenses for the currently authenticated user across all groups. However, the backend currently has `GET /api/expenses/{userId}` which tries to parse "user" as a Long, causing:

```
java.lang.NumberFormatException: For input string: "user"
500 Internal Server Error
```

#### Expected Endpoint

**GET** `/api/expenses/user`

**Purpose**: Get all expenses for the current user across all groups

**Headers**:

- `Authorization: Bearer <token>`
- `X-User-Id: <userId>` (auto-extracted by API Gateway)

**Expected Response (200 OK)**:

```json
{
  "success": true,
  "message": "Expenses retrieved successfully",
  "data": [
    {
      "id": 1,
      "description": "Dinner",
      "amount": 50.0,
      "paidBy": "507f1f77bcf86cd799439011",
      "paidByName": "John Doe",
      "groupId": 1,
      "groupName": "Weekend Trip",
      "category": "FOOD",
      "createdAt": "2025-11-01T20:00:00Z"
    }
  ]
}
```

#### Backend Implementation Needed

**File**: `expense-service/src/main/java/com/splitwise/expense/controller/ExpenseController.java`

```java
/**
 * Get all expenses for current user across all groups
 *
 * GET /api/expenses/user
 */
@GetMapping("/user")
public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getCurrentUserExpenses(
        @RequestHeader("X-User-Id") String userId) {

    log.info("Fetching all expenses for user: {}", userId);
    List<ExpenseResponse> expenses = expenseService.getUserExpenses(userId);

    return ResponseEntity.ok(ApiResponse.success(
        "Expenses retrieved successfully",
        expenses
    ));
}
```

**Service Layer**: `ExpenseService.java`

```java
public List<ExpenseResponse> getUserExpenses(String userId) {
    // Get all expenses where user is either payer or participant
    List<Expense> expenses = expenseRepository.findByUserInvolved(userId);
    return expenses.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
}
```

**Repository Layer**: `ExpenseRepository.java`

```java
@Query("SELECT DISTINCT e FROM Expense e LEFT JOIN e.splits s " +
       "WHERE (e.paidBy = :userId OR s.userId = :userId) " +
       "AND e.isActive = true " +
       "ORDER BY e.createdAt DESC")
List<Expense> findByUserInvolved(@Param("userId") String userId);
```

#### Current Frontend Workaround

**Location**: `src/app/features/dashboard/dashboard.ts` (or equivalent)

The frontend has temporarily commented out the call to prevent errors:

```typescript
// WORKAROUND: Backend /api/expenses/user endpoint not implemented yet
// TODO: Uncomment when backend implements GET /api/expenses/user
// this.expenseService.getUserExpenses().subscribe({
//   next: (expenses) => {
//     this.recentExpenses = expenses;
//     this.totalExpenses = expenses.length;
//   },
//   error: (error) => console.error('Failed to load user expenses:', error)
// });
```

#### Impact

- ✅ Dashboard loads without errors
- ✅ User balance still displays correctly
- ✅ Groups and other features work
- ❌ Dashboard doesn't show recent expenses count
- ❌ Recent expenses list is empty on dashboard

#### Testing the Fix

Once backend is fixed:

1. **Test with curl**:

```bash
curl -X GET http://localhost:8080/api/expenses/user \
  -H "Authorization: Bearer <your_jwt_token>" \
  -H "Accept: application/json"
```

2. **Uncomment frontend code** in dashboard component

3. **Verify**:
   - Dashboard shows total expense count
   - Recent activity list displays last 5 expenses
   - No console errors

#### Related Endpoints (Working Correctly)

- ✅ `GET /api/expenses/my-expenses` - **Use this instead!** Returns user expenses across all groups
- ✅ `GET /api/expenses/balance` - Correctly extracts user from JWT
- ✅ `GET /api/expenses/group/{groupId}` - Returns group expenses

**Resolution Options**:

1. **Frontend Fix (Recommended)**: Update frontend to use `/api/expenses/my-expenses` instead of `/api/expenses/user`
2. **Backend Fix**: Add `/api/expenses/user` as an alias to `/api/expenses/my-expenses`
3. **Both**: Implement backend endpoint AND update frontend for consistency

**Note**: The `/api/expenses/my-expenses` endpoint already exists and serves the same purpose. The frontend should be updated to use this endpoint, which will immediately resolve the issue without requiring backend changes.

#### Priority & Effort

**Priority**: High (blocks core dashboard feature)  
**Estimated Effort**: 1-2 hours

---

## 📝 Notes (Continued)

## 🚀 Getting Started Checklist

- [ ] Set up authentication flow (register/login)
- [ ] Create API service files with axios/fetch
- [ ] Implement token storage and interceptors
- [ ] Build dashboard with groups and balance
- [ ] Create group management pages
- [ ] Implement expense creation and listing
- [ ] Add settlement calculation and recording
- [ ] Test all error scenarios
- [ ] Add loading states and error messages
- [ ] Implement responsive design

---

## 📞 Support

For questions or issues:

- Check Swagger UI: http://localhost:8080/swagger-ui.html
- Refer to individual service documentation in `/docs` folder
- Test endpoints using Postman/Insomnia
- Review **Known Issues** section above for pending backend fixes

---

**Last Updated:** November 1, 2025  
**API Version:** 1.0.0  
**Backend Base URL:** http://localhost:8080
