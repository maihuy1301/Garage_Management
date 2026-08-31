#!/bin/bash
set -e

# Start SQL Server in background
/opt/mssql/bin/sqlservr &
MSSQL_PID=$!

# Function to locate sqlcmd
find_sqlcmd() {
    if [ -f /opt/mssql-tools18/bin/sqlcmd ]; then
        echo "/opt/mssql-tools18/bin/sqlcmd -C -b"
    elif [ -f /opt/mssql-tools/bin/sqlcmd ]; then
        echo "/opt/mssql-tools/bin/sqlcmd -b"
    elif command -v sqlcmd >/dev/null 2>&1; then
        echo "sqlcmd -b"
    else
        echo "sqlcmd -b"
    fi
}

SQLCMD=$(find_sqlcmd)

# Background initialization routine
(
    echo "Waiting for SQL Server to start accepting connections..."
    for i in {1..90}; do
        if $SQLCMD -S localhost -U sa -P "$SA_PASSWORD" -Q "SELECT 1" >/dev/null 2>&1; then
            echo "SQL Server is ready (attempt $i)."
            break
        fi
        sleep 2
    done

    # Check if database already exists
    DB_CHECK=$($SQLCMD -S localhost -U sa -P "$SA_PASSWORD" -h -1 -Q "SET NOCOUNT ON; IF DB_ID('GarageManagementSystem') IS NOT NULL SELECT 'EXISTS' ELSE SELECT 'NOT_EXISTS'" 2>/dev/null | tr -d '[:space:]')

    if [ "$DB_CHECK" = "EXISTS" ]; then
        echo "Database 'GarageManagementSystem' already exists. Skipping initialization."
    else
        echo "Database 'GarageManagementSystem' not found. Running initialization scripts..."

        if [ -f /docker-init/GarageSystemDB.sql ]; then
            echo "-> Executing /docker-init/GarageSystemDB.sql..."
            $SQLCMD -S localhost -U sa -P "$SA_PASSWORD" -i /docker-init/GarageSystemDB.sql
        fi

        if [ -f /docker-init/seed/V01__development_seed.sql ]; then
            echo "-> Executing /docker-init/seed/V01__development_seed.sql..."
            $SQLCMD -S localhost -U sa -P "$SA_PASSWORD" -i /docker-init/seed/V01__development_seed.sql
        fi

        if [ -f /docker-init/seed/V02__restore_original_roles.sql ]; then
            echo "-> Executing /docker-init/seed/V02__restore_original_roles.sql..."
            $SQLCMD -S localhost -U sa -P "$SA_PASSWORD" -i /docker-init/seed/V02__restore_original_roles.sql
        fi

        echo "Database initialization completed successfully!"
    fi
) &

# Keep container alive by waiting for sqlservr PID
wait "$MSSQL_PID"
