#!/usr/bin/env python3
"""
normalize_sql.py
----------------
Hibernate DDL vs pg_dump -s 결과를 "의미적으로" 동등 비교가 가능하도록 정규화합니다.

핵심 정규화 포인트
- 스키마 접두어(public.), ALTER TABLE ONLY → 제거
- 타입 표기 통일: character varying → varchar, timestamp without time zone → timestamp(0)
- pg_dump가 PK/UNIQUE를 ALTER/INDEX로 분리하는 관행을 흡수:
  * CREATE TABLE의 inline PRIMARY KEY → "pk <table> (<cols>)" 라인으로 추출
  * ALTER TABLE .. ADD CONSTRAINT .. PRIMARY KEY → 위와 동일 표현으로 치환
  * CREATE UNIQUE INDEX → "unique <table> (<cols>)" 로 치환
  * CREATE TABLE의 inline UNIQUE → 위와 동일 표현으로 추출
- 외래키를 한 줄 요약: "fk <table> (<cols>) -> <ref_table> (<ref_cols>)"
- Flyway 메타테이블 및 소음 제거: flyway_schema_history, SET/OWNER/GRANT/REVOKE 등 제거
- (옵션) ENUM check(...) 제거: STRICT_ENUM_CHECK=false (기본)일 때 check(...) 제거

사용법:
  normalize_sql.py <hibernate-ddl.sql> <flyway-schema.sql>
  정상 종료코드: 0(동일), 1(다름), 2(사용법 오류/입력파일 없음)
"""
import os, re, sys, pathlib

DROP_PREFIXES = (
  'set ', 'select pg_catalog.set_config', '\\connect ', 'grant ', 'revoke ',
  'comment on extension', 'create extension', 'alter schema '
)

def _strip_comments(s: str) -> str:
  s = re.sub(r'--.*', '', s)                        # line comments
  s = re.sub(r'/\*.*?\*/', '', s, flags=re.S)       # block comments
  return s

def _coarse_cleanup(s: str) -> str:
  s = _strip_comments(s)
  s = s.replace('"', '')
  s = s.replace('\r', '')
  # 한 줄씩 소음 제거
  lines = []
  for line in s.splitlines():
    l = line.strip()
    if not l:
      continue
    ll = l.lower()
    if any(ll.startswith(p) for p in DROP_PREFIXES):
      continue
    if ' flyway_schema_history' in ll:
      continue
    if ' owner to ' in ll:
      continue
    lines.append(l)
  s = '\n'.join(lines)
  # 전역 치환
  s = s.lower()
  s = s.replace('public.', '')
  s = re.sub(r'\balter\s+table\s+only\b', 'alter table', s)
  s = re.sub(r'\bif\s+exists\b', '', s)
  # 타입 정규화
  s = s.replace('character varying', 'varchar')
  s = re.sub(r'timestamp\(\s*0\s*\)\s+without\s+time\s+zone', 'timestamp(0)', s)
  s = re.sub(r'timestamp\s+without\s+time\s+zone', 'timestamp(0)', s)
  # FK 옵션 소거
  s = re.sub(r'\s*match\s+simple\s+on\s+update\s+no\s+action\s+on\s+delete\s+no\s+action', '', s)
  s = re.sub(r'\s*on\s+update\s+no\s+action\s+on\s+delete\s+no\s+action', '', s)
  # 불필요 공백 정리
  s = re.sub(r'\s+', ' ', s).strip()
  return s

def _split_statements(s: str):
  # 세미콜론 기준 분리하되, 마지막 ; 없어도 허용
  parts = [p.strip() for p in s.split(';')]
  return [p for p in parts if p]

