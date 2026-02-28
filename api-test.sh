#!/bin/bash
set -e
GW="https://api-gateway.delightfulfield-e71e7e6d.eastus.azurecontainerapps.io"
OPTS="-s --max-time 90"

echo "========================================"
echo "  SplitIt Live API End-to-End Test"
echo "========================================"

# 1. Register
echo ""
echo "1. Register user..."
REGISTER=$(curl $OPTS -X POST "$GW/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"testuser@splitit.com","password":"Test@1234"}')
echo "   $REGISTER" | head -c 200
echo ""

# 2. Login
echo ""
echo "2. Login..."
LOGIN=$(curl $OPTS -X POST "$GW/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"testuser@splitit.com","password":"Test@1234"}')
echo "   $LOGIN" | head -c 300
echo ""

TOKEN=$(echo "$LOGIN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('token','') or d.get('accessToken','') or d.get('jwt',''))" 2>/dev/null)
if [ -z "$TOKEN" ]; then
  echo "   WARNING: Could not extract token, trying alternate fields..."
  TOKEN=$(echo "$LOGIN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(list(d.values())[0] if d else '')" 2>/dev/null)
fi
echo "   Token: ${TOKEN:0:40}..."

# 3. Get profile (authenticated)
echo ""
echo "3. Get current user profile (auth required)..."
PROFILE=$(curl $OPTS -H "Authorization: Bearer $TOKEN" "$GW/api/users/me")
echo "   $PROFILE" | head -c 200
echo ""

# 4. Create group (authenticated)
echo ""
echo "4. Create a group..."
GROUP=$(curl $OPTS -X POST "$GW/api/groups" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Weekend Trip","description":"Test group","category":"TRIP"}')
echo "   $GROUP" | head -c 200
echo ""

GROUP_ID=$(echo "$GROUP" | python3 -c "import sys,json; d=json.load(sys.stdin); data=d.get('data',d); print(data.get('id','') or data.get('groupId',''))" 2>/dev/null)
echo "   Group ID: $GROUP_ID"

# Get user ID from profile
USER_ID=$(echo "$PROFILE" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('id','') or d.get('userId',''))" 2>/dev/null)
echo "   User ID: $USER_ID"

# 5. Add an expense
if [ -n "$GROUP_ID" ] && [ -n "$USER_ID" ]; then
  echo ""
  echo "5. Add expense to group..."
  EXPENSE=$(curl $OPTS -X POST "$GW/api/expenses" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"groupId\":$GROUP_ID,\"description\":\"Dinner\",\"amount\":60.00,\"currency\":\"USD\",\"paidBy\":\"$USER_ID\",\"splitType\":\"EQUAL\",\"participantIds\":[\"$USER_ID\"]}")
  echo "   $EXPENSE" | head -c 300
  echo ""
else
  echo "5. Skipping expense (no group ID or user ID)"
fi

echo ""
echo "========================================"
echo " Test complete"
echo "========================================"
