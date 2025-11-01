# 🧩 Database Setup — Microsoft SQL Server 2022 (Docker)

This guide shows how to spin up **SQL Server 2022** inside Docker, create the `marketplace` database, and connect from **VS Code** or a **Spring Boot** app.

---

## ⚙️ Prerequisites

- Docker Desktop installed
- VS Code with **SQL Server (mssql)** extension
- Java 17+ for your Spring Boot app

---

## 🚀 1. Start SQL Server in Docker

Create a file named `docker-compose.yml` at your project root:

```yaml
services:
  mssql:
    platform: linux/amd64
    image: mcr.microsoft.com/mssql/server:2022-latest
    container_name: mssql
    environment:
      ACCEPT_EULA: "Y"
      MSSQL_SA_PASSWORD: "Str0ng_P@ssw0rd!"
      MSSQL_PID: "Developer"
    ports:
      - "1433:1433"
    volumes:
      - mssql-data:/var/opt/mssql

volumes:
  mssql-data:
```

Start the container:

```sh
docker compose up -d
```

Check that the container is running:

```sh
docker ps
```

---

## 🗄️ 2. Initialize Database & User

Run the following commands to set up the database and user:

```sh
# Check SQL Server version
docker exec -it mssql /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'Str0ng_P@ssw0rd!' -C \
  -Q "SELECT @@VERSION;"

# Create 'marketplace' database if it doesn't exist
docker exec -it mssql /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'Str0ng_P@ssw0rd!' -C \
  -Q "IF DB_ID('marketplace') IS NULL CREATE DATABASE marketplace;"

# Create login 'market_user' if it doesn't exist
docker exec -it mssql /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'Str0ng_P@ssw0rd!' -C -d master \
  -Q "IF NOT EXISTS (SELECT * FROM sys.sql_logins WHERE name='market_user') CREATE LOGIN market_user WITH PASSWORD='MarketPass1234', DEFAULT_DATABASE=marketplace;"

# Create user in 'marketplace' DB and grant db_owner
docker exec -it mssql /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'Str0ng_P@ssw0rd!' -C -d marketplace \
  -Q "IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name='market_user') CREATE USER market_user FOR LOGIN market_user; EXEC sp_addrolemember 'db_owner','market_user';"

# Test login as 'market_user'
docker exec -it mssql /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U market_user -P MarketPass1234 -C -d marketplace \
  -Q "SELECT DB_NAME();"
```

---

## 🖥️ 3. Connect from VS Code

Use the following settings in the **SQL Server (mssql)** extension:

| Field                  | Value           |
|------------------------|-----------------|
| Server                 | localhost       |
| Port                   | 1433            |
| Authentication         | SQL Login       |
| Username               | market_user     |
| Password               | MarketPass1234  |
| Encrypt                | True            |
| Trust Server Certificate | True          |
| Database               | marketplace     |

---

## 🌱 4. Spring Boot Configuration

Add the following to your `application.properties`:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=marketplace;encrypt=true;trustServerCertificate=true
spring.datasource.username=market_user
spring.datasource.password=MarketPass1234
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

---

You are now ready to use SQL Server 2022 with your Spring Boot app and connect via VS Code!
