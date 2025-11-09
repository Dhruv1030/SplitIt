# Frontend Implementation Roadmap

## SplitIt - What Frontend Needs to Implement

**Last Updated:** November 7, 2025  
**Backend Status:** All endpoints ready and documented with Swagger  
**Frontend Status:** Core features complete (95%)

---

## 🎯 **Critical: New Backend Features to Implement**

These features were completed TODAY on the backend and need frontend integration:

### 1. **Activity Feed** 📊 (HIGH PRIORITY)

**Backend:** READY ✅ - All endpoints live on port 8085

#### What It Is:

Real-time activity feed showing all group events (member added, expense created, payment recorded, etc.)

#### Backend Endpoints Available:

```typescript
// Activity Service API (http://localhost:8085)

// Create activity (called by other services automatically)
POST /api/activities
Body: {
  groupId: number,
  userId: string,
  activityType: 'GROUP_CREATED' | 'MEMBER_ADDED' | 'MEMBER_REMOVED' |
                 'EXPENSE_CREATED' | 'EXPENSE_UPDATED' | 'EXPENSE_DELETED' |
                 'PAYMENT_RECORDED' | 'SETTLEMENT_COMPLETED',
  description: string,
  metadata?: object
}

// Get activities for a group (paginated)
GET /api/activities/group/{groupId}?page=0&size=20
Response: Page<ActivityResponse> {
  content: [{
    id: number,
    groupId: number,
    userId: string,
    userName: string,
    activityType: string,
    description: string,
    metadata: object,
    createdAt: string
  }],
  totalElements: number,
  totalPages: number
}

// Get recent activities (last 10)
GET /api/activities/group/{groupId}/recent
Response: ActivityResponse[]

// Get user activities across all groups
GET /api/activities/user/{userId}?page=0&size=20
Response: Page<ActivityResponse>

// Get activities by date range
GET /api/activities/group/{groupId}/range?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
Response: ActivityResponse[]

// Get activity count
GET /api/activities/group/{groupId}/count
Response: number
```

#### Frontend Components to Build:

**1.1 Activity Feed Component** (`features/activities/activity-feed/`)

```typescript
// Location: src/app/features/activities/activity-feed/
// Display real-time activity feed in groups

Features:
- Show activities with icons based on type
- Time-relative formatting ("2 hours ago", "yesterday")
- Infinite scroll or pagination
- Pull-to-refresh on mobile
- Filter by activity type
- Real-time updates (optional: WebSocket)

UI Design:
┌─────────────────────────────────────┐
│  Recent Activity                    │
├─────────────────────────────────────┤
│  👤 John added Sarah to the group   │
│     2 hours ago                     │
├─────────────────────────────────────┤
│  💰 Sarah added "Dinner" - $120     │
│     5 hours ago                     │
├─────────────────────────────────────┤
│  ✅ Mike paid John $50              │
│     Yesterday                       │
└─────────────────────────────────────┘
```

**1.2 Group Detail - Activity Tab**

```typescript
// Update: src/app/features/groups/group-detail/group-detail.component.ts
// Add new tab to show group activities

Add Tab:
- Overview (existing)
- Expenses (existing)
- Settlements (existing)
- Members (existing)
- Activity Feed (NEW) ← Add this tab

Implementation:
<mat-tab label="Activity">
  <app-activity-feed [groupId]="groupId"></app-activity-feed>
</mat-tab>
```

**1.3 Dashboard - Recent Activity Widget**

```typescript
// Update: src/app/features/dashboard/dashboard.component.ts
// Replace or enhance existing "Recent Expenses" with Activity Feed

Current: Shows only expenses
Enhanced: Shows all activities across all user's groups

Widget Design:
┌─────────────────────────────────────┐
│  Your Recent Activity        [View All] │
├─────────────────────────────────────┤
│  🏠 Trip to Paris                   │
│  Sarah added "Hotel" - $300         │
│  2 hours ago                        │
├─────────────────────────────────────┤
│  🍕 Roommates                       │
│  You paid Mike $150                 │
│  Yesterday                          │
└─────────────────────────────────────┘
```

**1.4 Activity Service (Angular)**

