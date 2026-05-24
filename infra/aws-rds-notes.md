# AWS RDS Notes

This project uses a private Amazon RDS PostgreSQL instance for production data.

## Network Model

- EC2 and RDS are placed in the same AWS region and VPC.
- RDS public access is disabled.
- The RDS security group allows PostgreSQL port `5432` from the EC2 security group, not from the whole internet.
- Developers can connect with DBeaver through an SSH tunnel via EC2.

## Security Group Rule

RDS inbound rule:

```text
Type: PostgreSQL
Protocol: TCP
Port: 5432
Source: <EC2 security group id>
```

This means: any EC2 instance attached to that security group can connect to RDS.

## Required Extension

The RAG feature stores embeddings in PostgreSQL through pgvector. Enable it before restoring schema/data:

```sql
create extension if not exists vector;
```

Without this extension, tables and indexes that use the `vector` type can fail during restore.

## Useful Checks

```sql
select extname from pg_extension order by extname;

select table_name
from information_schema.tables
where table_schema = 'public'
  and table_name like 'rag%'
order by table_name;

select tablename, indexname, indexdef
from pg_indexes
where schemaname = 'public'
  and tablename in ('rag_chunk', 'rag_document', 'products')
order by tablename, indexname;
```