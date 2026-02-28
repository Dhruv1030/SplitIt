# Expense Service Timeout Fix

**Date:** February 28, 2026

## Problem

After deploying to Azure Container Apps, `POST /api/expenses` requests via the API Gateway hung indefinitely and timed out, while other endpoints (login, profile, group creation) worked fine.

## Root Cause

The `ActivityClient` in expense-service made a **synchronous `RestTemplate` call** to `http://notification-service:8085/api/activities` after every expense creation. Two issues:

1. **Unresolvable hostname** — `notification-service:8085` is a Docker Compose hostname that doesn't exist in Azure Container Apps. The DNS lookup hung instead of failing fast.
2. **No timeout configured** — The `RestTemplate` was created with `new RestTemplate()` (infinite default timeout), so the blocked call never returned.
3. **Synchronous execution** — The activity logging ran on the request thread, meaning the client waited for it to complete before getting a response.

The same pattern existed in **group-service** and **settlement-service**, though group-service happened to fail fast due to DNS behavior differences between containers.

## Fix

Applied to `expense-service`, `group-service`, and `settlement-service`:

- **`@Async` on `logActivity()`** — Activity logging now runs in a background thread and never blocks the HTTP response.
- **RestTemplate timeouts** — 3s connect / 5s read timeout via `RestTemplateBuilder.setConnectTimeout()` / `setReadTimeout()`.
- **Configurable URL** — Replaced the hardcoded `http://notification-service:8085` with a `@Value("${activity.service.url}")` property, set via Azure env var to the correct internal hostname.
- **`@EnableAsync`** added to each Application class.

## Files Changed

- `expense-service/.../ExpenseServiceApplication.java`
- `expense-service/.../client/ActivityClient.java`
- `group-service/.../GroupServiceApplication.java`
- `group-service/.../client/ActivityClient.java`
- `settlement-service/.../SettlementServiceApplication.java`
- `settlement-service/.../client/ActivityClient.java`

## Azure Env Var

Set on all three services:

```
ACTIVITY_SERVICE_URL=http://notification-service.internal.delightfulfield-e71e7e6d.eastus.azurecontainerapps.io
```
