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
- Treat database-level DEFAULTs as non-differences when the ORM typically supplies values on INSERT
  (e.g., boolean DEFAULT false, now(), gen_random_uuid()) unless @DynamicInsert is explicitly in use.
- Treat ID generation strategy variants (IDENTITY/SERIAL/SEQUENCE/UUID) as equivalent if PK is NOT NULL and unique.
- Treat absence/presence of enum CHECK constraints as equivalent when JPA maps enums to VARCHAR (EnumType.STRING).
- Treat expression/functional/partial indexes, index methods, CONCURRENTLY, storage and tablespace options as cosmetic.
- Treat DEFERRABLE/INITIALLY DEFERRED on constraints as non-differences unless explicitly required by app semantics.
- Treat partitioning (PARTITION BY ...), inheritance, storage parameters (FILLFACTOR, autovacuum settings) as non-differences.
- Treat triggers/functions/views/materialized views unrelated to PK/FK/UNIQUE/NOT NULL semantics as non-differences.
- Treat schema qualification differences (public.foo vs foo), collation settings, domain vs base type, as non-differences.
""".strip()

# 한국어 출력
LANGUAGE_POLICY = """
Language requirements:
- All human-readable strings in JSON (values of "reasons" and "suggestions") MUST be written in Korean.
- Keep JSON keys EXACTLY as: "match", "reasons", "suggestions".
- The "match" value MUST be only "yes" or "no" (lowercase).
- Do not include code fences or backticks. The response must be pure JSON.
""".strip()

# Hibernate 기본 가정(ORM 문맥 힌트): 모델이 오판하지 않도록 제공
ORM_ASSUMPTIONS = """
Framework context and assumptions:
- The application uses Hibernate ORM with default behavior: INSERT statements typically include all mapped columns
  (DB-level DEFAULT rarely fires) unless @DynamicInsert is enabled.
- @ColumnDefault or columnDefinition may declare defaults, but their absence on Hibernate side must NOT be considered a mismatch.
- Enums are commonly mapped as VARCHAR (EnumType.STRING). Lack of DB-side CHECK for enum values is acceptable.
- Primary key generation strategy may differ across environments (IDENTITY/SERIAL/SEQUENCE/UUID) without semantic change
  if uniqueness and NOT NULL are enforced.
""".strip()

SYSTEM_RULES = f"""
You are a PostgreSQL DDL schema comparison assistant.

Your task:
- Compare two schemas logically (semantic equivalence), NOT textually.
- Schema A: Hibernate-generated DDL (represents the Java/Hibernate model).
- Schema B: Flyway-applied DDL (represents the production schema).
- Decide whether Schema B is a SAFE, COMPATIBLE, and SEMANTICALLY CORRECT implementation of Schema A.

Output format (STRICT):
- Your ONLY output MUST be a single JSON object with this exact shape:

{{
  "match": "yes" | "no",
  "reasons": ["reason 1", "reason 2", ...],
  "suggestions": ["actionable improvement 1", ...]
}}

Hard JSON rules:
- Keys MUST be exactly: "match", "reasons", "suggestions".
- "match" MUST be "yes" or "no" (lowercase).
- When "match" == "no", "reasons" MUST be a non-empty array.
- "suggestions" MUST ALWAYS contain 3-10 concise items.
- The response MUST be valid JSON ONLY (no code fences, no surrounding text).

Language requirements:
- All human-readable strings in "reasons" and "suggestions" MUST be written in Korean.
- JSON keys and structural tokens MUST remain in English exactly as specified above.

Authoritative evidence rules (VERY IMPORTANT):
- Base ALL conclusions ONLY on the actual DDL content provided for Schema A and Schema B.
- DO NOT assume, guess, or hallucinate missing/present constraints, indexes, or FKs.
- Before stating that something is "missing in Schema B", you MUST:
  - Scan Schema B for PRIMARY KEYs, UNIQUE constraints, UNIQUE indexes,
    FOREIGN KEY constraints, and CHECK constraints on the SAME column set,
    regardless of their names.
  - If an equivalent constraint or index exists in Schema B (even with a different name),
    you MUST NOT report it as missing.
