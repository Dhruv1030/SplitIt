# 🚀 Quick Reference - Frontend Developer

## API Endpoint Quick Reference

### 🔐 Authentication (No Auth Required)
```javascript
// Register
POST /api/users/register
{ email, password, name, phoneNumber }
→ { token, userId, email, name }

// Login
POST /api/users/login
{ email, password }
→ { token, userId, email, name }
```

### 👤 User Management (Auth Required)
```javascript
GET    /api/users/{id}                    // Get user profile
PUT    /api/users/{id}                    // Update profile
GET    /api/users/{id}/friends            // Get friends list
POST   /api/users/{id}/friends?friendId=  // Add friend
DELETE /api/users/{id}/friends/{friendId} // Remove friend
GET    /api/users/search?query=           // Search users
```

### 👥 Groups (Auth Required)
```javascript
POST   /api/groups                        // Create group
GET    /api/groups/{id}                   // Get group details
PUT    /api/groups/{id}                   // Update group
DELETE /api/groups/{id}                   // Delete group
GET    /api/groups/user/{userId}          // User's groups
POST   /api/groups/{id}/members           // Add member
DELETE /api/groups/{id}/members/{userId}  // Remove member
```

### 💰 Expenses (Auth Required)
```javascript
POST   /api/expenses                      // Create expense
GET    /api/expenses/{id}                 // Get expense
PUT    /api/expenses/{id}                 // Update expense
DELETE /api/expenses/{id}                 // Delete expense
GET    /api/expenses/group/{groupId}      // Group expenses
GET    /api/expenses/user/{userId}        // User expenses
```

### ⚖️ Settlements (Auth Required)
```javascript
GET    /api/settlements/user/{userId}/balances      // User balances
GET    /api/settlements/group/{groupId}/balances    // Group balances
POST   /api/settlements/optimize                    // Calculate settlements
POST   /api/settlements                             // Record settlement
GET    /api/settlements/{id}                        // Get settlement
```

### 💳 Payments (Auth Required)
```javascript
POST   /api/payments              // Create payment
GET    /api/payments/{id}         // Payment status
GET    /api/payments/user/{userId} // User payments
```

### 📊 Analytics (Auth Required)
```javascript
GET    /api/analytics/user/{userId}?period=month   // User analytics
GET    /api/analytics/group/{groupId}?period=year  // Group analytics
```

---

## 🔑 Authentication Header

```javascript
headers: {
  'Authorization': 'Bearer <your-jwt-token>',
  'Content-Type': 'application/json'
}
```

---

## 💻 API Client (Copy & Paste)

