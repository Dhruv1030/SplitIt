# 🎨 Frontend Integration Guide

## Overview

This guide provides everything frontend developers need to integrate with the SplitIt backend API.

## 🚀 Quick Start

### Prerequisites
- SplitIt backend services running (see README.md)
- Access to API Gateway: `http://localhost:8080`
- Swagger documentation: `http://localhost:8081/swagger-ui.html` (per service)

### Base Configuration

```javascript
// API Configuration
const API_CONFIG = {
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
};
```

## 🔐 Authentication Flow

### 1. User Registration

**Endpoint**: `POST /api/users/register`

```javascript
const registerUser = async (userData) => {
  const response = await fetch('http://localhost:8080/api/users/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      email: userData.email,
      password: userData.password,
      name: userData.name,
      phoneNumber: userData.phoneNumber
    })
  });

  const data = await response.json();
  
  // Store token
  localStorage.setItem('jwt_token', data.token);
  localStorage.setItem('user_id', data.userId);
  
  return data;
};
```

### 2. User Login

**Endpoint**: `POST /api/users/login`

```javascript
const loginUser = async (credentials) => {
  const response = await fetch('http://localhost:8080/api/users/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      email: credentials.email,
      password: credentials.password
    })
  });

  const data = await response.json();
  
  // Store token
  localStorage.setItem('jwt_token', data.token);
  localStorage.setItem('user_id', data.userId);
  
  return data;
};
```

### 3. Authenticated Requests

```javascript
const makeAuthenticatedRequest = async (endpoint, options = {}) => {
  const token = localStorage.getItem('jwt_token');
  
  const response = await fetch(`http://localhost:8080${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      ...options.headers
    }
  });

  if (response.status === 401) {
    // Token expired or invalid
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_id');
    window.location.href = '/login';
    return;
  }

  return response.json();
};
```

## 📦 API Client Implementation

### Complete API Client (Vanilla JavaScript)

```javascript
class SplitItAPI {
  constructor(baseURL = 'http://localhost:8080') {
    this.baseURL = baseURL;
  }

  getToken() {
    return localStorage.getItem('jwt_token');
  }

  setToken(token) {
    localStorage.setItem('jwt_token', token);
  }

  clearToken() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_id');
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

    try {
      const response = await fetch(`${this.baseURL}${endpoint}`, {
        ...options,
        headers
      });

      if (response.status === 401) {
        this.clearToken();
        throw new Error('Unauthorized');
      }

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Request failed');
      }

      return await response.json();
    } catch (error) {
      console.error('API Request failed:', error);
      throw error;
    }
  }

  // User APIs
  async registerUser(userData) {
    const data = await this.request('/api/users/register', {
      method: 'POST',
      body: JSON.stringify(userData)
    });
    this.setToken(data.token);
    localStorage.setItem('user_id', data.userId);
    return data;
  }

  async loginUser(credentials) {
    const data = await this.request('/api/users/login', {
      method: 'POST',
      body: JSON.stringify(credentials)
    });
    this.setToken(data.token);
    localStorage.setItem('user_id', data.userId);
    return data;
  }

  async getUserProfile(userId) {
    return this.request(`/api/users/${userId}`);
  }

  async updateUserProfile(userId, userData) {
    return this.request(`/api/users/${userId}`, {
      method: 'PUT',
      body: JSON.stringify(userData)
    });
  }

  async searchUsers(query) {
    return this.request(`/api/users/search?query=${encodeURIComponent(query)}`);
  }

  async addFriend(userId, friendId) {
    return this.request(`/api/users/${userId}/friends?friendId=${friendId}`, {
      method: 'POST'
    });
  }

  async getFriends(userId) {
    return this.request(`/api/users/${userId}/friends`);
  }

  // Group APIs
  async createGroup(groupData) {
    return this.request('/api/groups', {
      method: 'POST',
      body: JSON.stringify(groupData)
    });
  }

  async getGroup(groupId) {
    return this.request(`/api/groups/${groupId}`);
  }

  async getUserGroups(userId) {
    return this.request(`/api/groups/user/${userId}`);
  }

  async addGroupMember(groupId, memberData) {
    return this.request(`/api/groups/${groupId}/members`, {
      method: 'POST',
      body: JSON.stringify(memberData)
    });
  }

  async removeGroupMember(groupId, userId) {
    return this.request(`/api/groups/${groupId}/members/${userId}`, {
      method: 'DELETE'
    });
  }

  // Expense APIs
  async createExpense(expenseData) {
    return this.request('/api/expenses', {
      method: 'POST',
      body: JSON.stringify(expenseData)
    });
  }

  async getExpense(expenseId) {
    return this.request(`/api/expenses/${expenseId}`);
  }

  async getGroupExpenses(groupId) {
    return this.request(`/api/expenses/group/${groupId}`);
  }

  async getUserExpenses(userId) {
    return this.request(`/api/expenses/user/${userId}`);
  }

  async updateExpense(expenseId, expenseData) {
    return this.request(`/api/expenses/${expenseId}`, {
      method: 'PUT',
      body: JSON.stringify(expenseData)
    });
  }

  async deleteExpense(expenseId) {
    return this.request(`/api/expenses/${expenseId}`, {
      method: 'DELETE'
    });
  }

  // Settlement APIs
  async getUserBalances(userId) {
    return this.request(`/api/settlements/user/${userId}/balances`);
  }

  async getGroupBalances(groupId) {
    return this.request(`/api/settlements/group/${groupId}/balances`);
  }

  async optimizeSettlements(groupId) {
    return this.request(`/api/settlements/optimize`, {
      method: 'POST',
      body: JSON.stringify({ groupId })
    });
  }

  async recordSettlement(settlementData) {
    return this.request('/api/settlements', {
      method: 'POST',
      body: JSON.stringify(settlementData)
    });
  }

  // Payment APIs
  async createPayment(paymentData) {
    return this.request('/api/payments', {
      method: 'POST',
      body: JSON.stringify(paymentData)
    });
  }

  async getPaymentStatus(paymentId) {
    return this.request(`/api/payments/${paymentId}`);
  }

  // Analytics APIs
  async getUserAnalytics(userId, period = 'month') {
    return this.request(`/api/analytics/user/${userId}?period=${period}`);
  }

  async getGroupAnalytics(groupId, period = 'month') {
    return this.request(`/api/analytics/group/${groupId}?period=${period}`);
  }
}