def _normalize_create_table(stmt: str, out_lines: list, strict_enums: bool):
  # create table t ( ... )
  m = re.match(r'create table\s+(\w+)\s*\((.*)\)\s*$', stmt)
  if not m:
    out_lines.append(stmt)
    return
  tname, body = m.group(1), m.group(2)

  # 컬럼 파트에서 pk/unique/check 분리
  # (패턴 단순 가정하에 split(',') 사용)
  cols = [c.strip() for c in body.split(',') if c.strip()]

  pk_cols = None
  uniques = []    # list of column-name unique
  new_cols = []
  for c in cols:
    # PRIMARY KEY (col,...)
    mpk = re.match(r'primary key\s*\(([^)]+)\)', c)
    if mpk:
      pk_cols = mpk.group(1).strip()
      continue

    # column unique (inline)
    mcol = re.match(r'(\w+)\s+.*', c)
    if mcol and re.search(r'\bunique\b', c):
      colname = mcol.group(1)
      uniques.append(colname)
      # unique 키워드 제거
      c = re.sub(r'\s+unique\b', '', c)

    # check(...) 제거(기본)
    if not strict_enums:
      c = re.sub(r'\bcheck\s*\((?:[^()]*|\([^()]*\))*\)', '', c)

    c = re.sub(r'\s+', ' ', c).strip().rstrip()
    new_cols.append(c)

  # 재조립된 CREATE TABLE (제약 제거본)
  body2 = ', '.join(new_cols)
  out_lines.append(f'create table {tname} ( {body2} )')

  # 파생 라인 추가: pk / unique
  if pk_cols:
    out_lines.append(f'pk {tname} ( {pk_cols} )')
  for u in uniques:
    out_lines.append(f'unique {tname} ( {u} )')

def _normalize_alter_primary(stmt: str, out_lines: list):
  # alter table <t> add constraint _ primary key (cols)
  m = re.match(r'alter table\s+(\w+)\s+add constraint\s+_\s+primary key\s*\(([^)]+)\)', stmt)
  if m:
    t, cols = m.group(1), m.group(2).strip()
    out_lines.append(f'pk {t} ( {cols} )')
    return True
  return False

def _normalize_unique_index(stmt: str, out_lines: list):
  # create unique index _ on <t> using btree (cols)
  m = re.match(r'create unique index\s+_\s+on\s+(\w+)\s+(?:using btree\s*)?\(([^)]+)\)', stmt)
  if m:
    t, cols = m.group(1), m.group(2).strip()
    out_lines.append(f'unique {t} ( {cols} )')
    return True
  return False

def _normalize_fk(stmt: str, out_lines: list):
  # alter table <t> add constraint _ foreign key (a) references <r>(b)
  m = re.match(r'alter table\s+(\w+)\s+add constraint\s+_\s+foreign key\s*\(([^)]+)\)\s+references\s+(\w+)\s*\(([^)]+)\)', stmt)
  if m:
    t, cols, rt, rcols = m.group(1), m.group(2).strip(), m.group(3), m.group(4).strip()
    out_lines.append(f'fk {t} ( {cols} ) -> {rt} ( {rcols} )')
    return True
  return False

def normalize(sql: str, strict_enums_env: str = None) -> str:
  strict_enums = (strict_enums_env or os.getenv('STRICT_ENUM_CHECK') or 'false').lower() == 'true'
  s = _coarse_cleanup(sql)
  stmts = _split_statements(s)

  out = []
  for st in stmts:
    st = st.strip()
    if not st:
      continue

    # 우선 간단한 치환 규칙들 시도
    if _normalize_alter_primary(st, out):
      continue
    if _normalize_unique_index(st, out):
      continue
    if _normalize_fk(st, out):
      continue

    # CREATE TABLE은 제약 추출/제거
    if st.startswith('create table '):
      _normalize_create_table(st, out, strict_enums)
      continue

    # 그 외 문장
    out.append(st)

  # 후처리: 공백 정리 + 정렬
  out = [re.sub(r'\s+', ' ', x).strip() for x in out if x.strip()]
  out = sorted(set(out))
  return '\n'.join(out)

if __name__ == '__main__':
  if len(sys.argv) != 3:
    print('usage: normalize_sql.py <hibernate-ddl.sql> <flyway-schema.sql>', file=sys.stderr)
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
