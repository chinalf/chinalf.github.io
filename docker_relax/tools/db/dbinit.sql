set v_dbname itone

DROP DATABASE IF EXISTS :v_dbname;
CREATE DATABASE :v_dbname;
ALTER DATABASE :v_dbname SET search_path = public, platform, itone_data, itone;

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS dblink;
CREATE EXTENSION IF NOT EXISTS ltree;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS plpgsql;
CREATE EXTENSION IF NOT EXISTS pg_partman;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
ALTER TABLE part_config ALTER COLUMN infinite_time_partitions SET DEFAULT true;
