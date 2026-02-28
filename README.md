# HeavyRent

Platform for renting heavy equipment. Built on Java microservices with Keycloak, Kafka, gRPC, and PostgreSQL.

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│         Mobile App (iOS/Android) · Web App · Admin Panel     │
└──────────────┬───────────────────────────────┬───────────────┘
               │ REST / WebSocket              │ REST / WebSocket
    ┌──────────▼──────────────────────────────▼───────────┐
    │                    Envoy Proxy                      │  (planned)
    │         Single entry point · :443 / :80             │
    │   REST→gRPC transcoding · TLS · Rate Limiting       │
    └──────────────────────┬──────────────────────────────┘
                           │ gRPC (internal)
      ┌────────────────────┼──────────────┬────────────────┐
      │                    │              │                │
┌─────▼──────┐  ┌──────────▼───┐  ┌──────▼──────┐  ┌─────▼───────┐
│  Keycloak  │  │ user-service │  │ equipment-  │  │   order-    │
│   :8080    │  │  gRPC :9091  │  │  service    │  │   service   │
│ OAuth2/OIDC│  │  REST :8081  │  │ gRPC :9092  │  │ gRPC :9093  │
│    JWT     │  │              │  │ REST :8082  │  │  (planned)  │
└─────┬──────┘  └──────┬───────┘  └──────┬──────┘  └─────────────┘
      │                │                 │
      │         ┌──────▼─────────────────▼────────────────┐
      │         │             Kafka Event Bus             │
      │         │   user.* · equipment.* · order.*        │
      │         │   notification.* · chat.*               │
      │         └─────────────────────────────────────────┘
      │                │                 │
┌─────▼──────┐  ┌──────▼───────┐  ┌─────▼──────────┐
│ keycloak-db│  │heavyrent_    │  │heavyrent_      │
│ postgres   │  │users         │  │equipment       │
│ :5435      │  │postgres :5433│  │postgres :5434  │
└────────────┘  └──────────────┘  └────────────────┘
```

## Services

| Service | gRPC | REST | Description |
|---------|------|------|-------------|
| [Keycloak](./infrastructure/README.md) | — | :8080 | Identity Provider, OAuth2/OIDC, JWT |
| [user-service](./user-service/README.md) | :9091 | :8081 | User profiles, Kafka consumer |
| equipment-service | :9092 | :8082 | Equipment catalog, GPS, pricing |
| order-service | :9093 | — | Rental orders (planned) |
| notification-service | :9094 | — | Email / SMS / Push (planned) |

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.2 |
| Identity | Keycloak 26.0 |
| Messaging | Apache Kafka 4.1.1 (KRaft) |
| RPC | gRPC 1.68 + Protobuf |
| API Gateway | Envoy Proxy (planned) |
| Database | PostgreSQL 16 (per-service) |
| ORM | Hibernate 7 / Spring Data JPA |
| Build | Gradle 9 (multi-module) |
| Containers | Docker Compose |
| Misc | Lombok |

## Prerequisites

- Java 21+
- Docker + Docker Compose
- Gradle 9+

## Quick Start

### 1. Clone

```bash
git clone https://github.com/your-org/heavyrent.git
cd heavyrent
```

### 2. Start infrastructure (Keycloak + Kafka + PostgreSQL)

```bash
docker compose up -d
```

### 3. Build and deploy Keycloak SPI plugin

```bash
./gradlew :infrastructure:keycloak-kafka-plugin:build
docker compose restart keycloak
```

### 4. Configure Keycloak

Открой `http://localhost:8080`, войди как `admin / admin`:
- Создай realm `heavyrent`
- Включи Admin Events с `Include Representation`
- Добавь `kafka-event-listener` в Event Listeners realm-а
- Создай realm roles: `RENTER`, `OWNER`

### 5. Start services

```bash
# user-service
./gradlew :user-service:bootRun

# equipment-service (отдельный терминал)
./gradlew :equipment-service:bootRun
```

## Registration Event Flow

```
User registered via Keycloak Admin API / UI
              │
              ▼
  Keycloak SPI Plugin (keycloak-kafka-plugin)
              │  publishes JSON event
              ▼
    Kafka topic: user.registered
              │
              ▼
  user-service @KafkaListener
              │  idempotent upsert
              ▼
  heavyrent_users · PostgreSQL
```

## Project Structure

```
HeavyRent/
├── infrastructure/
│   └── keycloak-kafka-plugin/   # Keycloak SPI — publishes events to Kafka
├── grpc-contracts/              # Protobuf definitions + generated gRPC stubs
├── user-service/                # User profiles service
├── equipment-service/           # Equipment catalog service
├── docker-compose.yml           # Full local infrastructure stack
├── build.gradle.kts             # Root Gradle config
└── settings.gradle.kts          # Module declarations
```

## Development Status

| Feature | Status |
|---------|--------|
| Keycloak IAM (OAuth2/OIDC) | ✅ Done |
| Kafka event bus (KRaft, no ZooKeeper) | ✅ Done |
| Keycloak SPI plugin → Kafka | ✅ Done |
| user-service (gRPC + Kafka consumer + PostgreSQL) | ✅ Done |
| grpc-contracts (Protobuf) | ✅ Done |
| equipment-service | 🚧 In progress |
| Envoy Proxy (REST→gRPC transcoding) | 🔜 Planned |
| order-service | 🔜 Planned |
| notification-service | 🔜 Planned |
| API Gateway authentication (JWT validation) | 🔜 Planned |