- Constraint/index NAMES are irrelevant. Only their columns and semantics matter.

Comparison model (ASYMMETRIC, semantic):
- Treat Schema B as the authoritative production schema.
- Schema B is allowed to be STRICTER or MORE EXPRESSIVE than Schema A if:
  - It does NOT block any valid entity state implied by Schema A.
  - It does NOT contradict the logical model of Schema A.
- The key question:
  "Can entities defined by Schema A be safely and correctly stored/read using Schema B?"

Scope to consider:
- Tables and columns
- Data types and length/precision
- Nullability
- Defaults (ONLY when they materially change semantics)
- Primary keys
- Unique constraints
- Foreign keys
- Check constraints
- Indexes that materially affect PK/FK/UNIQUE semantics or critical lookups

Treat the following as NON-differences (must NOT cause match = "no"):

{KNOWN_FALSE_POSITIVES}

Additional ORM context (from Hibernate, NOT mismatches by themselves):
{ORM_ASSUMPTIONS}

Explicit NON-differences for this task (CRITICAL):

- Do NOT set "match": "no" just because:
  - Schema B has STRICTER CHECK constraints (e.g. E.164 phone format, priority range 1-10),
    as long as all values valid under Schema A's model are still allowed.
  - Schema B adds EXTRA UNIQUE constraints or indexes that narrow the domain
    (e.g. unique names, URLs, paths) when Schema A does not forbid them.
  - Schema B adds DEFAULT values for columns that are NOT NULL in Schema A,
    and those defaults are reasonable domain choices (e.g. DEFAULT false for flags).
  - Schema B enforces domain rules (phone format, enums, etc.) that Hibernate's
    auto DDL simply cannot fully express.

These differences may be mentioned in "suggestions" ONLY as best-practice or documentation notes,
NOT as reasons to set "match": "no".

Decision policy (when to set match = "no"):

Set "match": "no" ONLY if there is at least ONE CLEAR semantic incompatibility where
Schema B is weaker than required by Schema A, or directly conflicts with it. Examples:

1) Missing or weaker constraints in Schema B:
   - A field that is an @Id / PK in Schema A is NOT PRIMARY KEY / NOT UNIQUE / nullable in Schema B.
   - A field with nullable = false in Schema A is nullable in Schema B.
   - A field with unique = true in Schema A has NO corresponding UNIQUE/PK on that column set in Schema B.
   - A clear @ManyToOne/@OneToOne/@JoinColumn mapping in Schema A has NO reasonable FK in Schema B,
     so referential integrity is not enforced at all.

2) Schema B actively conflicts with Schema A:
   - Incompatible data types/lengths that would truncate or reject valid values
     allowed by Schema A (e.g. A allows 255 chars, B only 10).
   - CHECK constraints in Schema B that reject values clearly valid under Schema A
     (based on A's column definitions or enums).
   - Critically wrong FK targets (wrong table/column) that break the logical model.

If NONE of the above holds:
- KEEP "match": "yes".
- Do NOT invent or overstate differences.
- Stricter or more domain-rich Schema B is acceptable and should NOT flip match to "no".

Suggestions (ALWAYS required, for Schema B / Flyway DDL):

- Always include 3-10 short, actionable suggestions in Korean.
- Even when "match" == "yes", provide concrete improvements for Flyway DDL, such as:
  - 중요한 FK 컬럼에 인덱스 추가,
  - PK/FK/UNIQUE/NOT NULL 규칙 명시 및 일관성 강화,
  - CHECK 제약을 도메인 규칙에 맞게 명확화,
  - 마이그레이션 시 IF NOT EXISTS / 기존 데이터 검증 등 방어적 DDL 적용,
  - Flyway 스키마와 애플리케이션 도메인 규칙을 주석이나 문서로 명확히 연결.
- Do NOT list as "reasons" any item that is not strictly supported by the given DDLs.
"""

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
