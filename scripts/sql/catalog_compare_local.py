#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json, sys, pathlib, re
from collections import defaultdict

# 사용법: catalog_compare_local.py hib.json fly.json out_dir

NON_DIFFS = {
  "type_alias": {
    "varchar": {"character varying"},
    "float8": {"double precision"},
    "timestamp": {"timestamp without time zone", "timestamp(0)"},
    "timestamptz": {"timestamp with time zone"},
  }
}

def _type_eq(a, b):
  a, b = (a or "").lower(), (b or "").lower()
  if a == b: return True
  for base, aliases in NON_DIFFS["type_alias"].items():
    if a == base and b in aliases: return True
    if b == base and a in aliases: return True
  return False

def _key(*parts): return ".".join(parts)
def load(path): return json.loads(pathlib.Path(path).read_text(encoding="utf-8"))
def index_by_table(items):
  m = defaultdict(list)
  for it in items:
    m[(it["table_schema"], it["table_name"])].append(it)
  return m

def normalize_names_ok(name):
  return bool(re.fullmatch(r"[a-z0-9_]+", (name or "")))

def compare(hib, fly):
  reasons = []

  hib_tables = {(t["table_schema"], t["table_name"]) for t in hib["tables"]}
  fly_tables = {(t["table_schema"], t["table_name"]) for t in fly["tables"]}

  only_hib = sorted(hib_tables - fly_tables)
  only_fly = sorted(fly_tables - hib_tables)
  if only_hib:
    reasons.append(f"Flyway에 없는 테이블: {only_hib}")
  if only_fly:
    reasons.append(f"Hibernate에 없는 테이블: {only_fly}")

  hib_cols = index_by_table(hib["columns"])
  fly_cols = index_by_table(fly["columns"])
  for tbl in sorted(hib_tables & fly_tables):
    a_cols = {c["column_name"]: c for c in hib_cols.get(tbl, [])}
    b_cols = {c["column_name"]: c for c in fly_cols.get(tbl, [])}
    for c in sorted(set(a_cols) - set(b_cols)):
      reasons.append(f"{tbl} 컬럼 {c} 가 Flyway에 없음")
    for c in sorted(set(b_cols) - set(a_cols)):
      reasons.append(f"{tbl} 컬럼 {c} 가 Hibernate에 없음")
    for c in sorted(set(a_cols) & set(b_cols)):
      ac, bc = a_cols[c], b_cols[c]
      if not _type_eq(ac.get("norm_type"), bc.get("norm_type")):
        reasons.append(f"{tbl}.{c} 타입 불일치: {ac.get('norm_type')} vs {bc.get('norm_type')}")
      if bool(ac.get("is_nullable")) != bool(bc.get("is_nullable")):
        reasons.append(f"{tbl}.{c} NULL 허용 불일치: {ac.get('is_nullable')} vs {bc.get('is_nullable')}")
      adef = 1 if (ac.get("column_default") or "").strip() else 0
      bdef = 1 if (bc.get("column_default") or "").strip() else 0
      if adef != bdef:
        reasons.append(f"{tbl}.{c} DEFAULT 유무 불일치")

  def _keyset(rows, kind):
    out = defaultdict(set)
    for r in rows:
      if r["contype"] != kind: continue
      k = (r["table_schema"], r["table_name"])
      out[k].add(tuple(r["columns"] or []))
    return out

  hib_cons = hib["constraints"]; fly_cons = fly["constraints"]
  for kind, tag in (("p","PK"),("u","UNIQUE")):
    a = _keyset(hib_cons, kind); b = _keyset(fly_cons, kind)
    for tbl in sorted((set(a) | set(b))):
      if a[tbl] != b[tbl]:
        reasons.append(f"{tbl} {tag} 집합 불일치: {sorted(a[tbl])} vs {sorted(b[tbl])}")

  def _fkset(rows):
    out = defaultdict(set)
    for r in rows:
      k = (r["table_schema"], r["table_name"])
      out[k].add( (tuple(r["columns"] or []), (r["ref_table_schema"], r["ref_table_name"]), tuple(r["ref_columns"] or [])) )
    return out
  a_fk = _fkset(hib["fkeys"]); b_fk = _fkset(fly["fkeys"])
  for tbl in sorted((set(a_fk) | set(b_fk))):
    if a_fk[tbl] != b_fk[tbl]:
      reasons.append(f"{tbl} FK 집합 불일치")

  match = "yes" if not reasons else "no"
  return match, reasons

