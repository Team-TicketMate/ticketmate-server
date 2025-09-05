#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json, sys, pathlib
import psycopg2
import psycopg2.extras

# 사용법: catalog_snapshot.py postgresql://user:pass@host:5432/dbname /path/out.json

IGNORED_TABLES = {("public", "flyway_schema_history")}
IGNORED_SCHEMAS = {"pg_catalog", "information_schema"}

def q(conn, sql, args=None):
  with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
    cur.execute(sql, args or ())
    return cur.fetchall()

def normalize_type(row):
  t = row.get("data_type") or row.get("udt_name") or ""
  t = t.lower()
  t = t.replace("character varying", "varchar")
  t = t.replace("double precision", "float8")
  t = t.replace("timestamp without time zone", "timestamp")
  t = t.replace("timestamp with time zone", "timestamptz")
  return t

def snapshot(conn):
  tables = q(conn, """
                   select table_schema, table_name
                   from information_schema.tables
                   where table_type='BASE TABLE'
                     and table_schema not in %s
                   order by table_schema, table_name
                   """, (tuple(IGNORED_SCHEMAS),))
  tables = [t for t in tables if (t["table_schema"], t["table_name"]) not in IGNORED_TABLES]

  columns = q(conn, """
                    select table_schema, table_name, column_name, ordinal_position,
                           data_type, udt_name, is_nullable, column_default,
                           character_maximum_length, numeric_precision, numeric_scale
                    from information_schema.columns
                    where table_schema not in %s
                    order by table_schema, table_name, ordinal_position
                    """, (tuple(IGNORED_SCHEMAS),))
  for c in columns:
    c["norm_type"] = normalize_type(c)
    c["is_nullable"] = (c["is_nullable"] == "YES")

  constraints = q(conn, """
                        select n.nspname as table_schema, c.relname as table_name,
                               con.conname as constraint_name, con.contype,
                               array_agg(a.attname order by a.attnum) as columns
                        from pg_constraint con
                                 join pg_class c on c.oid = con.conrelid
                                 join pg_namespace n on n.oid = c.relnamespace
                                 left join unnest(con.conkey) with ordinality AS ck(attnum, ord) on true
                                 left join pg_attribute a on a.attrelid = c.oid and a.attnum = ck.attnum
                        where n.nspname not in %s
                          and con.contype in ('p','u')
                        group by 1,2,3,4
                        order by 1,2,3
                        """, (tuple(IGNORED_SCHEMAS),))

  fkeys = q(conn, """
                  select
                      ns.nspname  as table_schema,
                      cl.relname  as table_name,
                      con.conname as constraint_name,
                      nsc.nspname as ref_table_schema,
                      clr.relname as ref_table_name,
                      array_agg(a.attname order by a.attnum) as columns,
                      array_agg(ar.attname order by ar.attnum) as ref_columns
                  from pg_constraint con
                           join pg_class cl on cl.oid = con.conrelid
                           join pg_namespace ns on ns.oid = cl.relnamespace
                           join pg_class clr on clr.oid = con.confrelid
                           join pg_namespace nsc on nsc.oid = clr.relnamespace
                           join unnest(con.conkey) with ordinality AS ck(attnum, ord) on true
                           join pg_attribute a on a.attrelid = cl.oid and a.attnum = ck.attnum
                           join unnest(con.confkey) with ordinality AS rk(attnum, ord) on true
                           join pg_attribute ar on ar.attrelid = clr.oid and ar.attnum = rk.attnum and rk.ord = ck.ord
                  where ns.nspname not in %s and con.contype = 'f'
                  group by 1,2,3,4,5
                  order by 1,2,3
                  """, (tuple(IGNORED_SCHEMAS),))

  indexes = q(conn, """
                    select
                        ns.nspname as table_schema,
                        cl.relname as table_name,
                        idx.relname as index_name,
                        ix.indisunique as is_unique,
                        array_agg(att.attname order by ord.i) as columns
                    from pg_index ix
                             join pg_class idx on idx.oid = ix.indexrelid
                             join pg_class cl on cl.oid = ix.indrelid
                             join pg_namespace ns on ns.oid = cl.relnamespace
                             join LATERAL unnest(ix.indkey) with ordinality as ord(k, i) on true
                             join pg_attribute att on att.attrelid = cl.oid and att.attnum = ord.k
                    where ns.nspname not in %s
                    group by 1,2,3,4
                    order by 1,2,3
                    """, (tuple(IGNORED_SCHEMAS),))

  return {"tables": tables, "columns": columns, "constraints": constraints, "fkeys": fkeys, "indexes": indexes}

def main():
  if len(sys.argv) != 3:
    print("usage: catalog_snapshot.py <pg_url> <out.json>", file=sys.stderr); sys.exit(2)
  url, out = sys.argv[1], sys.argv[2]
  conn = psycopg2.connect(url)
  try:
    data = snapshot(conn)
    pathlib.Path(out).write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
  finally:
    conn.close()

if __name__ == "__main__":
  main()
