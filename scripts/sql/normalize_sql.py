#!/usr/bin/env python3
"""
normalize_sql.py
----------------
Hibernate DDL vs pg_dump -s 결과를 "의미적으로" 동등 비교가 가능하도록 정규화합니다.

핵심 포인트
- 스키마 접두어(public.), ALTER TABLE ONLY → 제거
- 타입 통일: character varying → varchar, timestamp without time zone → timestamp(0),
            double precision/float8 → float(53)
- PK/UNIQUE/FK 표준화:
  * CREATE TABLE의 inline PRIMARY KEY/UNIQUE → "pk/unique <table> (...)" 추출
  * 컬럼 인라인 FK (constraint ... references ...) → "fk <table> ( <col> ) -> <ref_table>"
  * ALTER TABLE ADD CONSTRAINT PRIMARY/FOREIGN/UNIQUE → 동일 포맷으로 치환
- Flyway 메타/노이즈 제거: flyway_schema_history, SET/OWNER/GRANT/REVOKE/psql meta(\...) 제거
- STRICT_ENUM_CHECK=true면 check(...) 유지, false면 제거

사용법:
  normalize_sql.py <hibernate-ddl.sql> <flyway-schema.sql>
  종료코드: 0(동일), 1(다름), 2(오류)
"""
import os
import pathlib
import re

import sys

DROP_PREFIXES = (
  'set ', 'select pg_catalog.set_config', '\\connect ', 'grant ', 'revoke ',
  'comment on extension', 'create extension', 'alter schema '
)


def _split_top_level(s: str) -> list[str]:
  out, buf, depth = [], [], 0
  for ch in s:
    if ch == '(':
      depth += 1
    elif ch == ')':
      depth = max(0, depth - 1)
    if ch == ',' and depth == 0:
      out.append(''.join(buf).strip())
      buf = []
    else:
      buf.append(ch)
  if buf:
    out.append(''.join(buf).strip())
  return out


def _strip_comments(s: str) -> str:
  s = re.sub(r'--.*', '', s)  # line comments
  s = re.sub(r'/\*.*?\*/', '', s, flags=re.S)  # block comments
  return s


def _coarse_cleanup(s: str) -> str:
  s = _strip_comments(s)
  s = s.replace('"', '')
  s = s.replace('\r', '')
  lines = []
  for line in s.splitlines():
    l = line.strip()
    if not l:
      continue
    # [CHANGED] psql meta-lines 제거
    if l.startswith('\\'):
      continue
    ll = l.lower()
    if any(ll.startswith(p) for p in DROP_PREFIXES):
      continue
    # [CHANGED] flyway meta table 라인 제거 (단어 경계)
    if re.search(r'\bflyway_schema_history\b', ll):
      continue
    # OWNER TO 제거
    if ' owner to ' in ll:
      continue
    lines.append(l)

  s = '\n'.join(lines)
  # 전역 치환/정규화
  s = s.lower()
  s = s.replace('public.', '')
  s = re.sub(r'\balter\s+table\s+only\b', 'alter table', s)
  s = re.sub(r'\bif\s+exists\b', '', s)

  # 타입 정규화
  s = s.replace('character varying', 'varchar')
  s = re.sub(r'timestamp\(\s*0\s*\)\s+without\s+time\s+zone', 'timestamp(0)', s)
  s = re.sub(r'timestamp\s+without\s+time\s+zone', 'timestamp(0)', s)
  # [CHANGED] float 동치화
  s = s.replace('double precision', 'float(53)')
  s = s.replace('float8', 'float(53)')

  # FK 옵션 소거
  s = re.sub(
      r'\s*match\s+simple\s+on\s+update\s+no\s+action\s+on\s+delete\s+no\s+action',
      '', s)
  s = re.sub(r'\s*on\s+update\s+no\s+action\s+on\s+delete\s+no\s+action', '', s)

  # 공백 정리
  s = re.sub(r'\s+', ' ', s).strip()
  return s


def _normalize_alter_unique(stmt: str, out_lines: list):
  # alter table <t> add constraint <name> unique (cols)
  m = re.match(
      r'alter table\s+(\w+)\s+add constraint\s+\S+\s+unique\s*\(([^)]+)\)',
      stmt)
  if m:
    t, cols = m.group(1), m.group(2).strip()
    out_lines.append(f'unique {t} ( {cols} )')
    return True
  return False


def _split_statements(s: str):
  parts = [p.strip() for p in s.split(';')]
  return [p for p in parts if p]


