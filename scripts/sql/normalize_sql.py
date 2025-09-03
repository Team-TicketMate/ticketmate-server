#!/usr/bin/env python3
import re, sys, pathlib

DROP_PREFIXES = (
  'set ', 'select pg_catalog.set_config', 'alter schema ',
  'create extension', 'comment on extension', '\\connect ',
  'grant ', 'revoke ', 'owner to '
)

def normalize(sql: str) -> str:
  # remove line comments and block comments
  sql = re.sub(r'--.*', '', sql)
  sql = re.sub(r'/\*.*?\*/', '', sql, flags=re.S)
  # remove quotes
  sql = sql.replace('"', '')
  # strip lines + drop noisy lines
  lines = [l.strip() for l in sql.splitlines()]
  lines = [l for l in lines if l and not any(l.lower().startswith(p) for p in DROP_PREFIXES)]
  sql = ' '.join(lines)
  # whitespace + case normalize
  sql = re.sub(r'\s+', ' ', sql).strip().lower()
  # anonymize constraint/index names
  sql = re.sub(r'\bconstraint\s+[a-z0-9_]+\b', 'constraint _', sql)
  sql = re.sub(r'\bindex\s+[a-z0-9_]+\b', 'index _', sql)
  # split by ';' and sort
  stmts = [s.strip() for s in sql.split(';') if s.strip()]
  return '\n'.join(sorted(stmts))

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
