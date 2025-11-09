# Backend Improvements Needed

**Last Updated:** November 8, 2025  
**Status:** Frontend workarounds implemented, backend fixes recommended

---

## 🐛 **Critical Issue: Activity Feed - Missing User Names**

### **Problem:**

The Activity Feed API returns `userName: null` in all activity responses, making the feed show user IDs instead of user names.

### **Current Behavior:**

```json
{
  "id": 1,
  "groupId": 1,
  "userId": "6906561a7cb0ab1c0ba9c019",
  "userName": null,  // ❌ Always null
  "activityType": "EXPENSE_CREATED",
  "description": "6906561a7cb0ab1c0ba9c019 added expense 'Iqbal' for CAD 70",
  "metadata": {...},
  "createdAt": "2025-11-06T04:22:30.123Z"
}
```

### **Expected Behavior:**

```json
{
  "id": 1,
  "groupId": 1,
  "userId": "6906561a7cb0ab1c0ba9c019",
  "userName": "John Smith",  // ✅ Actual user name
  "activityType": "EXPENSE_CREATED",
  "description": "John Smith added expense 'Iqbal' for CAD 70",
  "metadata": {...},
  "createdAt": "2025-11-06T04:22:30.123Z"
}
```

### **Impact:**

- User experience degraded (shows UUIDs instead of names)
- Frontend can't display "John added expense..." properly
- Activity feed looks technical instead of user-friendly

### **Root Cause:**

Backend service `ActivityService.java` doesn't fetch user details when creating/returning activities.

---

## 🔧 **Backend Fix Required**

### **File to Update:**

`notification-service/src/main/java/com/splitwise/notification/service/ActivityService.java`

### **Solution 1: Fetch User Name on Activity Creation (Recommended)**

When logging an activity, fetch the user's name from user-service and store it in the database:

```java
public ActivityResponse logActivity(CreateActivityRequest request) {
    // Fetch user name from user-service
    String userName = getUserName(request.getUserId());

    Activity activity = Activity.builder()
        .groupId(request.getGroupId())
        .userId(request.getUserId())
        .userName(userName)  // ✅ Store user name
        .activityType(request.getActivityType())
        .description(buildDescription(userName, request))  // Use name in description
        .metadata(request.getMetadata())
        .createdAt(LocalDateTime.now())
        .build();

    Activity saved = activityRepository.save(activity);
    return ActivityMapper.toResponse(saved);
}

private String getUserName(String userId) {
    try {
        // Call user-service to get user details
        WebClient.ResponseSpec response = webClientBuilder.build()
            .get()
            .uri("http://user-service/api/users/{userId}", userId)
            .retrieve();

        UserResponse user = response.bodyToMono(UserResponse.class).block();
        return user != null ? user.getName() : "Unknown User";
    } catch (Exception e) {
        log.error("Failed to fetch user name for userId: {}", userId, e);
        return "Unknown User";
    }
}
```

### **Solution 2: Populate on Retrieval (Less Efficient)**

Fetch user names when returning activities (causes N+1 queries):

```java
public ActivityResponse toResponse(Activity activity) {
    String userName = getUserName(activity.getUserId());

    return ActivityResponse.builder()
        .id(activity.getId())
        .groupId(activity.getGroupId())
        .userId(activity.getUserId())
        .userName(userName)  // ✅ Fetch on read
        .activityType(activity.getActivityType())
        .description(activity.getDescription())
        .metadata(activity.getMetadata())
        .createdAt(activity.getCreatedAt())
        .build();
}
```

**Note:** Solution 1 is better because:

- ✅ User name stored in DB (no extra API calls on read)
- ✅ Consistent even if user changes their name later
- ✅ Better performance (one API call vs N calls)
- ✅ Works even if user-service is down during read

---

## 📝 **Additional Improvements**

### **1. Better Description Formatting**

Current descriptions are inconsistent and show user IDs:

```
"6906561a7cb0ab1c0ba9c019 added expense 'Iqbal' for CAD 70"
"6906561a7cb0ab1c0ba9c019 added 690670ad7cb0ab1c0ba9c04c to group"
```

Should be:

```
"John Smith added expense 'Iqbal' for CAD 70"
"John Smith added Sarah Johnson to group"
```

**Fix:** Update description building logic to use user names instead of IDs:

```java
private String buildDescription(String userName, CreateActivityRequest request) {
    switch (request.getActivityType()) {
        case EXPENSE_CREATED:
            String expenseName = (String) request.getMetadata().get("expenseName");
            String amount = (String) request.getMetadata().get("amount");
            String currency = (String) request.getMetadata().get("currency");
            return String.format("%s added expense '%s' for %s %s",
                userName, expenseName, currency, amount);

        case MEMBER_ADDED:
            String addedUserName = (String) request.getMetadata().get("memberName");
            return String.format("%s added %s to group", userName, addedUserName);

        case PAYMENT_RECORDED:
            String payeeName = (String) request.getMetadata().get("payeeName");
            String paymentAmount = (String) request.getMetadata().get("amount");
            return String.format("%s paid %s to %s", userName, paymentAmount, payeeName);

        // ... other cases

        default:
            return request.getDescription();
    }
}
```

---

### **2. Include Group Name in Responses**

For user activity feeds (cross-group), include group name:

**Current:**

```json
{
  "groupId": 1,
  "description": "John added expense 'Dinner' for USD 50"
}
```

**Improved:**

```json
{
  "groupId": 1,
  "groupName": "Trip to Paris", // ✅ Add this
  "description": "John added expense 'Dinner' for USD 50"
}
```

