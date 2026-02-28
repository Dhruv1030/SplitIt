# SplitIt - Quick Start Guide 🚀

## 📧 Email Notifications & Weekly Digest - Quick Reference

### ⚡ Quick Setup (3 Steps)

#### 1. Add Gmail Credentials

Edit `.env` file:

```properties
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-16-char-app-password
```

Get App Password: https://myaccount.google.com/apppasswords

#### 2. Restart Services

```bash
docker compose restart notification-service
```

#### 3. Test

```bash
# Test notification service health
curl http://localhost:8085/api/notifications/health

# Test outstanding settlements (for weekly digest)
curl http://localhost:8084/api/settlements/outstanding
```

---

## 📋 Email Types

| Type                 | Trigger          | Recipient   | Template Color |
| -------------------- | ---------------- | ----------- | -------------- |
| **Payment Received** | Payment recorded | Payee       | 💚 Green       |
| **Payment Reminder** | Manual or Weekly | Debtor      | 💜 Purple      |
| **Group Invitation** | Member added     | New member  | 💗 Pink        |
| **Weekly Digest**    | Monday 9 AM      | All debtors | 💜 Purple      |

---

## 🔗 API Endpoints

### Email Notifications (Port 8085)

```bash
# Health Check
GET http://localhost:8085/api/notifications/health

# Send Test Email
POST http://localhost:8085/api/notifications/test
{
  "to": "test@example.com",
  "subject": "Test",
  "body": "Test message"
}

# Send Payment Received Email
POST http://localhost:8085/api/notifications/payment-received
{
  "payeeEmail": "payee@example.com",
  "payeeName": "John Doe",
  "payerName": "Jane Smith",
  "amount": 50.00,
  "currency": "USD",
  "groupName": "Weekend Trip",
  "groupId": 1,
  "paymentMethod": "UPI",
  "transactionId": "TXN123456"
}

# Send Payment Reminder Email
POST http://localhost:8085/api/notifications/payment-reminder
{
  "debtorEmail": "debtor@example.com",
  "debtorName": "Bob Wilson",
  "creditorName": "Alice Johnson",
  "amount": 75.50,
  "currency": "USD",
  "groupName": "Dinner Party",
  "groupId": 2,
  "notes": "Please pay by end of week"
}

# Send Group Invitation Email
POST http://localhost:8085/api/notifications/group-invitation
{
  "inviteeEmail": "newmember@example.com",
  "inviteeName": "Charlie Brown",
  "inviterName": "Admin User",
  "groupName": "Roommates Expenses",
  "groupId": 3,
  "groupDescription": "Track shared apartment expenses"
}
```

### Settlement Service (Port 8084)

```bash
# Get Outstanding Settlements (for weekly digest)
GET http://localhost:8084/api/settlements/outstanding

# Send Manual Payment Reminder
POST http://localhost:8084/api/settlements/reminders/send?debtorId=user789&creditorId=user123&groupId=1&amount=75.50&currency=USD&notes=Please%20settle
Headers: X-User-Id: user123

# Record Settlement (auto-sends payment received email)
POST http://localhost:8084/api/settlements
Headers: X-User-Id: user123, Content-Type: application/json
{
  "groupId": 1,
  "payerId": "user123",
  "payeeId": "user456",
  "amount": 50.00,
  "currency": "USD",
  "paymentMethod": "Bank Transfer",
  "transactionId": "TXN789012"
}
```

### Group Service (Port 8082)

```bash
# Add Member (auto-sends invitation email)
POST http://localhost:8082/api/groups/1/members
Headers: X-User-Id: admin123, Content-Type: application/json
{
  "userId": "newuser456",
  "role": "MEMBER"
}
```

---

## 📅 Weekly Digest Schedule

- **When**: Every Monday at 9:00 AM
- **What**: Sends payment reminders to all users with outstanding debts
- **How**: Queries `GET /api/settlements/outstanding` and emails each debtor

### Enable Test Mode (Run Every 2 Minutes)

Edit `WeeklyDigestScheduler.java`:

```java
@Scheduled(cron = "0 */2 * * * *")  // Uncomment this line
public void sendWeeklyPaymentRemindersTest() {
    log.info("TEST: Manual trigger");
    sendWeeklyPaymentReminders();
}
```

Rebuild: `mvn clean install -DskipTests`  
Restart: `docker compose restart notification-service`

---

## 🐛 Troubleshooting

### Check Service Logs

```bash
docker logs notification-service -f
docker logs settlement-service -f
```

### Common Issues

**"Username and Password not accepted"**

- Use Gmail App Password, not regular password
- Remove spaces from the 16-character password

**"No emails received"**

- Check spam folder
- Verify credentials in `.env`
- Check logs: `docker logs notification-service`

**"Weekly digest not running"**

- Check if Monday 9 AM has passed
- Enable test mode (every 2 minutes)
- Check logs for scheduler messages

---

## 📊 Service Status

```bash
# Check all services
docker ps

# Check specific service
docker ps --filter "name=notification-service"

# Restart service
docker compose restart notification-service
docker compose restart settlement-service

# View logs
docker logs notification-service --tail 100 -f
```

---

## 📁 Important Files

| File                                   | Purpose                            |
| -------------------------------------- | ---------------------------------- |
| `.env`                                 | Gmail credentials (add yours here) |
| `docker-compose.yml`                   | Docker configuration               |
| `docs/GMAIL_SMTP_SETUP.md`             | Detailed setup guide               |
| `docs/WEEKLY_DIGEST_IMPLEMENTATION.md` | Implementation details             |
| `docs/EMAIL_INTEGRATION_COMPLETED.md`  | Email integration overview         |

---

## 🎯 Current Status

✅ **Implemented:**

- Activity Feed Service (7 endpoints)
- Email Notifications Service (4 email types)
- Settlement Service email integration
- Group Service email integration
- Weekly Digest Scheduler (every Monday 9 AM)
- Gmail SMTP configuration ready

⏳ **Pending:**

- Add your Gmail credentials to `.env`
- Test emails with real Gmail account

---

## 🔥 Quick Commands

```bash
# Build all services
mvn clean install -DskipTests

# Start all containers
docker compose up -d

# Restart email services
docker compose restart notification-service settlement-service

# Check logs
docker logs notification-service -f

# Test outstanding settlements
curl http://localhost:8084/api/settlements/outstanding

# Test health
curl http://localhost:8085/api/notifications/health
```

---

## 📞 Support

For detailed help, see:

- **Setup**: `docs/GMAIL_SMTP_SETUP.md`
- **Implementation**: `docs/WEEKLY_DIGEST_IMPLEMENTATION.md`
- **Logs**: `docker logs notification-service`

---

**Ready to send emails! 🎉 Just add your Gmail credentials and test!** 📧
