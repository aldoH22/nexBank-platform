-- ============================================
--       CREAR BASES DE DATOS MYSQL
-- ============================================

-- Base de datos para auth-service
CREATE DATABASE IF NOT EXISTS nexbank_auth;

-- Base de datos para account-service
CREATE DATABASE IF NOT EXISTS nexbank_accounts;

-- Usuario para los servicios
CREATE USER IF NOT EXISTS 'nexbank_user'@'%' IDENTIFIED BY 'nexbank_password';

-- Otorgar permisos
GRANT ALL PRIVILEGES ON nexbank_auth.* TO 'nexbank_user'@'%';
GRANT ALL PRIVILEGES ON nexbank_accounts.* TO 'nexbank_user'@'%';

FLUSH PRIVILEGES;

-- Confirmar creación
SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'nexbank%';