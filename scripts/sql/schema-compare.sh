#!/usr/bin/env bash
set -euo pipefail

HIB="${HIB_DDL:?HIB_DDL env가 필요합니다}"
FLY="${FLY_SCHEMA:?FLY_SCHEMA env가 필요합니다}"
NORM="${NORMALIZE_PY:?NORMALIZE_PY env가 필요합니다}"
MIG="${MIG_COUNT:-0}"

echo "🔍 스키마 정규화 & 비교를 시작합니다."
echo "   - Hibernate DDL : $HIB"
echo "   - Flyway Schema : $FLY"
echo "   - Normalizer    : $NORM"

# 마이그레이션 0개면 비교 생략
if [ "$MIG" = "0" ]; then
  echo "ℹ️ 사용자 마이그레이션이 없어 비교를 생략합니다. (MIG_COUNT=0)"
  exit 0
fi

# 파일 체크
[ -f "$HIB" ] || { echo "❌ 파일 없음: $HIB"; exit 1; }
[ -s "$HIB" ] || { echo "❌ 파일이 비어있음: $HIB"; exit 1; }
[ -f "$FLY" ] || { echo "❌ 파일 없음: $FLY"; exit 1; }
[ -s "$FLY" ] || { echo "❌ 파일이 비어있음: $FLY"; exit 1; }

python3 "$NORM" "$HIB" "$FLY" && {
  echo "✅ 스키마 일치"
  exit 0
}

echo "❌ 스키마가 다릅니다. 정규화 결과(diff) 출력:"
diff -u "${HIB}.norm" "${FLY}.norm" || true
exit 1