```typescript
// Create: src/app/core/services/activity.service.ts

import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";

export interface Activity {
  id: number;
  groupId: number;
  userId: string;
  userName: string;
  activityType: string;
  description: string;
  metadata?: any;
  createdAt: string;
}

export interface ActivityPage {
  content: Activity[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({
  providedIn: "root",
})
export class ActivityService {
  private apiUrl = "/api/activities";

  constructor(private http: HttpClient) {}

  getGroupActivities(
    groupId: number,
    page = 0,
    size = 20
  ): Observable<ActivityPage> {
    const params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());
    return this.http.get<ActivityPage>(`${this.apiUrl}/group/${groupId}`, {
      params,
    });
  }

  getRecentGroupActivities(groupId: number): Observable<Activity[]> {
    return this.http.get<Activity[]>(`${this.apiUrl}/group/${groupId}/recent`);
  }

  getUserActivities(
    userId: string,
    page = 0,
    size = 20
  ): Observable<ActivityPage> {
    const params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());
    return this.http.get<ActivityPage>(`${this.apiUrl}/user/${userId}`, {
      params,
    });
  }

  getActivitiesByDateRange(
    groupId: number,
    startDate: Date,
    endDate: Date
  ): Observable<Activity[]> {
    const params = new HttpParams()
      .set("startDate", startDate.toISOString())
      .set("endDate", endDate.toISOString());
    return this.http.get<Activity[]>(`${this.apiUrl}/group/${groupId}/range`, {
      params,
    });
  }

  getActivityCount(groupId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/group/${groupId}/count`);
  }
}
```

---

### 2. **Email Notifications Display** 📧 (MEDIUM PRIORITY)

**Backend:** READY ✅ - Emails are sent automatically

#### What It Is:

Backend automatically sends beautiful HTML emails for:

- Payment reminders (purple gradient)
- Payment received confirmations (green gradient)
- Group invitations (pink gradient)
- Weekly digest (every Monday 9 AM)

#### Frontend Changes Needed:

**2.1 Email Preferences Component** (NEW)

```typescript
// Create: src/app/features/profile/email-preferences/

Features:
- Toggle email notifications on/off (future backend support)
- Choose notification frequency
- Unsubscribe from weekly digest
- Test email button (sends test email)

UI Design:
┌─────────────────────────────────────┐
│  Email Notification Preferences     │
├─────────────────────────────────────┤
│  ☑ Payment Reminders               │
│     Receive reminders when you owe  │
│                                     │
│  ☑ Payment Received                │
│     Get notified when paid          │
│                                     │
│  ☑ Group Invitations               │
│     Email when added to groups      │
│                                     │
│  ☑ Weekly Digest                   │
│     Summary every Monday            │
│                                     │
│  [Send Test Email]                  │
└─────────────────────────────────────┘
```

**2.2 Test Email Endpoint**

```typescript
// Already available - just needs frontend integration
POST /api/notifications/test?email=user@example.com
Response: {
  success: boolean,
  message: string,
  sentAt: string
}

// Add button in Email Preferences:
testEmail() {
  this.notificationService.sendTestEmail(this.userEmail).subscribe({
    next: (response) => {
      this.toastr.success('Test email sent! Check your inbox.');
    },
    error: (error) => {
      this.toastr.error('Failed to send test email');
    }
  });
}
```

**2.3 Notification Indicators** (Enhancement)

```typescript
// Add visual indicators in UI when emails are sent

Examples:
- After recording payment:
  "✅ Payment recorded! Confirmation email sent to Sarah."

- After adding member:
  "✅ Mike added to group! Invitation email sent."

- In settlement dialog:
  "💌 Payment reminder email will be sent"
```

---

### 3. **Weekly Digest Preview** 📅 (LOW PRIORITY)

**Backend:** Scheduler runs every Monday 9 AM automatically

#### Frontend Enhancement:

```typescript
// Create: src/app/features/profile/digest-preview/

Feature:
- Preview what will be in next weekly digest
- Shows all outstanding settlements
- Option to trigger digest manually (admin only)

GET /api/settlements/outstanding
Response: [{
  id: number,
  debtorEmail: string,
  debtorName: string,
  creditorName: string,
  amount: number,
  currency: string,
  groupName: string,
  groupId: number,
  createdAt: string
}]

UI Design:
┌─────────────────────────────────────┐
│  Weekly Digest Preview              │
│  Next digest: Monday, Nov 11, 9 AM  │
├─────────────────────────────────────┤
│  Outstanding Payments:              │
│                                     │
│  💰 Trip to Paris                   │
│     You owe John $150               │
│                                     │
│  🏠 Roommates                       │
│     You owe Sarah $75               │
│                                     │
│  Total: $225                        │
│                                     │
│  [Preview Email]                    │
└─────────────────────────────────────┘
```

---

## 🚀 **Additional Features to Implement**

These features are ready on backend and will enhance the frontend:

### 4. **Enhanced Dashboard Statistics** 📊

**Current:** Basic stats (groups, expenses, balances)  
**Enhanced:** More detailed analytics

#### New API Calls to Add:

```typescript
// Already working - just add to dashboard:

