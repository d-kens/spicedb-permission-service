# permission-service

A Spring Boot service that exposes a REST API on top of [SpiceDB](https://authzed.com/docs) (via the Authzed gRPC client) for managing relationship-based access control (ReBAC) permissions.

## Tech stack

- Java 25 (via Gradle toolchain)
- Spring Boot 4.1.1 (Web MVC, Validation)
- springdoc-openapi (Swagger UI)
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
     token: somerandomdevkey
   ```

   The `token` must match the `SPICEDB_GRPC_PRESHARED_KEY` set for the `spicedb` service in `docker-compose.yml`.

3. Load the schema into SpiceDB (the service does not write it itself), for example with [`zed`](https://authzed.com/docs/spicedb/getting-started/installing-zed) via Docker:

   ```bash
   docker run --rm --network host -v "$PWD/src/main/resources/schema:/schema" authzed/zed:latest \
     schema write /schema/schema.zed --endpoint localhost:50051 --token somerandomdevkey --insecure
   ```

4. Run the application:

   ```bash
   ./gradlew bootRun
   ```

   The API is served on `http://localhost:8080`, with Swagger UI at `http://localhost:8080/swagger-ui.html`.

## API

All endpoints live under `/permissions` and take a JSON body.

| Method | Path | Description |
|---|---|---|
| `POST` | `/permissions` | Assign a permission by writing a relationship. Returns the ZedToken of the write. |
| `DELETE` | `/permissions` | Revoke a permission by deleting a relationship. Returns the ZedToken of the delete. Succeeds even if the relationship did not exist. |
| `POST` | `/permissions/check` | Check whether a subject has a permission on a resource. |
| `POST` | `/permissions/check/bulk` | Check several resource permissions for one subject in a single call. |
| `POST` | `/permissions/lookup/resources` | List the IDs of all resources of a type on which a subject has a permission. |

Assign and revoke take a relationship:

```json
{
  "resourceType": "document",
  "resourceId": "1",
  "relation": "viewer",
  "subjectType": "user",
  "subjectId": "123",
  "optionalSubjectRelation": null
}
```

Set `optionalSubjectRelation` (for example `member` with subject `group:engineering`) to grant the relation to a subject set. Revoke ignores it.

Check:

```json
{ "resourceType": "document", "resourceId": "1", "permission": "view", "subjectType": "user", "subjectId": "123" }
```

returns the request fields plus `"authorized": true | false`.

Bulk check:

```json
{
  "subjectType": "user",
  "subjectId": "123",
  "items": [
    { "resourceType": "document", "resourceId": "1", "permission": "view" },
    { "resourceType": "document", "resourceId": "2", "permission": "edit" }
  ]
}
```

returns one `{ resourceType, resourceId, permission, authorized }` per item. An item SpiceDB fails to evaluate is returned as not authorized.

Lookup resources:

```json
{ "resourceType": "document", "permission": "view", "subjectType": "user", "subjectId": "123" }
```

returns an array of resource IDs, for example `["1", "42"]`. Results are not paginated.

Ready-made requests for every endpoint are in [`collection/permissions.http`](collection/permissions.http) (IntelliJ HTTP Client format).

## Errors

Errors are returned as [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem details:

- **Validation failures** return `400` with `title: "Validation failed"` and every message in `errors`, for example `items[0] permission is required`.
- **Malformed or missing JSON** returns `400` with `title: "Malformed request"`.
- **SpiceDB errors** are mapped by status: invalid requests (such as an unknown permission) return `400` with SpiceDB's message, assigning a relationship that already exists returns `409`, an unreachable or timed-out SpiceDB returns `503`, and anything else returns `500` with a generic message.

```json
{
  "title": "Validation failed",
  "status": 400,
  "detail": "permission is required",
  "instance": "/permissions/lookup/resources",
  "errors": ["permission is required"]
}
```

## Schema

The authorization schema is defined in [`src/main/resources/schema/schema.zed`](src/main/resources/schema/schema.zed) using SpiceDB's schema language. It currently models `user`, `group`, `folder`, and `document` object types with `owner`/`editor`/`viewer` relations and `edit`/`view` permissions, including folder hierarchy inheritance.


### Updating the schema

SpiceDB does not read `schema.zed`; it keeps its own copy in its datastore. After changing the file, send the new version to SpiceDB from the project root:

```bash
docker run --rm --network host -v "$PWD/src/main/resources/schema:/schema" authzed/zed:latest \
  schema write /schema/schema.zed --endpoint localhost:50051 --token somerandomdevkey --insecure
```

SpiceDB uses the new schema immediately; neither SpiceDB nor the app needs a restart.
