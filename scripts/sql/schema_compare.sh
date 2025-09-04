#!/usr/bin/env bash
set -euo pipefail

# ── 1) 인자 우선, 없으면 env 폴백 ───────────────────────────────────────────────
HIB="${1:-${HIB_DDL:-}}"
FLY="${2:-${FLY_SCHEMA:-}}"
NORMALIZER="${3:-${NORMALIZE_PY:-}}"
DIFF_PY_PATH="${4:-${DIFF_PY:-}}"
OUT_DIR="${GEN_DIR:-$(dirname "${HIB:-.}")}"

echo "🔍 스키마 비교를 시작합니다 (정규화 → 비교)"
echo "   - Hibernate DDL : ${HIB}"
echo "   - Flyway Schema : ${FLY}"
echo "   - Normalizer    : ${NORMALIZER}"
echo "   - Diff Maker    : ${DIFF_PY_PATH}"
echo "   - Out Dir       : ${OUT_DIR}"

# ── 2) 입력/존재 검증 (무엇이 비었는지 명확히) ───────────────────────────────
[ -n "${HIB}" ] || { echo "❌ HIB_DDL 경로가 비었습니다."; exit 2; }
[ -n "${FLY}" ] || { echo "❌ FLY_SCHEMA 경로가 비었습니다."; exit 2; }
[ -n "${NORMALIZER}" ] || { echo "❌ NORMALIZE_PY 경로가 비었습니다."; exit 2; }
[ -n "${DIFF_PY_PATH}" ] || { echo "❌ DIFF_PY 경로가 비었습니다."; exit 2; }

[ -f "${HIB}" ] || { echo "❌ 파일 없음: ${HIB}"; ls -la "$(dirname "${HIB}")" || true; exit 2; }
[ -f "${FLY}" ] || { echo "❌ 파일 없음: ${FLY}"; ls -la "$(dirname "${FLY}")" || true; exit 2; }
[ -f "${NORMALIZER}" ] || { echo "❌ 파일 없음: ${NORMALIZER}"; exit 2; }
[ -f "${DIFF_PY_PATH}" ] || { echo "❌ 파일 없음: ${DIFF_PY_PATH}"; exit 2; }

mkdir -p "${OUT_DIR}"

# ── 3) 정규화 (항상 수행) ─────────────────────────────────────────────────────
python3 "${NORMALIZER}" "${HIB}" "${FLY}"

# ── 4) 요약 리포트/only-in 목록/통합 diff 파일을 항상 생성 ────────────────────
# 요약 리포트
python3 "${DIFF_PY_PATH}" "${HIB}.norm" "${FLY}.norm" "${OUT_DIR}/schema-compare-summary.md" || true

# only-in 리스트 (정렬 후 차집합)
# (정렬이 normalization 결과 순서를 고정해줘요)
sort "${HIB}.norm" > "${OUT_DIR}/hib.tmp"
sort "${FLY}.norm" > "${OUT_DIR}/fly.tmp"
comm -23 "${OUT_DIR}/hib.tmp" "${OUT_DIR}/fly.tmp" > "${OUT_DIR}/only-in-hibernate.txt" || true
comm -13 "${OUT_DIR}/hib.tmp" "${OUT_DIR}/fly.tmp" > "${OUT_DIR}/only-in-flyway.txt" || true
rm -f "${OUT_DIR}/hib.tmp" "${OUT_DIR}/fly.tmp"

# 통합 diff (파일로 저장)
set +e
diff -u "${HIB}.norm" "${FLY}.norm" > "${OUT_DIR}/schema.diff"
DIFF_RC=$?
set -e

if [ ${DIFF_RC} -eq 0 ]; then
  echo "✅ 스키마 일치"
  exit 0
else
  echo "❌ 스키마 불일치 - 자세한 내용:"
  echo "   - ${OUT_DIR}/schema.diff"
  echo "   - ${OUT_DIR}/only-in-hibernate.txt"
  echo "   - ${OUT_DIR}/only-in-flyway.txt"
  echo "   - ${OUT_DIR}/schema-compare-summary.md"
  exit 1
fi