// Get user's total balance across all groups
GET /api/expenses/user/balance
Response: {
  totalOwed: number,      // Money you're owed
  totalOwing: number,     // Money you owe
  netBalance: number      // totalOwed - totalOwing
}

// Get recent settlements
GET /api/settlements/user/{userId}
Response: Settlement[]

// Show on dashboard:
┌─────────────────────────────────────┐
│  Financial Summary                  │
├─────────────────────────────────────┤
│  You're Owed:    +$450.00 🟢       │
│  You Owe:        -$225.00 🔴       │
│  Net Balance:    +$225.00 🟢       │
│                                     │
│  Recent Payments:                   │
│  ✅ Received $50 from Mike          │
│  ✅ Paid $100 to Sarah              │
└─────────────────────────────────────┘
```

---

### 5. **Group Balance Visualization** 💹

**Backend:** GET /api/settlements/group/{groupId} returns all settlements

#### Component to Build:

```typescript
// Create: src/app/features/groups/balance-chart/

Features:
- Visual chart showing who owes whom in group
- Network graph or sankey diagram
- Simplified vs detailed view
- Click to record payment

Libraries:
- ngx-charts
- Chart.js
- D3.js

Sample UI:
     John
    /  |  \
  $50  |   $30
  /    |     \
Mike  $75   Sarah
        \   /
         \ /
        $45
       Emma
```

---

### 6. **Expense Categories Analysis** 📈

**Backend:** Expenses have category field (FOOD, TRANSPORT, etc.)

#### Component to Build:

```typescript
// Create: src/app/features/expenses/expense-analytics/

Features:
- Pie chart of expenses by category
- Bar chart of monthly spending
- Filter by group, date range
- Export as CSV/PDF

GET /api/expenses/group/{groupId}
Filter by category in frontend:
const foodExpenses = expenses.filter(e => e.category === 'FOOD');
const transportExpenses = expenses.filter(e => e.category === 'TRANSPORT');

Sample UI:
┌─────────────────────────────────────┐
│  Expense Breakdown - Trip to Paris  │
├─────────────────────────────────────┤
│  [Pie Chart]                        │
│  🍕 Food: 40% ($480)                │
│  🚗 Transport: 30% ($360)           │
│  🏨 Accommodation: 25% ($300)       │
│  🎉 Entertainment: 5% ($60)         │
│                                     │
│  Total: $1,200                      │
│  [Export PDF] [Export CSV]          │
└─────────────────────────────────────┘
```

---

### 7. **Settlement Suggestions** 💡

**Backend:** POST /api/settlements/group/{groupId}/calculate returns optimized settlements

#### Enhancement:

```typescript
// Update: src/app/features/settlements/settlement-dialog/

Current: Manual selection of payer/payee
Enhanced: Show smart suggestions

GET /api/settlements/group/{groupId}/calculate
Response: [{
  debtorId: string,
  debtorName: string,
  creditorId: string,
  creditorName: string,
  amount: number
}]

UI Enhancement:
┌─────────────────────────────────────┐
│  Settle Up - Trip to Paris          │
├─────────────────────────────────────┤
│  💡 Smart Suggestions:              │
│                                     │
│  1️⃣ Mike pays John $150            │
│     (Recommended)                   │
│     [Record This]                   │
│                                     │
│  2️⃣ Sarah pays John $75            │
│     [Record This]                   │
│                                     │
│  Or enter custom payment:           │
│  [Manual Entry Form]                │
└─────────────────────────────────────┘
```

---

### 8. **Search & Filter Enhancements** 🔍

**Backend:** Most endpoints support filtering

#### Components to Enhance:

**8.1 Global Search Bar**

```typescript
// Add to navbar: src/app/shared/navbar/

Features:
- Search across groups, expenses, users
- Quick navigation
- Keyboard shortcuts (Ctrl+K)

APIs to use:
GET /api/groups/search?query=trip
GET /api/expenses/search?query=dinner
GET /api/users/search?query=john
```

**8.2 Advanced Expense Filters**

```typescript
// Update: src/app/features/expenses/expenses-list/

Current filters: Group, Date Range
Add filters:
- Category (FOOD, TRANSPORT, etc.)
- Amount range ($0-$50, $50-$100, etc.)
- Split type (EQUAL, EXACT, PERCENTAGE)
- Created by (user dropdown)
- Status (PENDING, SETTLED)

