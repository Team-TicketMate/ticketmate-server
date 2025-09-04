#!/usr/bin/env python3
import sys, pathlib, difflib

def load_lines(p: pathlib.Path):
  return [x for x in p.read_text(encoding='utf-8').splitlines()]

def main(a_norm: str, b_norm: str, out_dir: str):
  a = pathlib.Path(a_norm); b = pathlib.Path(b_norm)
  out = pathlib.Path(out_dir)
  out.mkdir(parents=True, exist_ok=True)

  a_lines = load_lines(a)
  b_lines = load_lines(b)

  # unified diff
  diff_text = '\n'.join(difflib.unified_diff(a_lines, b_lines, fromfile=str(a), tofile=str(b), lineterm=''))
  (out / 'schema.diff').write_text(diff_text, encoding='utf-8')

  # 집합 비교
  sa = set([x for x in a_lines if x.strip()])
  sb = set([x for x in b_lines if x.strip()])
  only_a = sorted(sa - sb)
  only_b = sorted(sb - sa)

  (out / 'only-in-hibernate.txt').write_text('\n'.join(only_a), encoding='utf-8')
  (out / 'only-in-flyway.txt').write_text('\n'.join(only_b), encoding='utf-8')

  # 요약 MD
  def preview(items, n=30):
    return '\n'.join(items[:n]) if items else '(없음)'

  md = []
  md.append('# 스키마 비교 요약')
  md.append('')
  md.append(f'- hibernate 전용 문장 수: **{len(only_a)}**')
  md.append(f'- flyway 전용 문장 수: **{len(only_b)}**')
  md.append('')
  md.append('## hibernate에만 존재 (상위 30)')
  md.append('```sql')
  md.append(preview(only_a))
  md.append('```')
  md.append('## flyway에만 존재 (상위 30)')
  md.append('```sql')
  md.append(preview(only_b))
  md.append('```')
  (out / 'schema-compare-summary.md').write_text('\n'.join(md), encoding='utf-8')

  # 동일 여부로 종료코드
  return 0 if not only_a and not only_b else 1

if __name__ == '__main__':
  if len(sys.argv) != 4:
    print('usage: make_sql_diff_report.py <hibernate.sql.norm> <flyway.sql.norm> <out_dir>', file=sys.stderr)
    sys.exit(2)
  sys.exit(main(sys.argv[1], sys.argv[2], sys.argv[3]))
