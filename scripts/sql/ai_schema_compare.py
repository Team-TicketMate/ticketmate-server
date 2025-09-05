#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import json
import os
import pathlib
import re
import sys
import time

# Vertex AI
try:
  import vertexai
  from vertexai.generative_models import GenerativeModel, GenerationConfig
except Exception as e:
  print(
      "ERROR: google-cloud-aiplatform 패키지가 필요합니다. pip install google-cloud-aiplatform",
      file=sys.stderr,
  )
  raise

KNOWN_FALSE_POSITIVES = """
- Ignore the Flyway internal table (flyway_schema_history) entirely.
- Ignore ownership/privileges and session/setup statements (e.g., OWNER TO, GRANT/REVOKE, SET, search_path).
- Treat automatically generated names for constraints/indexes as irrelevant (name differences do not matter).
- Ignore case, quoting, whitespace, newlines, and semicolons.
- Treat equivalent type spellings as the same (e.g., varchar vs character varying; timestamptz vs timestamp with time zone).
- Ignore extensions and comments (CREATE EXTENSION, COMMENT ON).
- Ignore cosmetic reformatting of CHECK constraints (extra parentheses/spacing).
""".strip()

SYSTEM_RULES = f"""
You are a PostgreSQL DDL schema comparison assistant. Compare two schemas **logically** (semantic equivalence),
not textually. Your **only** output must be a single JSON object with this exact shape:

{{
  "match": "yes" | "no",
  "reasons": ["reason 1", "reason 2", ...],      // non-empty only when match == "no"
  "suggestions": ["actionable improvement 1", ...] // ALWAYS return 3-10 concise improvements for the Flyway DDL
}}

Rules for logical equivalence:
- Focus only on user-domain objects and their semantics (tables, columns, data types, nullability, defaults,
  primary keys, unique constraints, foreign keys, check constraints, and indexes).
- Consider the following **non-differences** (treat as equal, do not fail on them):
{KNOWN_FALSE_POSITIVES}

Improvement guidance (for "suggestions"):
- Always write suggestions targeted at **the Flyway DDL** (Schema B), even when match == "yes".
- Focus on production-ready migration best practices: idempotency, transactional safety, explicit data types/precision,
  naming conventions (snake_case, constraint/index names), deterministic default values, NOT NULL + CHECK coverage,
  index strategy for FKs & unique lookups, enum/check design, timestamp/timezone choices, extension usage hygiene,
  and safe roll-forward/rollback considerations.
- Keep each suggestion short (<= 160 chars) and actionable.

Important:
- If schemas are logically equivalent, return: {{ "match": "yes", "reasons": [], "suggestions": [...] }}
- If there is any semantic difference, return: {{ "match": "no", "reasons": [ ... ], "suggestions": [...] }}
- Do **not** include backticks or code fences. Output **pure JSON only**.
""".strip()

PROMPT_TMPL = """# Schema A (Hibernate-generated DDL)
{sql_a}

# Schema B (Flyway-applied DDL)
{sql_b}
"""


