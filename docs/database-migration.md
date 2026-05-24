# Database Migration

The production database was migrated from Supabase PostgreSQL to private Amazon RDS PostgreSQL.

## Main Lessons

- Dump with a PostgreSQL client version that is equal to or newer than the source server version.
- If the source database uses pgvector, enable `vector` on the destination before restore.
- Restore warnings should be inspected. Missing extensions can cause dependent tables and indexes to be skipped.

## Dump Example

```bash
docker run --rm \
  -v "$PWD:/work" \
  postgres:17 \
  pg_dump "postgresql://<user>:<password>@<source-host>:5432/<db>?sslmode=require" \
  --schema=public \
  --no-owner \
  --no-privileges \
  --format=custom \
  --file=/work/source-public.dump
```

## Destination Setup

```sql
create extension if not exists vector;
```

## Restore Example

```bash
docker run --rm -it \
  -v "$PWD:/work" \
  postgres:17 \
  pg_restore \
  --host=<rds-endpoint> \
  --port=5432 \
  --dbname=<db> \
  --username=<username> \
  --no-owner \
  --no-privileges \
  --clean \
  --if-exists \
  /work/source-public.dump
```

If `public` cannot be dropped because an extension depends on it, verify whether the tables, indexes, and data were restored before rerunning the restore.

## Verification

```sql
select extname from pg_extension order by extname;

select table_name
from information_schema.tables
where table_schema = 'public'
order by table_name;

select tablename, indexname, indexdef
from pg_indexes
where schemaname = 'public'
order by tablename, indexname;
```

Important RAG objects:

- `rag_document`
- `rag_import_job`
- `rag_chunk`
- `idx_rag_chunk_tsv`
- `idx_rag_chunk_embedding_vector`