def _normalize_create_table(stmt: str, out_lines: list, strict_enums: bool):
  m = re.match(r'create table\s+(\w+)\s*\((.*)\)\s*$', stmt)
  if not m:
    out_lines.append(stmt)
    return
  tname, body = m.group(1), m.group(2)

  # naive split by comma (전제: hibernate/pg_dump 포맷에서 괄호 중첩이 단순)
  cols = _split_top_level(body)

  pk_cols = None
  uniques = []
  fks = []  # [CHANGED] 인라인 FK 수집
  new_cols = []

  for c in cols:
    # [CHANGED] 테이블 수준 PK (primary key (a,b))
    # UNIQUE (a, b) - table-level
    mu = re.match(r'unique\s*\(([^)]+)\)', c)
    if mu:
      out_lines.append(f'unique {tname} ( {mu.group(1).strip()} )')
      continue

    # [CHANGED] 컬럼명
    mcol = re.match(r'(\w+)\s+.*', c)
    colname = mcol.group(1) if mcol else None

    # [CHANGED] 컬럼 인라인 PK (col ... primary key)
    if colname and re.search(r'\bprimary\s+key\b', c):
      pk_cols = colname

      # 컬럼표현에서 primary key 토큰 제거
      c = re.sub(r'\bprimary\s+key\b', '', c)

    # 인라인 UNIQUE
    if colname and re.search(r'\bunique\b', c):
      uniques.append(colname)
      c = re.sub(r'\s+unique\b', '', c)

    # [CHANGED] 인라인 FK: (optional) constraint <name> references <table> [(cols)]
    # 예: "constraint fkxxxx references member", "references concert(concert_id)"
    fk_match = re.search(
        r'(?:constraint\s+\S+\s+)?references\s+(\w+)(?:\s*\(([^)]+)\))?', c)
    if colname and fk_match:
      ref_table = fk_match.group(1)
      # ref_cols = (fk_match.group(2) or '').strip()  # 컬럼은 비교 표준화에서 생략
      fks.append((tname, colname, ref_table))
      # 컬럼 정의에서 FK 구문 제거
      c = re.sub(
          r'\s*(?:constraint\s+\S+\s+)?references\s+\w+(?:\s*\([^)]+\))?', '',
          c)

    # enum check 제거/유지
    if not strict_enums:
      c = re.sub(r'\bcheck\s*\((?:[^()]*|\([^()]*\))*\)', '', c)

    # 공백 정리
    c = re.sub(r'\s+', ' ', c).strip().rstrip()
    if c:
      new_cols.append(c)

  # 재조립된 CREATE TABLE (제약 제거본)
  body2 = ', '.join(new_cols)
  out_lines.append(f'create table {tname} ( {body2} )')

  # 파생 라인: pk / unique / fk
  if pk_cols:
    out_lines.append(f'pk {tname} ( {pk_cols} )')
  for u in uniques:
    out_lines.append(f'unique {tname} ( {u} )')
  for (_t, _c, _rt) in fks:
    # [CHANGED] 참조 컬럼은 통일성/단순화를 위해 생략 (ALTER 기반과 동일 포맷)
    out_lines.append(f'fk {_t} ( {_c} ) -> {_rt}')


def _normalize_alter_primary(stmt: str, out_lines: list):
  # alter table <t> add constraint <name> primary key (cols)
  m = re.match(
      r'alter table\s+(\w+)\s+add constraint\s+\S+\s+primary key\s*\(([^)]+)\)',
      stmt)
  if m:
    t, cols = m.group(1), m.group(2).strip()
    out_lines.append(f'pk {t} ( {cols} )')
    return True
  return False


def _normalize_unique_index(stmt: str, out_lines: list):
  # create unique index <name> on <t> using btree (cols)
  m = re.match(
      r'create unique index\s+\S+\s+on\s+(\w+)\s+(?:using btree\s*)?\(([^)]+)\)',
      stmt)
  if m:
    t, cols = m.group(1), m.group(2).strip()
    out_lines.append(f'unique {t} ( {cols} )')
    return True
  return False


def _normalize_fk(stmt: str, out_lines: list):
  # ALTER FK: alter table <t> add constraint <name> foreign key (a) references <rt> [(b)]
  m = re.match(
      r'alter table\s+(\w+)\s+add constraint\s+\S+\s+foreign key\s*\(([^)]+)\)\s+references\s+(\w+)(?:\s*\(([^)]+)\))?',
      stmt
  )
  if m:
    t, cols, rt = m.group(1), m.group(2).strip(), m.group(3)
    # [CHANGED] 참조 컬럼은 생략해 표준화(인라인 FK와 동일 포맷)
    out_lines.append(f'fk {t} ( {cols} ) -> {rt}')
    return True
  return False


def normalize(sql: str, strict_enums_env: str = None) -> str:
  strict_enums = (strict_enums_env or os.getenv(
      'STRICT_ENUM_CHECK') or 'false').lower() == 'true'
  s = _coarse_cleanup(sql)
  stmts = _split_statements(s)

  out = []
  for st in stmts:
    st = st.strip()
    if not st:
      continue
    if _normalize_alter_primary(st, out):
      continue
    if _normalize_alter_unique(st, out):
      continue
    if _normalize_unique_index(st, out):
      continue
    if _normalize_fk(st, out):
      continue
    if st.startswith('create table '):
      _normalize_create_table(st, out, strict_enums)
      continue
    out.append(st)

  out = [re.sub(r'\s+', ' ', x).strip() for x in out if x.strip()]
  out = sorted(set(out))
  return '\n'.join(out)


if __name__ == '__main__':
  if len(sys.argv) != 3:
    print('usage: normalize_sql.py <hibernate-ddl.sql> <flyway-schema.sql>',
          file=sys.stderr)
    sys.exit(2)
  a_path, b_path = sys.argv[1], sys.argv[2]
  if not (pathlib.Path(a_path).exists() and pathlib.Path(b_path).exists()):
    print('input file not found', file=sys.stderr)
    sys.exit(2)

  a = pathlib.Path(a_path).read_text(encoding='utf-8', errors='ignore')
  b = pathlib.Path(b_path).read_text(encoding='utf-8', errors='ignore')
  a_norm, b_norm = normalize(a), normalize(b)
  pathlib.Path(a_path + '.norm').write_text(a_norm, encoding='utf-8')
  pathlib.Path(b_path + '.norm').write_text(b_norm, encoding='utf-8')
  if a_norm == b_norm:
    sys.exit(0)
  else:
    sys.stderr.write('Schemas differ. See *.norm files for details.\n')
    sys.exit(1)