UI:
[Search: "dinner"]
[Group: All ▼] [Category: Food ▼] [Amount: $0-$100 ▼]
[Date: Last 30 days ▼] [Created by: All ▼]
```

---

### 9. **Mobile App Enhancements** 📱

**Current:** Responsive web design  
**Enhanced:** PWA features

#### Features to Add:

**9.1 Progressive Web App (PWA)**

```typescript
// Setup:
ng add @angular/pwa

Features:
- Install as app on mobile/desktop
- Offline support with service workers
- Push notifications (future)
- Home screen icon
- Splash screen
```

**9.2 Mobile-Specific Features**

```typescript
// Components to optimize:

- Swipe actions on expense list (swipe to delete)
- Pull-to-refresh on all lists
- Bottom sheet for mobile dialogs
- Touch-friendly larger buttons
- Mobile number keyboard for amounts
- Camera integration for receipt upload (future)
```

---

### 10. **Real-Time Features** ⚡ (ADVANCED)

**Backend:** Would need WebSocket support

#### Future Enhancement:

```typescript
// When backend adds WebSocket:

Features:
- Live expense updates
- Real-time activity feed
- Online/offline user status
- Typing indicators in comments
- Instant settlement notifications

Technology:
- Socket.io or SockJS
- Spring WebSocket on backend
```

---

## 📊 **Backend Endpoints Reference**

### Complete API Coverage:

#### **User Service** (Port 8081)

```typescript
POST   /api/users/register
POST   /api/users/login
GET    /api/users/{id}
PUT    /api/users/{id}
GET    /api/users/{userId}/friends
POST   /api/users/{userId}/friends/{friendId}
DELETE /api/users/{userId}/friends/{friendId}
GET    /api/users/search?query={query}
```

#### **Group Service** (Port 8082)

```typescript
POST / api / groups;
GET / api / groups / user / { userId };
GET / api / groups / { id };
PUT / api / groups / { id };
DELETE / api / groups / { id };
POST / api / groups / { groupId } / members;
DELETE / api / groups / { groupId } / members / { memberId };
GET / api / groups / { groupId } / balance;
```

#### **Expense Service** (Port 8083)

```typescript
POST / api / expenses;
GET / api / expenses / my - expenses;
GET / api / expenses / group / { groupId };
GET / api / expenses / { id };
PUT / api / expenses / { id };
DELETE / api / expenses / { id };
GET / api / expenses / user / balance;
```

#### **Settlement Service** (Port 8084)

```typescript
POST   /api/settlements/group/{groupId}/calculate
GET    /api/settlements/group/{groupId}
GET    /api/settlements/user/{userId}
POST   /api/settlements
GET    /api/settlements/outstanding
POST   /api/settlements/reminders/send (Manual trigger)
```

#### **Notification Service** (Port 8085)

```typescript
// Email Endpoints:
POST   /api/notifications/payment-reminder
POST   /api/notifications/payment-received
POST   /api/notifications/group-invitation
POST   /api/notifications/test?email={email}
GET    /api/notifications/health

// Activity Endpoints:
POST   /api/activities
GET    /api/activities/group/{groupId}?page=0&size=20
GET    /api/activities/group/{groupId}/recent
GET    /api/activities/user/{userId}?page=0&size=20
GET    /api/activities/group/{groupId}/range?startDate=...&endDate=...
GET    /api/activities/group/{groupId}/count
GET    /api/activities/health
```

#### **API Gateway** (Port 8080)

All services accessible through gateway with `/service-name/` prefix

---

## 🎨 **UI/UX Improvements**

### Missing Components:

1. **Loading States**

   - Skeleton loaders for lists ✅ (Partially done)
   - Shimmer effect for cards
   - Progress bars for long operations

2. **Error States**

   - Empty state illustrations ✅ (Done)
   - Error illustrations
   - Retry mechanisms

3. **Success Feedback**

   - Toast notifications ✅ (Done)
   - Confetti animation on payment recorded
   - Success checkmark animations

4. **Accessibility**

   - ARIA labels for screen readers
   - Keyboard navigation
   - High contrast mode
   - Font size adjustments

5. **Animations**
   - Route transitions
   - List item animations
   - Micro-interactions
   - Loading spinners

---

## 🧪 **Testing Requirements**

### Unit Tests Needed:

```typescript
// Services:
-auth.service.spec.ts -
  group.service.spec.ts -
  expense.service.spec.ts -
  settlement.service.spec.ts -
  activity.service.spec.ts -
  // Components:
  dashboard.component.spec.ts -
  group -
  list.component.spec.ts -
  expense -
  form.component.spec.ts -
  settlement -
  dialog.component.spec.ts;
