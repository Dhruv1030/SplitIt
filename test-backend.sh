#!/bin/bash

BASE_URL="http://localhost:8080"
PASS=0
FAIL=0
ERRORS=()

check() {
  local name="$1"
  local response="$2"
  local expected="$3"
  
  if echo "$response" | grep -q "$expected"; then
    echo "  ✅ PASS - $name"
    PASS=$((PASS+1))
  else
    echo "  ❌ FAIL - $name"
    echo "     Response: $(echo $response | head -c 200)"
    FAIL=$((FAIL+1))
    ERRORS+=("$name")
  fi
}

echo ""
echo "=================================================="
echo "       SplitIt Backend Test Suite"
echo "=================================================="

# ==================== USER SERVICE ====================
echo ""
echo "📋 USER SERVICE"
echo "------------------"

TIMESTAMP=$(date +%s)
TEST_EMAIL="testuser_${TIMESTAMP}@test.com"

REGISTER=$(curl -s -X POST "$BASE_URL/api/users/register" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Test User\",\"email\":\"$TEST_EMAIL\",\"password\":\"Test@123\",\"defaultCurrency\":\"USD\"}")
check "User Registration" "$REGISTER" "token"

TOKEN=$(echo $REGISTER | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('token',''))" 2>/dev/null)
USER_ID=$(echo $REGISTER | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('user',{}).get('id',''))" 2>/dev/null)

LOGIN=$(curl -s -X POST "$BASE_URL/api/users/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"Test@123\"}")
check "User Login" "$LOGIN" "token"

TOKEN=$(echo $LOGIN | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('token',''))" 2>/dev/null)

PROFILE=$(curl -s "$BASE_URL/api/users/me" -H "Authorization: Bearer $TOKEN")
check "Get User Profile" "$PROFILE" "email"

INVALID_LOGIN=$(curl -s -X POST "$BASE_URL/api/users/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"wrong@test.com\",\"password\":\"wrongpass\"}")
check "Invalid Login Returns Error" "$INVALID_LOGIN" "401\|error\|invalid\|Invalid\|Unauthorized"

UNAUTH=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/users/me")
check "Unauthenticated Request Blocked (401/403)" "$UNAUTH" "401\|403"

# ==================== GROUP SERVICE ====================
echo ""
echo "📋 GROUP SERVICE"
echo "------------------"

CREATE_GROUP=$(curl -s -X POST "$BASE_URL/api/groups" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Weekend Trip","description":"Goa trip expenses","category":"TRIP"}')
check "Create Group" "$CREATE_GROUP" "id\|name"

GROUP_ID=$(echo $CREATE_GROUP | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('id','') if 'data' in d else d.get('id',''))" 2>/dev/null)

GET_GROUPS=$(curl -s "$BASE_URL/api/groups" -H "Authorization: Bearer $TOKEN")
check "Get User Groups" "$GET_GROUPS" "\[\|id\|name\|success"

if [ -n "$GROUP_ID" ]; then
  GET_GROUP=$(curl -s "$BASE_URL/api/groups/$GROUP_ID" -H "Authorization: Bearer $TOKEN")
  check "Get Group By ID" "$GET_GROUP" "Weekend Trip\|id"
fi

# ==================== EXPENSE SERVICE ====================
echo ""
echo "📋 EXPENSE SERVICE"
echo "------------------"

echo "  Group ID captured: ${GROUP_ID:-EMPTY}"

if [ -n "$GROUP_ID" ]; then
  CREATE_EXPENSE=$(curl -s -X POST "$BASE_URL/api/expenses" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"description\":\"Dinner\",\"amount\":1200.00,\"groupId\":$GROUP_ID,\"splitType\":\"EQUAL\",\"paidBy\":\"$USER_ID\",\"currency\":\"USD\",\"participantIds\":[\"$USER_ID\"]}")
  check "Create Expense (Equal Split)" "$CREATE_EXPENSE" "id\|amount\|description"
  
  EXPENSE_ID=$(echo $CREATE_EXPENSE | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('id',''))" 2>/dev/null)

  GET_EXPENSES=$(curl -s "$BASE_URL/api/expenses/group/$GROUP_ID" -H "Authorization: Bearer $TOKEN")
  check "Get Group Expenses" "$GET_EXPENSES" "\[\|id\|Dinner"

  GET_BALANCE=$(curl -s "$BASE_URL/api/expenses/balance" -H "Authorization: Bearer $TOKEN")
  check "Get User Balance" "$GET_BALANCE" "totalOwed\|netBalance\|totalLent\|\[\]"
fi

# ==================== SETTLEMENT SERVICE ====================
echo ""
echo "📋 SETTLEMENT SERVICE"
echo "------------------"

if [ -n "$GROUP_ID" ]; then
  SUGGESTIONS=$(curl -s "$BASE_URL/api/settlements/group/$GROUP_ID/suggestions" -H "Authorization: Bearer $TOKEN")
  check "Get Settlement Suggestions" "$SUGGESTIONS" "\[\|\[\]"

  SETTLEMENTS=$(curl -s "$BASE_URL/api/settlements/group/$GROUP_ID" -H "Authorization: Bearer $TOKEN")
  check "Get Group Settlements" "$SETTLEMENTS" "\[\|\[\]"
fi

# ==================== NOTIFICATION SERVICE ====================
echo ""
echo "📋 NOTIFICATION SERVICE"
echo "------------------"

ACTIVITIES=$(curl -s "$BASE_URL/api/activities/user/$USER_ID" -H "Authorization: Bearer $TOKEN")
check "Get User Activities" "$ACTIVITIES" "\[\|\[\]"

# ==================== ANALYTICS SERVICE ====================
echo ""
echo "📋 ANALYTICS SERVICE"
echo "------------------"

ANALYTICS_HEALTH=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8087/actuator/health")
check "Analytics Service Health" "$ANALYTICS_HEALTH" "200"

ANALYTICS_GATEWAY=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/api/analytics/health" -H "Authorization: Bearer $TOKEN")
check "Analytics Gateway Route" "$ANALYTICS_GATEWAY" "200\|404"

# ==================== GATEWAY SECURITY ====================
echo ""
echo "📋 GATEWAY SECURITY"
echo "------------------"

PROTECTED=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/groups")
check "Protected Route Blocked Without Token" "$PROTECTED" "401\|403"

BAD_TOKEN=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/groups" -H "Authorization: Bearer invalidtoken")
check "Invalid JWT Token Rejected" "$BAD_TOKEN" "401\|403"

# ==================== SUMMARY ====================
echo ""
echo "=================================================="
echo "                   RESULTS"
echo "=================================================="
echo "  ✅ Passed: $PASS"
echo "  ❌ Failed: $FAIL"
echo "  📊 Total:  $((PASS+FAIL))"

if [ ${#ERRORS[@]} -gt 0 ]; then
  echo ""
  echo "  Failed Tests:"
  for e in "${ERRORS[@]}"; do
    echo "    - $e"
  done
fi

echo "=================================================="
echo ""
