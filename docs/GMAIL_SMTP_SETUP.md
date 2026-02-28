# Gmail SMTP Setup Guide for SplitIt

## 📧 Setting Up Gmail App Password

To send emails through Gmail SMTP, you need to create an **App Password** (not your regular Gmail password).

### Prerequisites

- A Gmail account
- 2-Step Verification enabled on your Google Account

---

## Step-by-Step Setup

### 1. Enable 2-Step Verification

1. Go to [Google Account Security](https://myaccount.google.com/security)
2. Under "Signing in to Google", click on **2-Step Verification**
3. Follow the prompts to set up 2-Step Verification if not already enabled
4. You'll need your phone to verify

### 2. Create App Password

1. Go to [Google App Passwords](https://myaccount.google.com/apppasswords)
   - Or navigate: Google Account → Security → 2-Step Verification → App passwords
2. You might need to sign in again
3. In the "Select app" dropdown, choose **Mail**
4. In the "Select device" dropdown, choose **Other (Custom name)**
5. Enter a name like: `SplitIt Notification Service`
6. Click **Generate**
7. Google will display a 16-character password (like: `abcd efgh ijkl mnop`)
8. **Copy this password** - you won't see it again!

### 3. Configure SplitIt

#### Option A: Update .env File (Recommended)

Edit `/Users/dhruvpatel/Desktop/SplitIt/.env`:

```properties
# Gmail SMTP Configuration
MAIL_USERNAME=your-actual-email@gmail.com
MAIL_PASSWORD=abcdefghijklmnop
```

**Example:**

```properties
MAIL_USERNAME=dhruv.splitit@gmail.com
MAIL_PASSWORD=xyzw abcd efgh ijkl
```

> **Note:** Remove spaces from the App Password! Google shows it as 4 groups of 4 characters, but you should enter all 16 characters without spaces.

#### Option B: Set Environment Variables Directly

```bash
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-16-char-app-password"
```

---

## 🧪 Testing the Configuration

### 1. Rebuild and Restart Services

```bash
cd /Users/dhruvpatel/Desktop/SplitIt

# Rebuild notification-service and settlement-service
./build.sh

# Restart Docker containers with new environment variables
docker compose down
docker compose up -d
```

### 2. Check Service Logs

```bash
# Check notification-service logs
docker logs notification-service -f

# Look for successful startup without SMTP errors
```

### 3. Test Email Endpoints

#### Test 1: Send Test Email

```bash
curl -X POST http://localhost:8085/api/notifications/test \
  -H "Content-Type: application/json" \
  -d '{
    "to": "recipient@example.com",
    "subject": "Test Email from SplitIt",
    "body": "This is a test email to verify SMTP configuration."
  }'
```

#### Test 2: Payment Received Email

```bash
curl -X POST http://localhost:8085/api/notifications/payment-received \
  -H "Content-Type: application/json" \
  -d '{
    "payeeEmail": "payee@example.com",
    "payeeName": "John Doe",
    "payerName": "Jane Smith",
    "amount": 50.00,
    "currency": "USD",
    "groupName": "Weekend Trip",
    "groupId": 1,
    "paymentMethod": "UPI",
    "transactionId": "TXN123456"
  }'
```

#### Test 3: Payment Reminder Email

```bash
curl -X POST http://localhost:8085/api/notifications/payment-reminder \
  -H "Content-Type: application/json" \
  -d '{
    "debtorEmail": "debtor@example.com",
    "debtorName": "Bob Wilson",
    "creditorName": "Alice Johnson",
    "amount": 75.50,
    "currency": "USD",
    "groupName": "Dinner Party",
    "groupId": 2,
    "notes": "Please pay by end of week"
  }'
```

#### Test 4: Group Invitation Email

```bash
curl -X POST http://localhost:8085/api/notifications/group-invitation \
  -H "Content-Type: application/json" \
  -d '{
    "inviteeEmail": "newmember@example.com",
    "inviteeName": "Charlie Brown",
    "inviterName": "Admin User",
    "groupName": "Roommates Expenses",
    "groupId": 3,
    "groupDescription": "Track shared apartment expenses"
  }'
```

### 4. Test Via Settlement Service (End-to-End)

#### Record a Payment (Triggers Automatic Email)

```bash
curl -X POST http://localhost:8084/api/settlements \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user123" \
  -d '{
    "groupId": 1,
    "payerId": "user123",
    "payeeId": "user456",
    "amount": 50.00,
    "currency": "USD",
    "paymentMethod": "Bank Transfer",
    "transactionId": "TXN789012"
  }'
```

**Expected Result:** Payment received email sent to payee (user456)

#### Send Manual Payment Reminder

```bash
curl -X POST "http://localhost:8084/api/settlements/reminders/send?debtorId=user789&creditorId=user123&groupId=1&amount=75.50&currency=USD&notes=Please%20settle%20by%20Friday" \
  -H "X-User-Id: user123"
```

**Expected Result:** Payment reminder email sent to debtor (user789)

### 5. Test Weekly Digest Scheduler

The weekly digest runs **every Monday at 9:00 AM**. To test immediately:

1. Uncomment the test method in `WeeklyDigestScheduler.java`:

```java
@Scheduled(cron = "0 */2 * * * *")  // Every 2 minutes for testing
public void sendWeeklyPaymentRemindersTest() {
    log.info("TEST: Manual trigger for weekly payment reminder digest");
    sendWeeklyPaymentReminders();
}
```

2. Rebuild and restart notification-service
3. Check logs - digest will run every 2 minutes
4. After testing, comment out the test method again

---

## 🔍 Troubleshooting

### Error: "Username and Password not accepted"

**Cause:** Using regular Gmail password instead of App Password

**Solution:**

1. Make sure you've created an App Password (see Step 2 above)
2. Use the 16-character App Password, not your regular password
3. Remove any spaces from the App Password

### Error: "Failed to send email: Mail server connection failed"

**Cause:** SMTP settings incorrect or firewall blocking port 587

**Solution:**

1. Verify SMTP settings in `notification-service/application.yml`:
   ```yaml
   spring:
     mail:
       host: smtp.gmail.com
       port: 587
   ```
2. Check if port 587 is open in your firewall
3. Try port 465 with SSL (requires config change)

### Error: "Must issue a STARTTLS command first"

**Cause:** TLS not properly configured

**Solution:**
Check `application.yml` has:

```yaml
spring:
  mail:
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

### Emails Go to Spam

**Solutions:**

1. Add your Gmail address to recipient's contacts
2. Check email content for spam triggers
3. For production, consider using a custom domain with proper SPF/DKIM records
4. Use Gmail's "Report not spam" if emails are marked as spam

### No Emails Received

**Check:**

1. Docker container logs: `docker logs notification-service`
2. Settlement service logs: `docker logs settlement-service`
3. Verify recipient email address is correct
4. Check spam folder
5. Verify Gmail App Password is correct in `.env`

---

## 📊 Monitoring Email Sending

### Check Notification Service Logs

```bash
docker logs notification-service --tail 100 -f
```

Look for:

```
✅ Successfully sent payment received email to payee@example.com
✅ Successfully sent payment reminder email to debtor@example.com
✅ Successfully sent group invitation email to newmember@example.com
```

### Check Settlement Service Integration

```bash
docker logs settlement-service --tail 100 -f
```

Look for:

```
Sending payment received email for settlement...
Fetched user email: payee@example.com
Fetched group name: Weekend Trip
```

---

## 🔐 Security Best Practices

### Production Deployment

1. **Never commit .env file to Git**

   - Add `.env` to `.gitignore`
   - Use environment variables in production

2. **Use Secrets Management**

   - AWS Secrets Manager
   - Azure Key Vault
   - HashiCorp Vault
   - Kubernetes Secrets

3. **Rotate App Passwords Regularly**

   - Create new App Password
   - Update `.env`
   - Revoke old App Password

4. **Monitor Email Sending**

   - Track email delivery rates
   - Monitor for bounces
   - Set up alerts for failures

5. **Rate Limiting**
   - Gmail has daily sending limits
   - Free Gmail: 500 emails/day
   - Google Workspace: 2000 emails/day

---

## 📈 Gmail Sending Limits

### Free Gmail Account

- **Daily limit:** 500 emails per day
- **Rate limit:** ~2-3 emails per second
- **Recipients per email:** 500

### Google Workspace Account

- **Daily limit:** 2000 emails per day
- **Rate limit:** ~5 emails per second
- **Recipients per email:** 2000

### What Happens When You Hit Limits?

- Gmail returns error: "Daily sending quota exceeded"
- Service will log error but won't crash
- Emails will queue and retry next day

### For High Volume

Consider using:

- **SendGrid** - 100 emails/day free
- **Mailgun** - 5000 emails/month free
- **Amazon SES** - Very low cost, high volume
- **Postmark** - Transaction email specialist

---

## ✅ Verification Checklist

- [ ] 2-Step Verification enabled on Gmail
- [ ] App Password created and copied
- [ ] `.env` file updated with correct email and App Password
- [ ] No spaces in App Password
- [ ] Docker containers restarted with new environment
- [ ] Test email endpoint works
- [ ] Payment received email works
- [ ] Payment reminder email works
- [ ] Group invitation email works
- [ ] HTML templates display correctly
- [ ] Emails not going to spam
- [ ] Logs show successful email sending

---

## 🎉 Success!

Once all tests pass, your SplitIt application is sending beautiful HTML emails via Gmail SMTP! 🚀

Users will receive:

- 💚 **Payment Confirmations** - When they receive money
- 💜 **Payment Reminders** - When they owe money
- 💗 **Group Invitations** - When they're added to groups
- 📅 **Weekly Digests** - Every Monday at 9 AM for pending payments

---

## Support

For issues:

1. Check logs: `docker logs notification-service`
2. Review this guide's Troubleshooting section
3. Verify Gmail App Password is correct
4. Test with curl commands above
