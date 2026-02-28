#!/bin/bash
BASE_DOMAIN="delightfulfield-e71e7e6d.eastus.azurecontainerapps.io"
SERVICES=(
  "user-service:8081"
  "group-service:8082"
  "expense-service:8083"
  "settlement-service:8085"
  "payment-service:8086"
  "notification-service:8087"
  "analytics-service:8088"
)

echo "=== Warming up all services (90s timeout each) ==="
for entry in "${SERVICES[@]}"; do
  svc="${entry%%:*}"
  url="https://${svc}.${BASE_DOMAIN}/actuator/health"
  echo -n "  $svc ... "
  resp=$(curl -s --max-time 90 "$url")
  status=$(echo "$resp" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('status','?'))" 2>/dev/null)
  db=$(echo "$resp" | python3 -c "import sys,json; d=json.load(sys.stdin); c=d.get('components',{}); db=c.get('db',c.get('mongo',{})); print(db.get('status','N/A'))" 2>/dev/null)
  echo "$status (db: $db)"
done

echo ""
echo "=== Eureka Registry ==="
curl -s --max-time 30 "https://discovery-server.${BASE_DOMAIN}/eureka/apps" \
  -H "Accept: application/json" | \
  python3 -c "
import sys, json
data = json.load(sys.stdin)
apps = data.get('applications', {}).get('application', [])
for a in apps:
    print(f\"  {a['name']}: {len(a['instance'])} instance(s)\")
if not apps:
    print('  (no services registered)')
"
