#!/usr/bin/env bash
set -euo pipefail

HIB="ticketmate-api/build/generated/hibernate-ddl.sql"
FLY="ticketmate-api/build/generated/flyway-schema.sql"

python3 scripts/normalize_sql.py "$HIB" "$FLY" && {
  echo "✅ Schema match"
  exit 0
}

echo "❌ Schema mismatch"
diff -u "${HIB}.norm" "${FLY}.norm" || true
exit 1