```

### E2E Tests Needed:

```typescript
// Critical Flows:
1. User registration → Login → Create group
2. Add expense → Split among members
3. View settlements → Record payment
4. Search users → Add to group
5. View activity feed → Filter by type
```

---

## 📦 **Package Updates Needed**

### Add Dependencies:

```json
{
  "dependencies": {
    "ngx-charts": "^20.0.0", // For analytics charts
    "chart.js": "^4.0.0", // Alternative charting
    "ng2-charts": "^5.0.0", // Chart.js wrapper
    "moment": "^2.29.4", // Date formatting
    "ngx-infinite-scroll": "^16.0.0", // Infinite scroll
    "ngx-skeleton-loader": "^8.0.0" // Skeleton loaders
  }
}
```

---

## 🚀 **Priority Implementation Order**

### **Phase 1: Critical (This Week)**

1. ✅ Activity Feed Service (Angular) - **2 hours**
2. ✅ Activity Feed Component - **3 hours**
3. ✅ Dashboard Activity Widget - **1 hour**
4. ✅ Group Detail Activity Tab - **1 hour**

**Total:** ~7 hours

### **Phase 2: High Value (Next Week)**

1. Email Preferences UI - **2 hours**
2. Settlement Suggestions Enhancement - **2 hours**
3. Enhanced Dashboard Stats - **2 hours**
4. Expense Analytics Charts - **4 hours**

**Total:** ~10 hours

### **Phase 3: Polish (Week 3)**

1. Global Search Bar - **3 hours**
2. Advanced Filters - **3 hours**
3. Mobile PWA Setup - **2 hours**
4. Animations & Transitions - **2 hours**

**Total:** ~10 hours

### **Phase 4: Testing & Quality (Week 4)**

1. Unit Tests - **8 hours**
2. E2E Tests - **8 hours**
3. Accessibility Audit - **4 hours**
4. Performance Optimization - **4 hours**

**Total:** ~24 hours

---

## 📝 **Documentation Needs**

### Update These Files:

1. ✅ `FRONTEND_API_REFERENCE.md` - Add new Activity & Notification endpoints
2. ✅ `TODO.md` - Update with new features
3. ⏳ `README.md` - Add setup instructions for new features
4. ⏳ Component documentation - JSDoc for all new components

---

## 🎯 **Success Metrics**

### Feature Completion:

- ✅ Activity Feed: 0% → 100%
- ✅ Email Notifications UI: 0% → 100%
- ⏳ Analytics Charts: 0% → 100%
- ⏳ PWA Features: 0% → 100%
- ⏳ Test Coverage: 10% → 80%

### Performance:

- ⏳ Lighthouse Score: ? → 90+
- ⏳ Bundle Size: ? → <2MB
- ⏳ First Contentful Paint: ? → <1.5s

### User Experience:

- ⏳ Mobile Responsiveness: 90% → 100%
- ⏳ Accessibility Score: ? → 95+
- ⏳ Error Handling: 80% → 100%

---

## 🎉 **Summary**

### **Must Implement (Critical):**

1. ✅ Activity Feed (uses new backend endpoints)
2. ✅ Email notification indicators
3. ⏳ Test email functionality

### **Should Implement (High Value):**

1. Settlement suggestions UI
2. Expense analytics charts
3. Enhanced dashboard statistics
4. Email preferences component

### **Nice to Have (Polish):**

1. Global search
2. PWA features
3. Advanced animations
4. Real-time updates (needs backend WebSocket)

### **Testing & Quality:**

1. Unit tests for all services
2. E2E tests for critical flows
3. Accessibility compliance
4. Performance optimization

---

**Total Estimated Work:** ~50 hours (1-2 weeks full-time)

**Status:** Backend 100% ready, Frontend 95% complete  
**Next Steps:** Start with Activity Feed implementation (Phase 1)

**Swagger Documentation:** All endpoints documented at:

- http://localhost:8081/swagger-ui/index.html (User)
- http://localhost:8082/swagger-ui/index.html (Group)
- http://localhost:8083/swagger-ui/index.html (Expense)
- http://localhost:8084/swagger-ui/index.html (Settlement)
- http://localhost:8085/swagger-ui/index.html (Notification + Activity)
- http://localhost:8086/swagger-ui/index.html (Payment)

---

**Questions?** Check `docs/SWAGGER_DOCUMENTATION.md` for complete API reference!
