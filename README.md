# permission-service

A Spring Boot service that exposes a REST API on top of [SpiceDB](https://authzed.com/docs) (via the Authzed gRPC client) for managing relationship-based access control (ReBAC) permissions.

## Tech stack

- Java 25 (via Gradle toolchain)
- Spring Boot 4.1.1 (Web MVC, Validation)
- Authzed / SpiceDB gRPC client
- Gradle wrapper

## Prerequisites

- JDK 25
- Docker (for running SpiceDB and its MySQL datastore locally)

## Running locally

1. Start SpiceDB and its datastore:

   ```bash
   docker-compose up -d
   ```

   This brings up:
   - `mysql` — the datastore backing SpiceDB
   - `migrate` — runs SpiceDB schema migrations against MySQL
   - `spicedb` — the SpiceDB server, exposing gRPC (`50051`), HTTP (`8443`), and metrics (`9090`)

2. Configure the connection in `src/main/resources/application.yml`:

   ```yaml
   authzed:
     target: localhost:50051
     token: foobar
   ```

   The `token` must match the `SPICEDB_GRPC_PRESHARED_KEY` set for the `spicedb` service in `docker-compose.yml`.

3. Run the application:

   ```bash
   ./gradlew bootRun
   ```

## Schema

The authorization schema is defined in [`src/main/resources/schema/schema.zed`](src/main/resources/schema/schema.zed) using SpiceDB's schema language. It currently models `user`, `group`, `folder`, and `document` object types with `owner`/`editor`/`viewer` relations and `edit`/`view` permissions, including folder hierarchy inheritance.