**Backend Change:**

```java
// In ActivityResponse DTO
private String groupName;

// In ActivityService
private String getGroupName(Long groupId) {
    try {
        WebClient.ResponseSpec response = webClientBuilder.build()
            .get()
            .uri("http://group-service/api/groups/{groupId}", groupId)
            .retrieve();

        GroupResponse group = response.bodyToMono(GroupResponse.class).block();
        return group != null ? group.getName() : "Unknown Group";
    } catch (Exception e) {
        log.error("Failed to fetch group name for groupId: {}", groupId, e);
        return "Unknown Group";
    }
}
```

---

### **3. Add Activity Metadata Enrichment**

Store more context in metadata for richer descriptions:

**Current metadata:**

```json
{
  "expenseName": "Dinner",
  "amount": "50"
}
```

**Enriched metadata:**

```json
{
  "expenseName": "Dinner",
  "amount": "50",
  "currency": "USD",
  "expenseId": 123,
  "category": "FOOD",
  "paidByName": "John Smith",
  "splitCount": 4,
  "groupName": "Trip to Paris"
}
```

Benefits:

- Frontend can show detailed tooltips
- Can reconstruct activity context later
- Better for analytics and reporting

---

## 🎯 **Priority Order**

### **P0 - Critical (Fix ASAP)**

1. ✅ **Populate userName field** - Fetch from user-service and store in DB
2. ✅ **Update descriptions to use names** - Replace user IDs with actual names

### **P1 - High Priority**

3. ⏳ **Add groupName to responses** - For cross-group activity feeds
4. ⏳ **Enrich metadata** - Store additional context

### **P2 - Nice to Have**

5. ⏳ **Add activity icons** - Suggest icon names in metadata
6. ⏳ **Add deep links** - Include URLs to related resources (expense, settlement, etc.)
7. ⏳ **Add actor/target distinction** - Clearly separate who did what to whom

---

## 🔄 **Frontend Workaround (Temporary)**

Until backend is fixed, frontend hides the userName display:

**File:** `splitit-frontend/src/app/features/activities/activity-feed/activity-feed.component.html`

```html
<!-- Only show userName if available (currently always null) -->
<strong *ngIf="activity.userName">{{ activity.userName }}</strong>

<!-- Description already contains action (with user ID unfortunately) -->
<span class="description">{{ activity.description }}</span>
```

This works but shows user IDs in descriptions, which is not ideal UX.

**Note:** Frontend could fetch user names individually, but this causes N+1 API calls:

```typescript
// ❌ Don't do this - too many API calls
activities.forEach((activity) => {
  this.userService.getUserById(activity.userId).subscribe((user) => {
    activity.userName = user.name;
  });
});
```

---

## 📊 **Testing After Fix**

### **Test Cases:**

1. **Create Expense**

   ```bash
   POST /api/expenses
   # Check activity has userName populated
   GET /api/activities/group/1/recent
   # Verify: userName is "John Smith" not null
   # Verify: description is "John Smith added..." not "6906561a..."
   ```

2. **Add Member**

   ```bash
   POST /api/groups/1/members
   # Check activity description
   GET /api/activities/group/1/recent
   # Verify: "John Smith added Sarah Johnson to group"
   ```

3. **Record Payment**
   ```bash
   POST /api/settlements
   # Check activity
   GET /api/activities/group/1/recent
   # Verify: "John Smith paid $50 to Sarah Johnson"
   ```

---

## 📝 **Files to Update**

### **Backend:**

1. `notification-service/src/main/java/com/splitwise/notification/dto/ActivityResponse.java`

   - Ensure userName field exists (already there)

2. `notification-service/src/main/java/com/splitwise/notification/service/ActivityService.java`

   - Add `getUserName()` method
   - Update `logActivity()` to fetch and store userName
   - Update `buildDescription()` to use names not IDs

3. `notification-service/src/main/java/com/splitwise/notification/entity/Activity.java`

   - Ensure userName column exists in database

4. **Database Migration:**

   ```sql
   -- If userName column doesn't exist:
   ALTER TABLE activities ADD COLUMN user_name VARCHAR(255);

   -- Update existing records (one-time):
   -- This would require a script to fetch names for existing activities
   ```

### **Frontend:**

No changes needed! Once backend fixes userName, it will automatically display.

---

## 🎉 **Expected Result After Fix**

### **Before (Current):**

```
Activity Feed:
━━━━━━━━━━━━━━━━━━━━━━
💰 Expense Added
6906561a7cb0ab1c0ba9c019 added expense 'Iqbal' for CAD 70
2 days ago
━━━━━━━━━━━━━━━━━━━━━━
```

### **After (Fixed):**

```
Activity Feed:
━━━━━━━━━━━━━━━━━━━━━━
👤 John Smith
💰 Expense Added
John Smith added expense 'Iqbal' for CAD 70
2 days ago
━━━━━━━━━━━━━━━━━━━━━━
```

---

## 📞 **Contact**

If you need help implementing these fixes, refer to:

- `docs/SWAGGER_DOCUMENTATION.md` - API reference
- `docs/FRONTEND_IMPLEMENTATION_ROADMAP.md` - Frontend expectations
- User Service Swagger: http://localhost:8081/swagger-ui/index.html
- Activity Service Swagger: http://localhost:8085/swagger-ui/index.html

---

**Status:** Documented and awaiting backend implementation  
**Workaround:** Frontend hides userName field (shows description only)  
**ETA:** ~2 hours backend development time