```javascript
const API_BASE_URL = 'http://localhost:8080';

class API {
  getToken() {
    return localStorage.getItem('jwt_token');
  }

  async request(endpoint, options = {}) {
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers
    };

    const token = this.getToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers
    });

    if (response.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
      return;
    }

    if (!response.ok) {
      throw new Error((await response.json()).message);
    }

    return response.json();
  }

  // Auth
  async register(data) {
    const res = await this.request('/api/users/register', {
      method: 'POST',
      body: JSON.stringify(data)
    });
    localStorage.setItem('jwt_token', res.token);
    localStorage.setItem('user_id', res.userId);
    return res;
  }

  async login(data) {
    const res = await this.request('/api/users/login', {
      method: 'POST',
      body: JSON.stringify(data)
    });
    localStorage.setItem('jwt_token', res.token);
    localStorage.setItem('user_id', res.userId);
    return res;
  }

  // Users
  async getUser(id) {
    return this.request(`/api/users/${id}`);
  }

  async updateUser(id, data) {
    return this.request(`/api/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  }

  async searchUsers(query) {
    return this.request(`/api/users/search?query=${query}`);
  }

  async addFriend(userId, friendId) {
    return this.request(`/api/users/${userId}/friends?friendId=${friendId}`, {
      method: 'POST'
    });
  }

  // Groups
  async createGroup(data) {
    return this.request('/api/groups', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  async getGroup(id) {
    return this.request(`/api/groups/${id}`);
  }

  async getUserGroups(userId) {
    return this.request(`/api/groups/user/${userId}`);
  }

  async addMember(groupId, data) {
    return this.request(`/api/groups/${groupId}/members`, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  // Expenses
  async createExpense(data) {
    return this.request('/api/expenses', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  async getGroupExpenses(groupId) {
    return this.request(`/api/expenses/group/${groupId}`);
  }

  async deleteExpense(id) {
    return this.request(`/api/expenses/${id}`, {
      method: 'DELETE'
    });
  }

  // Settlements
  async getUserBalances(userId) {
    return this.request(`/api/settlements/user/${userId}/balances`);
  }

  async optimizeSettlements(groupId) {
    return this.request('/api/settlements/optimize', {
      method: 'POST',
      body: JSON.stringify({ groupId })
    });
  }
}

const api = new API();
export default api;
```

---

## 🎨 React Hook (Copy & Paste)

```javascript
import { useState, useEffect } from 'react';
import api from './api';

export function useAuth() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const userId = localStorage.getItem('user_id');
    if (userId) {
      api.getUser(userId)
        .then(setUser)
        .catch(() => localStorage.clear())
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (credentials) => {
    const data = await api.login(credentials);
    setUser(data);
    return data;
  };

  const register = async (userData) => {
    const data = await api.register(userData);
    setUser(data);
    return data;
  };

  const logout = () => {
    localStorage.clear();
    setUser(null);
  };

  return { user, loading, login, register, logout };
}
```

---

## 📦 Data Models (TypeScript)

```typescript
interface User {
  id: string;
  email: string;
  name: string;
  phoneNumber: string;
}

interface Group {
  id: string;
  name: string;
  description?: string;
  createdBy: string;
  members: Member[];
}

interface Member {
  userId: string;
  name: string;
  email: string;
  role: 'ADMIN' | 'MEMBER';
}

interface Expense {
  id: string;
  description: string;
  amount: number;
  currency: string;
  groupId: string;
  paidBy: string;
  splitType: 'EQUAL' | 'PERCENTAGE' | 'EXACT';
  splits: Split[];
  date: string;
}

interface Split {
  userId: string;
  amount: number;
  percentage?: number;
}

interface Balance {
  userId: string;
  userName: string;
  totalOwed: number;    // Others owe this user
  totalOwing: number;   // This user owes others
  netBalance: number;   // Positive = owed, Negative = owing
}

interface Settlement {
  from: string;
  to: string;
  amount: number;
}
```

---

## 🌐 URLs

| What | URL |
|------|-----|
| **API Gateway** | http://localhost:8080 |
| **Swagger UI** | http://localhost:8081/swagger-ui.html |
| **Service Discovery** | http://localhost:8761 |
| **Distributed Tracing** | http://localhost:9411 |

---

## ⚠️ Error Handling

```javascript
try {
  await api.createExpense(data);
} catch (error) {
  // error.message contains user-friendly message
  console.error(error.message);
}
```

### Common Status Codes
- `200` - Success
- `201` - Created
- `204` - Deleted
- `400` - Bad Request (validation error)
- `401` - Unauthorized (login required)
- `404` - Not Found
- `409` - Conflict (e.g., duplicate email)
- `500` - Server Error

---

## 🎯 Complete User Flow Example

```javascript
// 1. Register
const user = await api.register({
  email: 'user@example.com',
  password: 'password123',
  name: 'John Doe',
  phoneNumber: '+1234567890'
});
// Token automatically stored

// 2. Create Group
const group = await api.createGroup({
  name: 'Vacation 2025',
  description: 'Beach trip expenses'
});

// 3. Add Member
await api.addMember(group.id, {
  userId: 'friend-user-id',
  role: 'MEMBER'
});

// 4. Create Expense
const expense = await api.createExpense({
  description: 'Hotel booking',
  amount: 300,
  currency: 'USD',
  groupId: group.id,
  paidBy: user.userId,
  splitType: 'EQUAL',
  splits: [
    { userId: user.userId },
    { userId: 'friend-user-id' }
  ]
});

// 5. Check Balances
const balances = await api.getUserBalances(user.userId);
// balances = [{ userId, userName, totalOwed, totalOwing, netBalance }]

// 6. Optimize Settlements
const settlements = await api.optimizeSettlements(group.id);
// settlements = [{ from, to, amount }]
```

---

## 📱 CORS Support

✅ Works from:
- React (localhost:3000)
- Angular (localhost:4200)
- Vite (localhost:5173)
- Ionic (localhost:8100)

---

## 🆘 Quick Troubleshooting

**401 Unauthorized**
→ Check token is being sent in Authorization header

**CORS Error**
→ Use API Gateway (port 8080), not direct service ports

**Connection Refused**
→ Check services are running: `docker compose ps`

**Token Expired**
→ Token valid for 1 hour, login again

---

**📚 Full Documentation**: See `API_DOCUMENTATION.md` and `FRONTEND_INTEGRATION_GUIDE.md`
