#!/usr/bin/env bash
set -euo pipefail
ROOT="${1:-ticketmate-api/src/main/resources/db/migration}"
bad=0
while IFS= read -r -d '' f; do
  base=$(basename "$f")
  if [[ ! "$base" =~ ^V[0-9]{8}_[0-9]{6}__[a-z0-9_]+\.sql$ ]]; then
    echo "❌ 잘못된 파일명: $f"
    bad=1
  fi
done < <(find "$ROOT" -type f -name '*.sql' -print0)
exit $bad