// Export singleton instance
const api = new SplitItAPI();
export default api;
```

## ⚛️ React Integration

### Setup with Axios

```bash
npm install axios
```

```javascript
// src/api/client.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Request interceptor
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

// Response interceptor
api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('user_id');
      window.location.href = '/login';
    }
    return Promise.reject(error.response?.data || error);
  }
);

export default api;
```

### React Hooks Example

```javascript
// src/hooks/useAuth.js
import { useState, useEffect } from 'react';
import api from '../api/client';

export const useAuth = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('jwt_token');
    const userId = localStorage.getItem('user_id');
    
    if (token && userId) {
      api.get(`/api/users/${userId}`)
        .then(data => {
          setUser(data);
          setLoading(false);
        })
        .catch(() => {
          localStorage.removeItem('jwt_token');
          localStorage.removeItem('user_id');
          setLoading(false);
        });
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (credentials) => {
    try {
      const data = await api.post('/api/users/login', credentials);
      localStorage.setItem('jwt_token', data.token);
      localStorage.setItem('user_id', data.userId);
      setUser(data);
      return data;
    } catch (error) {
      throw error;
    }
  };

  const register = async (userData) => {
    try {
      const data = await api.post('/api/users/register', userData);
      localStorage.setItem('jwt_token', data.token);
      localStorage.setItem('user_id', data.userId);
      setUser(data);
      return data;
    } catch (error) {
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_id');
    setUser(null);
  };

  return { user, loading, login, register, logout };
};
```

### React Context Provider

```javascript
// src/contexts/AuthContext.js
import React, { createContext, useContext } from 'react';
import { useAuth } from '../hooks/useAuth';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const auth = useAuth();

  return (
    <AuthContext.Provider value={auth}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuthContext = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuthContext must be used within AuthProvider');
  }
  return context;
};
```

### React Component Examples

```javascript
// src/components/LoginForm.jsx
import React, { useState } from 'react';
import { useAuthContext } from '../contexts/AuthContext';

const LoginForm = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useAuthContext();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    try {
      await login({ email, password });
      // Redirect or show success
    } catch (err) {
      setError(err.message || 'Login failed');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="Email"
        required
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="Password"
        required
      />
      {error && <div className="error">{error}</div>}
      <button type="submit">Login</button>
    </form>
  );
};

export default LoginForm;
```

## 🅰️ Angular Integration

### Setup HttpClient

```typescript
// src/app/services/api.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseURL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('jwt_token');
    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
    
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    
    return headers;
  }

  private handleError(error: HttpErrorResponse) {
    if (error.status === 401) {
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('user_id');
      // Navigate to login
    }
    return throwError(() => error.error);
  }

  // User APIs
  register(userData: any): Observable<any> {
    return this.http.post(
      `${this.baseURL}/api/users/register`,
      userData,
      { headers: this.getHeaders() }
    ).pipe(
      map((data: any) => {
        localStorage.setItem('jwt_token', data.token);
        localStorage.setItem('user_id', data.userId);
        return data;
      }),
      catchError(this.handleError)
    );
  }

  login(credentials: any): Observable<any> {
    return this.http.post(
      `${this.baseURL}/api/users/login`,
      credentials,
      { headers: this.getHeaders() }
    ).pipe(
      map((data: any) => {
        localStorage.setItem('jwt_token', data.token);
        localStorage.setItem('user_id', data.userId);
        return data;
      }),
      catchError(this.handleError)
    );
  }

  getUserProfile(userId: string): Observable<any> {
    return this.http.get(
      `${this.baseURL}/api/users/${userId}`,
      { headers: this.getHeaders() }
    ).pipe(catchError(this.handleError));
  }

  // Group APIs
  createGroup(groupData: any): Observable<any> {
    return this.http.post(
      `${this.baseURL}/api/groups`,
      groupData,
      { headers: this.getHeaders() }
    ).pipe(catchError(this.handleError));
  }

  getUserGroups(userId: string): Observable<any> {
    return this.http.get(
      `${this.baseURL}/api/groups/user/${userId}`,
      { headers: this.getHeaders() }
    ).pipe(catchError(this.handleError));
  }

  // Expense APIs
  createExpense(expenseData: any): Observable<any> {
    return this.http.post(
      `${this.baseURL}/api/expenses`,
      expenseData,
      { headers: this.getHeaders() }
    ).pipe(catchError(this.handleError));
  }

  getGroupExpenses(groupId: string): Observable<any> {
    return this.http.get(
      `${this.baseURL}/api/expenses/group/${groupId}`,
      { headers: this.getHeaders() }
    ).pipe(catchError(this.handleError));
  }
}
```

## 📱 Vue.js Integration

```javascript
// src/api/index.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
});

