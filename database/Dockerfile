FROM mcr.microsoft.com/mssql/server:2022-latest

USER root

# Create init directory and copy SQL initialization and seed scripts
WORKDIR /docker-init
COPY GarageManagementSystem_modified.sql /docker-init/
COPY seed /docker-init/seed/
COPY entrypoint.sh /docker-init/

RUN chmod +x /docker-init/entrypoint.sh

USER mssql

ENTRYPOINT ["/docker-init/entrypoint.sh"]
