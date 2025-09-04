#!/usr/bin/env bash
set -euo pipefail

# 인자 우선, 없으면 env 폴백
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

# 입력 검증(어떤게 없는지 친절하게 출력)
[ -n "${HIB}" ] || { echo "❌ HIB_DDL 경로가 비었습니다."; exit 2; }
[ -n "${FLY}" ] || { echo "❌ FLY_SCHEMA 경로가 비었습니다."; exit 2; }
[ -n "${NORMALIZER}" ] || { echo "❌ NORMALIZE_PY 경로가 비었습니다."; exit 2; }
[ -n "${DIFF_PY_PATH}" ] || { echo "❌ DIFF_PY 경로가 비었습니다."; exit 2; }

[ -f "${HIB}" ] || { echo "❌ 파일 없음: ${HIB}"; ls -la "$(dirname "${HIB}")" || true; exit 2; }
[ -f "${FLY}" ] || { echo "❌ 파일 없음: ${FLY}"; ls -la "$(dirname "${FLY}")" || true; exit 2; }
[ -f "${NORMALIZER}" ] || { echo "❌ 파일 없음: ${NORMALIZER}"; exit 2; }
[ -f "${DIFF_PY_PATH}" ] || { echo "❌ 파일 없음: ${DIFF_PY_PATH}"; exit 2; }

# 정규화
python3 "${NORMALIZER}" "${HIB}" "${FLY}"

# 요약 리포트 생성
python3 "${DIFF_PY_PATH}" "${HIB}.norm" "${FLY}.norm" "${OUT_DIR}/schema-compare-summary.md"

# diff 파일 저장 + 종료코드 관리
if diff -u "${HIB}.norm" "${FLY}.norm" > "${OUT_DIR}/schema.diff"; then
  echo "✅ 스키마 일치"
  exit 0
else
  echo "❌ 스키마 불일치 - 자세한 내용: ${OUT_DIR}/schema.diff, only-in-*.txt, schema-compare-summary.md"
  exit 1
fi
