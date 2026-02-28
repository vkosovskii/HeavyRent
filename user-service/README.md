# user-service

Microservice responsible for user profiles in HeavyRent. Consumes registration events from Kafka and exposes user data via gRPC.

## Responsibilities

- Consume `user.registered` events from Kafka and persist user profiles to PostgreSQL
- Expose gRPC API for querying and managing user profiles
- Idempotent processing — duplicate events are safely ignored

## Ports

| Protocol | Port | Description |
|----------|------|-------------|
| gRPC | 9091 | Internal service communication |
| REST | 8081 | (reserved, not yet active) |

## Architecture

```
Kafka topic: user.registered
        │
        ▼
UserRegistrationConsumer (@KafkaListener)
        │  deserializes JSON event
        ▼
UserProfileService
        │  findOrCreate by keycloakId (idempotent)
        ▼
UserProfileRepository (Spring Data JPA)
        │
        ▼
PostgreSQL · heavyrent_users · user_profiles table


gRPC Client (other services / Envoy)
        │
        ▼
UserGrpcServiceImpl
        │
        ▼
UserProfileService
        │
        ▼
PostgreSQL · heavyrent_users
```

## Kafka Consumer

Subscribes to `user.registered` topic. Expected event format:

```json
{
  "userId": "uuid",
  "eventType": "USER_REGISTERED",
  "role": "RENTER",
  "data": {
    "username": "alex.renter",
    "firstName": "Alex",
    "lastName": "Renter",
    "email": "alex@heavyrent.com",
    "emailVerified": true,
    "enabled": true,
    "attributes": {
      "phone": ["+7-999-999-99-99"]
    },
    "realmRoles": ["RENTER"]
  }
}
```

Events without `data` or `phone` are rejected with a warning log — they are old/malformed events.

## gRPC API

Defined in `grpc-contracts/src/main/proto/user.proto`.

```protobuf
service UserGrpcService {
  rpc GetUserByKeycloakId (GetUserByKeycloakIdRequest) returns (UserResponse);
  rpc GetUserByPublicId   (GetUserByPublicIdRequest)   returns (UserResponse);
}
```

### Test with grpcurl

```bash
# List services
grpcurl -plaintext localhost:9091 list

# Get user by Keycloak ID
grpcurl -plaintext \
  -d '{"keycloak_id": "your-keycloak-uuid"}' \
  localhost:9091 user.UserGrpcService/GetUserByKeycloakId
```

## Database

- **Database:** `heavyrent_users`
- **Host:** `localhost:5433`
- **User:** `user_service`
- **Table:** `user_profiles`

### Schema

| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Internal PK |
| public_id | UUID | External-facing ID |
| keycloak_id | UUID | Keycloak user ID (unique) |
| email | VARCHAR | User email |
| first_name | VARCHAR | First name |
| last_name | VARCHAR | Last name |
| phone | VARCHAR | Phone number |
| role | VARCHAR | RENTER or OWNER |
| status | VARCHAR | UNVERIFIED, ACTIVE, BLOCKED |
| is_verified | BOOLEAN | Email verified flag |
| created_at | TIMESTAMP | Created timestamp |
| updated_at | TIMESTAMP | Last updated timestamp |

## Configuration

`src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: user-service
  datasource:
    url: jdbc:postgresql://localhost:5433/heavyrent_users
    username: user_service
    password: user_secret
  jpa:
    hibernate:
      ddl-auto: update
  kafka:
    bootstrap-servers: localhost:29092
    consumer:
      group-id: user-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer

grpc:
  server:
    port: 9091
```

## Running

```bash
# Make sure infrastructure is up
docker compose up -d

# Start user-service
./gradlew :user-service:bootRun
```

Expected startup output:

```
Subscribed to topic(s): user.registered
gRPC Server started, listening on address: *, port: 9091
```

## Dependencies

```kotlin
implementation(project(":grpc-contracts"))
implementation("net.devh:grpc-server-spring-boot-starter:3.1.0.RELEASE")
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
implementation("org.springframework.boot:spring-boot-starter")
implementation("org.springframework.kafka:spring-kafka")
implementation("com.fasterxml.jackson.core:jackson-databind")
runtimeOnly("org.postgresql:postgresql")
compileOnly("org.projectlombok:lombok")
annotationProcessor("org.projectlombok:lombok")
```

> **Note:** Spring Boot 4.0 ships Kafka autoconfiguration in a separate `spring-boot-kafka` module.
> The `spring-boot-dependencies` BOM must be imported in the root `build.gradle.kts` to resolve the correct versions.