def read_text(path: str, max_bytes: int = 800_000) -> str:
  p = pathlib.Path(path)
  if not p.exists():
    print(f"input file not found: {path}", file=sys.stderr)
    sys.exit(2)
  data = p.read_bytes()
  if len(data) > max_bytes:
    head = data[: max_bytes // 2]
    tail = data[-(max_bytes // 2) :]
    note = f"\n-- [TRUNCATED for length: kept head/tail of {len(data)} bytes] --\n"
    return head.decode("utf-8", "ignore") + note + tail.decode("utf-8", "ignore")
  return data.decode("utf-8", "ignore")


def _extract_json_loose(s: str) -> str | None:
  """느슨한 JSON 추출: 코드펜스/접두 텍스트가 섞인 경우 복원."""
  if not s:
    return None
  s = s.strip()
  s = re.sub(r"^```(?:json)?\s*", "", s)
  s = re.sub(r"\s*```$", "", s)
  first = s.find("{")
  last = s.rfind("}")
  if first != -1 and last != -1 and last > first:
    return s[first : last + 1]
  return None


def _fallback(val: str | None, default: str) -> str:
  """빈 문자열/공백도 기본값으로 대체."""
  if val is None:
    return default
  v = str(val).strip()
  return v if v else default


def main():
  ap = argparse.ArgumentParser()
  ap.add_argument("hibernate_sql", help="hibernate-ddl.sql 경로")
  ap.add_argument("flyway_sql", help="flyway-schema.sql 경로")
  ap.add_argument("--out_dir", default=None, help="산출물 디렉터리 (기본: 입력 파일 폴더)")
  ap.add_argument("--model", default=os.environ.get("VERTEX_MODEL"))
  ap.add_argument("--project", default=os.environ.get("VERTEX_PROJECT_ID"))
  ap.add_argument("--location", default=os.environ.get("VERTEX_LOCATION"))
  args = ap.parse_args()

  hib = pathlib.Path(args.hibernate_sql)
  fly = pathlib.Path(args.flyway_sql)
  out_dir = pathlib.Path(args.out_dir) if args.out_dir else hib.parent
  out_dir.mkdir(parents=True, exist_ok=True)

  # 입력 로드
  sql_a = read_text(str(hib))
  sql_b = read_text(str(fly))

  # 안전한 기본값 적용
  effective_model = _fallback(args.model, "gemini-1.5-pro-002")
  effective_location = _fallback(args.location, "us-central1")

  if not args.project:
    print("ERROR: VERTEX_PROJECT_ID 환경변수 또는 --project 인자가 필요합니다.", file=sys.stderr)
    sys.exit(2)

  # Vertex 초기화
  vertexai.init(project=args.project, location=effective_location)

  # 시스템 프롬프트는 system_instruction으로 전달
  model = GenerativeModel(effective_model, system_instruction=SYSTEM_RULES)
  prompt = PROMPT_TMPL.format(sql_a=sql_a, sql_b=sql_b)

  gen_cfg = GenerationConfig(
      temperature=0.0,
      top_p=0.1,
      top_k=32,
      max_output_tokens=1024,
      response_mime_type="application/json",
  )

  last_err = None
  for attempt in range(3):
    try:
      # 문자열 하나로 호출 (파트/컨텐츠 오브젝트 불필요)
      resp = model.generate_content(prompt, generation_config=gen_cfg)

      # 원시 응답 보존
      raw_txt = ""
      if hasattr(resp, "text") and resp.text:
        raw_txt = resp.text
      else:
        try:
          raw_txt = "".join(
              p.text for p in resp.candidates[0].content.parts if hasattr(p, "text")
          )
        except Exception:
          raw_txt = ""

      (out_dir / "ai_raw_response.txt").write_text(raw_txt or "", encoding="utf-8")

      txt = (raw_txt or "").strip()
      if not txt:
        raise RuntimeError("빈 응답 수신")

      # JSON 파싱(느슨한 복원 포함)
      try:
        data = json.loads(txt)
      except Exception:
        loose = _extract_json_loose(txt)
        if not loose:
          raise
        data = json.loads(loose)

      match = str(data.get("match", "")).strip().lower()
      reasons = data.get("reasons", [])
      suggestions = data.get("suggestions", [])  # [ADDED]

      # 산출물 저장
      (out_dir / "ai_schema_compare.json").write_text(
          json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8"
      )
      (out_dir / "ai_verdict.txt").write_text(f"{match}\n", encoding="utf-8")

      # 요약
      md = [
        f"# AI 스키마 비교 결과",
        f"- 모델: `{effective_model}`",
        f"- 프로젝트: `{args.project}` / 리전: `{effective_location}`",
        f"- 결과(match): **{match.upper()}**",
        "",
      ]
      if match == "no":
        md.append("## ❌ 차이 사유")
        if isinstance(reasons, list) and reasons:
          for i, r in enumerate(reasons, 1):
            md.append(f"{i}. {r}")
        else:
          md.append("- (사유 미제공)")
      else:
        md.append("## ✅ 논리적으로 동일한 스키마로 판단되었습니다.")
      # [ADDED] 개선 제안 섹션 (항상 출력)
      md.append("")
      md.append("## 🔧 개선 제안 (Flyway DDL)")
      if isinstance(suggestions, list) and suggestions:
        for i, s in enumerate(suggestions, 1):
          md.append(f"{i}. {s}")
      else:
        md.append("- (모델이 별도 제안을 제공하지 않았습니다)")

      (out_dir / "schema-compare-summary.md").write_text("\n".join(md), encoding="utf-8")

      # [ADDED] 별도 제안 파일도 생성 (가독성용)
      sug_lines = []
      if isinstance(suggestions, list) and suggestions:
        for i, s in enumerate(suggestions, 1):
          sug_lines.append(f"{i}. {s}")
      else:
        sug_lines.append("No suggestions provided.")
      (out_dir / "ai_suggestions.md").write_text("\n".join(sug_lines), encoding="utf-8")

      # 종료 코드
      if match == "yes":
        print("✅ AI 비교 결과: YES (논리 동일)")
        sys.exit(0)
      elif match == "no":
        print("❌ AI 비교 결과: NO (차이 있음)")
        sys.exit(1)
      else:
        print("⚠️ 응답에 match 필드가 yes/no가 아닙니다. 일단 실패 처리.", file=sys.stderr)
        sys.exit(1)

    except Exception as e:
      last_err = e
      time.sleep(2)

  print(f"ERROR: AI 비교 호출 실패: {last_err}", file=sys.stderr)
  sys.exit(2)


if __name__ == "__main__":
  main()