def suggestions_for_fly(fly):
  sug = []
  idx_by_tbl = index_by_table(fly["indexes"])
  fk_by_tbl = index_by_table(fly["fkeys"])
  for tbl, fks in fk_by_tbl.items():
    tbl_cols_index_sets = { tuple(i["columns"]) for i in idx_by_tbl.get(tbl, []) }
    for fk in fks:
      cols = tuple(fk["columns"] or [])
      if cols and cols not in tbl_cols_index_sets:
        sug.append(f"{tbl} FK {cols}에 보조 인덱스 생성을 검토하세요 (조인/삭제 성능).")
  cons_by_tbl = index_by_table(fly["constraints"])
  cols_by_tbl = index_by_table(fly["columns"])
  for tbl, cols in cols_by_tbl.items():
    pk_sets = [set(c["columns"] or []) for c in cons_by_tbl.get(tbl, []) if c.get("contype")=="p"]
    if not pk_sets:
      sug.append(f"{tbl}에 기본키(PK)가 없습니다. ID 전략/자연키 여부를 명확히 하세요.")
    else:
      pk = next(iter(pk_sets))
      for c in cols:
        if c["column_name"] in pk and c.get("is_nullable"):
          sug.append(f"{tbl}.{c['column_name']}는 PK인데 NULL 허용입니다. NOT NULL로 고정하세요.")
  for t in fly["tables"]:
    if not normalize_names_ok(t["table_name"]):
      sug.append(f"{(t['table_schema'], t['table_name'])} 이름을 snake_case로 정리하세요.")
  for c in fly["columns"]:
    if not normalize_names_ok(c["column_name"]):
      sug.append(f"{(c['table_schema'], c['table_name'])}.{c['column_name']} 컬럼명을 snake_case로 정리하세요.")
  for con in fly["constraints"]:
    if not normalize_names_ok(con["constraint_name"]):
      sug.append(f"{(con['table_schema'], con['table_name'])} 제약명 `{con['constraint_name']}` 네이밍 컨벤션 정비(pk_/fk_/uq_ 등).")
  for ix in fly["indexes"]:
    if not normalize_names_ok(ix["index_name"]):
      sug.append(f"{(ix['table_schema'], ix['table_name'])} 인덱스명 `{ix['index_name']}` 네이밍 컨벤션 정비(idx_<table>__<cols>).")
  for c in fly["columns"]:
    tn = (c.get("norm_type") or "").lower()
    nm = (c["column_name"] or "").lower()
    if nm in {"created_date","updated_date","created_at","updated_at"} and tn == "timestamp":
      sug.append(f"{(c['table_schema'], c['table_name'])}.{c['column_name']}에 timestamptz 사용을 검토하세요(타임존 안전).")
  for c in fly["columns"]:
    if (c.get("norm_type") or "").lower() == "varchar" and not c.get("character_maximum_length"):
      sug.append(f"{(c['table_schema'], c['table_name'])}.{c['column_name']} VARCHAR 길이 미지정. TEXT 또는 명시적 길이 전략을 확정하세요.")

  out=[]; seen=set()
  for s in sug:
    if s not in seen:
      out.append(s); seen.add(s)
    if len(out) >= 10: break
  if not out:
    out.append("스키마는 양호합니다. 마이그레이션 트랜잭션/롤백 전략을 정기 점검하세요.")
  return out

def main():
  if len(sys.argv) != 4:
    print("usage: catalog_compare_local.py <hib.json> <fly.json> <out_dir>", file=sys.stderr)
    sys.exit(2)
  hib_path, fly_path, out_dir = sys.argv[1], sys.argv[2], sys.argv[3]
  hib = load(hib_path); fly = load(fly_path)

  match, reasons = compare(hib, fly)
  sugg = suggestions_for_fly(fly)

  result = {"match": match, "reasons": reasons, "suggestions": sugg}
  out_dir_p = pathlib.Path(out_dir); out_dir_p.mkdir(parents=True, exist_ok=True)
  (out_dir_p / "local_schema_compare.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
  (out_dir_p / "ai_verdict.txt").write_text(f"{match}\n", encoding="utf-8")

  md = [f"# Local 스키마 비교 결과", f"- 결과(match): **{match.upper()}**", ""]
  if match == "no":
    md.append("## ❌ 차이 사유")
    for i, r in enumerate(reasons, 1): md.append(f"{i}. {r}")
  else:
    md.append("## ✅ 논리적으로 동일한 스키마로 판단되었습니다.")
  md.append(""); md.append("## 🔧 개선 제안 (Flyway DDL)")
  for i, s in enumerate(sugg, 1): md.append(f"{i}. {s}")
  (out_dir_p / "schema-compare-summary.md").write_text("\n".join(md), encoding="utf-8")

  print(f"Local compare done: {match}")

if __name__ == "__main__":
  main()