api.interceptors.request.use(config => {
  const token = localStorage.getItem('jwt_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('user_id');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

```javascript
// src/composables/useAuth.js
import { ref } from 'vue';
import api from '@/api';

export function useAuth() {
  const user = ref(null);
  const loading = ref(false);
  const error = ref(null);

  const login = async (credentials) => {
    loading.value = true;
    error.value = null;
    
    try {
      const data = await api.post('/api/users/login', credentials);
      localStorage.setItem('jwt_token', data.token);
      localStorage.setItem('user_id', data.userId);
      user.value = data;
      return data;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const register = async (userData) => {
    loading.value = true;
    error.value = null;
    
    try {
      const data = await api.post('/api/users/register', userData);
      localStorage.setItem('jwt_token', data.token);
      localStorage.setItem('user_id', data.userId);
      user.value = data;
      return data;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const logout = () => {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_id');
    user.value = null;
  };

  return {
    user,
    loading,
    error,
    login,
    register,
    logout
  };
}
```

## 📊 Data Models (TypeScript)

```typescript
// types/api.ts

export interface User {
  id: string;
  email: string;
  name: string;
  phoneNumber: string;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
  name: string;
}

export interface Group {
  id: string;
  name: string;
  description?: string;
  createdBy: string;
  members: GroupMember[];
  createdAt: string;
}

export interface GroupMember {
  userId: string;
  name: string;
  email: string;
  role: 'ADMIN' | 'MEMBER';
}

export interface Expense {
  id: string;
  description: string;
  amount: number;
  currency: string;
  groupId: string;
  paidBy: string;
  splitType: 'EQUAL' | 'PERCENTAGE' | 'EXACT';
  splits: ExpenseSplit[];
  category?: string;
  date: string;
  createdAt: string;
}

export interface ExpenseSplit {
  userId: string;
  amount: number;
  percentage?: number;
}

export interface Balance {
  userId: string;
  userName: string;
  totalOwed: number;
  totalOwing: number;
  netBalance: number;
}

export interface Settlement {
  id: string;
  from: string;
  to: string;
  amount: number;
  groupId: string;
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED';
  createdAt: string;
}
```

## 🎯 Best Practices

### 1. Token Management
- Store JWT in localStorage or httpOnly cookies
- Implement token refresh mechanism
- Clear token on logout or 401 responses

### 2. Error Handling
- Always handle API errors gracefully
- Show user-friendly error messages
- Log errors for debugging

### 3. Loading States
- Show loading indicators during API calls
- Disable buttons during submission
- Handle race conditions

### 4. API Optimization
- Implement request debouncing for search
- Cache frequently accessed data
- Use pagination for large lists

### 5. Security
- Never store passwords in frontend
- Validate user input before sending
- Use HTTPS in production
- Implement CSRF protection

## 🧪 Testing

### Example API Tests (Jest)

```javascript
import api from './client';

describe('User API', () => {
  it('should register a new user', async () => {
    const userData = {
      email: 'test@example.com',
      password: 'password123',
      name: 'Test User',
      phoneNumber: '+1234567890'
    };

    const response = await api.registerUser(userData);

    expect(response).toHaveProperty('token');
    expect(response).toHaveProperty('userId');
    expect(response.email).toBe(userData.email);
  });

  it('should login user', async () => {
    const credentials = {
      email: 'test@example.com',
      password: 'password123'
    };

    const response = await api.loginUser(credentials);

    expect(response).toHaveProperty('token');
    expect(localStorage.getItem('jwt_token')).toBeTruthy();
  });
});
```

## 📞 Support

For issues or questions:
- Check API Documentation: `/API_DOCUMENTATION.md`
- View Swagger UI: `http://localhost:8081/swagger-ui.html`
- GitHub Issues: https://github.com/Dhruv1030/SplitIt/issues
