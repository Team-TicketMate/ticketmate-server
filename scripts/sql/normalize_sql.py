#!/usr/bin/env python3
import re, sys, pathlib

DROP_PREFIXES = (
  'set ', 'select pg_catalog.set_config', 'alter schema ',
  'create extension', 'comment on extension', '\\connect',
  '\\restrict', '\\unrestrict', 'grant ', 'revoke ',
)

OWNER_STMT = re.compile(
    r'^alter\s+table\s+(if\s+exists\s+)?(only\s+)?[a-z0-9_.]+\s+owner\s+to\s+[a-z0-9_]+$'
)

def normalize(sql: str) -> str:
  # 주석 제거
  sql = re.sub(r'--.*', '', sql)
  sql = re.sub(r'/\*.*?\*/', '', sql, flags=re.S)
  # 인용부호 제거 + 소문자화
  sql = sql.replace('"', '').lower()

  # 라인 단위 정리 + 잡음 제거
  lines = [l.strip() for l in sql.splitlines()]
  lines = [l for l in lines if l and not any(l.startswith(p) for p in DROP_PREFIXES)]
  sql = ' '.join(lines)
  sql = re.sub(r'\s+', ' ', sql).strip()

  # 문장 분리
  stmts = [s.strip() for s in sql.split(';') if s.strip()]

  filtered = []
  for s in stmts:
    if 'flyway_schema_history' in s:  # flyway 메타테이블 제거
      continue
    if OWNER_STMT.match(s):           # OWNER TO 제거
      continue
    filtered.append(s)

  # 제약/인덱스 이름 익명화
  filtered = [re.sub(r'\bconstraint\s+[a-z0-9_]+\b', 'constraint _', x) for x in filtered]
  filtered = [re.sub(r'\bindex\s+[a-z0-9_]+\b', 'index _', x) for x in filtered]

  return '\n'.join(sorted(filtered))

if __name__ == '__main__':
  if len(sys.argv) != 3:
    print('usage: normalize_sql.py <in.sql> <out.sql.norm>', file=sys.stderr)
    sys.exit(2)
  in_path = pathlib.Path(sys.argv[1])
  out_path = pathlib.Path(sys.argv[2])
  out_path.write_text(normalize(in_path.read_text(encoding='utf-8')), encoding='utf-8')
