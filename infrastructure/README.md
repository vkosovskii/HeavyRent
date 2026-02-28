# Infrastructure

Local development infrastructure for HeavyRent. Includes Keycloak, Apache Kafka, PostgreSQL, and the Keycloak SPI plugin that bridges user registration events to Kafka.

## Components

| Component | Port | Description |
|-----------|------|-------------|
| Keycloak | :8080 | Identity Provider — OAuth2/OIDC, JWT, user management |
| keycloak-db | :5435 | PostgreSQL for Keycloak internal storage |
| Kafka (KRaft) | :9092 (internal), :29092 (external) | Event bus, no ZooKeeper |
| user-db | :5433 | PostgreSQL for user-service |
| equipment-db | :5434 | PostgreSQL for equipment-service |
| keycloak-kafka-plugin | — | Keycloak SPI JAR, deployed inside Keycloak container |

## Keycloak Kafka Plugin

Keycloak SPI (Service Provider Interface) plugin that intercepts user registration events and publishes them to Kafka.

### How it works

```
User registers in Keycloak
        │
        ▼
EventListenerProvider.onEvent(AdminEvent)
        │  type = CREATE, resourceType = USER
        ▼
Enrich with user attributes (phone, roles)
        │
        ▼
Publish to Kafka topic: user.registered
```

### Published event format

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

### Build & Deploy

```bash
# Build the plugin JAR
./gradlew :infrastructure:keycloak-kafka-plugin:build

# Restart Keycloak to pick up new JAR
docker compose restart keycloak
```

The built JAR is automatically mounted into the Keycloak container via Docker volume.

## Quick Start

```bash
# Start all infrastructure
docker compose up -d

# Check status
docker compose ps

# View Keycloak logs
docker compose logs -f keycloak

# View Kafka logs
docker compose logs -f kafka
```

## Keycloak Setup

After first launch, configure the `heavyrent` realm:

1. Open `http://localhost:8080`, login as `admin / admin`
2. Create realm: `heavyrent`
3. **Realm Settings → Events → Event Listeners** — add `kafka-event-listener`
4. **Realm Settings → Events → Admin Events** — enable `Save Events`, enable `Include Representation`
5. Create realm roles: `RENTER`, `OWNER`

## Kafka Topics

| Topic | Producer | Consumer | Description |
|-------|----------|----------|-------------|
| `user.registered` | keycloak-kafka-plugin | user-service | New user created in Keycloak |
| `user.verified` | user-service | — | User email verified |
| `user.role.changed` | user-service | — | User role updated |
| `equipment.location.updated` | equipment-service | — | GPS location update |
| `equipment.status.changed` | equipment-service | — | Equipment availability change |
| `order.created` | order-service | — | New rental order |
| `order.accepted` | order-service | — | Order accepted by owner |
| `order.completed` | order-service | — | Rental completed |

## Database Connections

| Database | Host | Port | DB Name | User | Password |
|----------|------|------|---------|------|----------|
| keycloak-db | localhost | 5435 | keycloak | keycloak | keycloak_secret |
| user-db | localhost | 5433 | heavyrent_users | user_service | user_secret |
| equipment-db | localhost | 5434 | heavyrent_equipment | equipment_service | equipment_secret |

## Kafka Listener Configuration

Kafka uses dual listeners to support both internal Docker communication and external host access:

| Listener | Address | Used by |
|----------|---------|---------|
| `PLAINTEXT` | `kafka:9092` | Docker services (Keycloak plugin) |
| `EXTERNAL` | `localhost:29092` | Host machine (Spring Boot services in dev) |

## Useful Commands

```bash
# List Kafka topics
docker exec -it heavyrent-kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 --list

# Consume from user.registered topic
docker exec -it heavyrent-kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user.registered \
  --from-beginning

# Keycloak DB — list users
docker exec -it heavyrent-keycloak-db psql -U keycloak -d keycloak \
  -c "SELECT id, username, email FROM user_entity WHERE realm_id = 'heavyrent';"

# Restart all
docker compose down && docker compose up -d
```