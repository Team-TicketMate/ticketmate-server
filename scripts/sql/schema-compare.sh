#!/usr/bin/env bash
set -euo pipefail

# ⬇️ 워크플로 env에서 주입됨
: "${GEN_DIR:?}"; : "${HIB_DDL:?}"; : "${FLY_SCHEMA:?}"
: "${NORMALIZE_PY:?}"; : "${DIFF_PY:?}"

HIB_NORM="${HIB_DDL}.norm"
FLY_NORM="${FLY_SCHEMA}.norm"

echo "🔍 스키마 비교를 시작합니다 (SRP 분리: 정규화 → 비교)"
echo "   - Hibernate DDL : $HIB_DDL"
echo "   - Flyway Schema : $FLY_SCHEMA"

# 1) 정규화 (각각 독립 실행)
python3 "$NORMALIZE_PY" "$HIB_DDL" "$HIB_NORM"
python3 "$NORMALIZE_PY" "$FLY_SCHEMA" "$FLY_NORM"

# 2) 비교 리포트 생성 (diff/요약/only-in-*.txt)
if python3 "$DIFF_PY" "$HIB_NORM" "$FLY_NORM" "$GEN_DIR"; then
  echo "✅ 스키마 일치"
  if [ -n "${GITHUB_STEP_SUMMARY:-}" ] && [ -f "$GEN_DIR/schema-compare-summary.md" ]; then
    {
      echo "## 스키마 비교 결과"
      echo ""
      echo "✅ **일치합니다**"
    } >> "$GITHUB_STEP_SUMMARY"
  fi
  exit 0
fi

# 불일치 시, 요약/일부 diff를 보여주고 실패
echo "❌ 스키마가 다릅니다."
if [ -f "$GEN_DIR/schema-compare-summary.md" ]; then
  echo "----- 요약 (상위 항목) -----"
  sed -n '1,120p' "$GEN_DIR/schema-compare-summary.md" || true
fi
if [ -f "$GEN_DIR/schema.diff" ]; then
  echo "----- unified diff (상위 200줄) -----"
  sed -n '1,200p' "$GEN_DIR/schema.diff" || true
fi

if [ -n "${GITHUB_STEP_SUMMARY:-}" ] && [ -f "$GEN_DIR/schema-compare-summary.md" ]; then
  {
    echo "## 스키마 비교 결과 ❌ 불일치"
    echo ""
    echo "- 원본/정규화/차이 파일은 **Artifacts: \`schema-compare-artifacts\`**로 다운로드 가능"
    echo ""
    cat "$GEN_DIR/schema-compare-summary.md"
  } >> "$GITHUB_STEP_SUMMARY"
fi

exit 1
