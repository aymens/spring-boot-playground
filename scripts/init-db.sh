#!/bin/bash
set -e

# Map users to their passwords
declare -A USER_PASSWORDS=(
    ["$APP_USER"]="$APP_PASSWORD"
    ["$KC_USER"]="$KC_PASSWORD"
)

declare -A DB_USERS=(
    ["$APP_DB"]="$APP_USER"
    ["$KC_DB"]="$KC_USER"
)

# Create databases if they don't exist
for DB in "${!DB_USERS[@]}"; do
    # Check if the database exists
    DB_EXISTS=$(psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -tAc "SELECT 1 FROM pg_database WHERE datname = '$DB'")

    # If the database does not exist, create it
    if [ -z "$DB_EXISTS" ]; then
        psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -c "CREATE DATABASE \"$DB\" WITH OWNER \"$POSTGRES_USER\""
    fi
done

# Create users if they don't exist and set their passwords
for USER in "${!USER_PASSWORDS[@]}"; do
    PASSWORD="${USER_PASSWORDS[$USER]}"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        DO
        \$\$
        BEGIN
            IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '$USER') THEN
                CREATE ROLE "$USER" WITH LOGIN PASSWORD '$PASSWORD';
            END IF;
        END
        \$\$;
EOSQL
done

# Grant privileges to users on their respective databases
for DB in "${!DB_USERS[@]}"; do
    USER="${DB_USERS[$DB]}"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$DB" <<-EOSQL
        GRANT ALL PRIVILEGES ON DATABASE "$DB" TO "$USER";
        GRANT ALL PRIVILEGES ON SCHEMA public TO "$USER";
EOSQL
done

## Grant privileges to users on their respective databases (only if needed)
#for DB in "${!DB_USERS[@]}"; do
#    USER="${DB_USERS[$DB]}"
#
#    # Check if the user already has privileges on the database
#    DB_PRIVILEGES_EXIST=$(psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$DB" -tAc "
#        SELECT 1
#        FROM pg_roles r
#        JOIN pg_auth_members m ON r.oid = m.roleid
#        JOIN pg_roles u ON m.member = u.oid
#        WHERE u.rolname = '$USER' AND r.rolname = '$USER'
#    ")
#
#    # If privileges do not exist, grant them
#    if [ -z "$DB_PRIVILEGES_EXIST" ]; then
#        psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$DB" <<-EOSQL
#            GRANT ALL PRIVILEGES ON DATABASE "$DB" TO "$USER";
#            GRANT ALL PRIVILEGES ON SCHEMA public TO "$USER";
#EOSQL
#    fi
#done