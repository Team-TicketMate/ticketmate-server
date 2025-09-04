#!/usr/bin/env python3
import re, sys, pathlib

DROP_PREFIXES = (
  'set ', 'select pg_catalog.set_config', 'alter schema ',
  'create extension', 'comment on extension', '\\connect ',
  '\\restrict', '\\unrestrict',  # pg_dump 17 메타 라인 제거
  'grant ', 'revoke ',
)

def normalize(sql: str) -> str:
  # remove line comments and block comments
  sql = re.sub(r'--.*', '', sql)
  sql = re.sub(r'/\*.*?\*/', '', sql, flags=re.S)
  # remove quotes
  sql = sql.replace('"', '')
  # lower for easier matching
  sql = sql.lower()

  # line-based drops
  lines = [l.strip() for l in sql.splitlines()]
  lines = [l for l in lines if l and not any(l.startswith(p) for p in DROP_PREFIXES)]
  sql = ' '.join(lines)

  # collapse whitespace
  sql = re.sub(r'\s+', ' ', sql).strip()

  # split statements
  stmts = [s.strip() for s in sql.split(';') if s.strip()]

  filtered = []
  for s in stmts:
    # 1) Flyway 메타 테이블 관련 DDL/인덱스/소유자 변경 전부 무시
    if 'flyway_schema_history' in s:
      continue
    # 2) OWNER 변경 같은 환경 잡음 제거
    if re.search(r'alter table .* owner to ', s):
      continue
    filtered.append(s)

  # anonymize constraint/index names (남은 문장에 대해서만)
  filtered = [re.sub(r'\bconstraint\s+[a-z0-9_]+\b', 'constraint _', x) for x in filtered]
  filtered = [re.sub(r'\bindex\s+[a-z0-9_]+\b', 'index _', x) for x in filtered]

  return '\n'.join(sorted(filtered))

if __name__ == '__main__':
  if len(sys.argv) != 3:
    print('usage: normalize_sql.py <hibernate-ddl.sql> <flyway-schema.sql>', file=sys.stderr)
    sys.exit(2)
  a_path, b_path = sys.argv[1], sys.argv[2]
  a = pathlib.Path(a_path).read_text(encoding='utf-8')
  b = pathlib.Path(b_path).read_text(encoding='utf-8')
  a_norm, b_norm = normalize(a), normalize(b)
  pathlib.Path(a_path + '.norm').write_text(a_norm, encoding='utf-8')
  pathlib.Path(b_path + '.norm').write_text(b_norm, encoding='utf-8')
  if a_norm == b_norm:
    sys.exit(0)
  else:
    sys.stderr.write('Schemas differ. See *.norm files for details.\n')
    sys.exit(1)
