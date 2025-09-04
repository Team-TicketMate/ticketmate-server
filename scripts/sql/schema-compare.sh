#!/usr/bin/env bash
set -euo pipefail

# 기대 env (GitHub Actions에서 job-level env로 세팅됨)
: "${HIB_DDL:?HIB_DDL env required}"
: "${FLY_SCHEMA:?FLY_SCHEMA env required}"
: "${NORMALIZE_PY:?NORMALIZE_PY env required}"

echo "🔍 스키마 정규화 & 비교를 시작합니다."
echo "   - Hibernate DDL : $HIB_DDL"
echo "   - Flyway Schema : $FLY_SCHEMA"
echo "   - Normalizer    : $NORMALIZE_PY"

# 존재 확인
test -f "$HIB_DDL"    || { echo "❌ $HIB_DDL 파일이 없습니다."; exit 1; }
test -f "$FLY_SCHEMA" || { echo "❌ $FLY_SCHEMA 파일이 없습니다."; exit 1; }
test -f "$NORMALIZE_PY" || { echo "❌ $NORMALIZE_PY 스크립트를 찾을 수 없습니다."; exit 1; }

# 정규화 & 비교 (정상일 때 0, 다르면 1)
if python3 "$NORMALIZE_PY" "$HIB_DDL" "$FLY_SCHEMA"; then
  echo "✅ 스키마 일치"
  exit 0
fi

echo "❌ 스키마가 다릅니다. 정규화 결과(diff) 출력:"
diff -u "${HIB_DDL}.norm" "${FLY_SCHEMA}.norm" || true
exit 1
