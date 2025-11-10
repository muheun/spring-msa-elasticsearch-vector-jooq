#!/bin/bash
set -e

echo "====================================="
echo "PostgreSQL 확장 설치 시작"
echo "====================================="

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    CREATE EXTENSION IF NOT EXISTS pg_trgm;

    SELECT extname, extversion FROM pg_extension;
EOSQL

echo "====================================="
echo "PostgreSQL 확장 설치 완료"
echo "====================================="
