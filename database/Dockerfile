FROM mcr.microsoft.com/mssql/server:2022-latest

USER root

# Create init directory and copy the schema initialization script
WORKDIR /docker-init
COPY GarageManagementSystem.sql /docker-init/
COPY entrypoint.sh /docker-init/

RUN chmod +x /docker-init/entrypoint.sh

USER mssql

ENTRYPOINT ["/docker-init/entrypoint.sh"]
