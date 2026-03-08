# equipment-service

Microservice for managing heavy equipment catalog in the HeavyRent platform.

## Overview

Handles equipment profiles: creation, retrieval, and filtered listing. Communicates with `user-service` via gRPC for owner identity resolution. Exposes REST via Envoy gRPC-JSON transcoding.

## Ports

| Protocol | Port |
|----------|------|
| gRPC     | 9092 |
| REST     | 8082 |

## API

All REST endpoints are exposed via **Envoy Gateway** at `localhost:8000`.

### Create Equipment
```
POST /api/equipment
Authorization: Bearer <JWT>  (OWNER role required)
```

**Request body (JSON):**
```json
{
  "name": "Гусеничный экскаватор CAT 320",
  "type": "EXCAVATOR",
  "registrationNumber": "EXC-2024-001",
  "brand": "Caterpillar",
  "model": "320",
  "pricePerHourCents": 150000,
  "yearOfManufacture": 2020,
  "hasOperator": true,
  "hasAccreditation": true,
  "deliveryType": "SELF_PROPELLED",
  "equipmentStatus": "FREE",
  "availableFrom": "2026-03-10T00:00:00Z",
  "latitude": 55.7558,
  "longitude": 37.6173
}
```

### Get Equipment by ID
```
GET /api/equipment/{equipmentId}
Authorization: not required
```

### List Equipment (with filters)
```
GET /api/equipment?type=EXCAVATOR&page=0&page_size=20
Authorization: not required
```

**Query parameters:**

| Parameter              | Type    | Description                        |
|------------------------|---------|------------------------------------|
| `type`                 | string  | Equipment type enum                |
| `equipment_status`     | string  | Equipment status enum              |
| `model`                | string  | Model name (partial match)         |
| `name`                 | string  | Equipment name (partial match)     |
| `owner_id`             | string  | Owner public UUID                  |
| `max_price_per_hour_cents` | int | Max price filter                   |
| `page`                 | int     | Page number (default: 0)           |
| `page_size`            | int     | Page size (default: 20)            |

## Enums

### EquipmentType
`EQUIPMENT_TYPE_UNSPECIFIED`, `CRANE`, `TRUCK`, `BULLDOZER`, `LOADER`, `COMPACTOR`, `CONCRETE_MIXER`, `GENERATOR`, `EXCAVATOR`, `OTHER`

### EquipmentStatus
`EQUIPMENT_STATUS_UNSPECIFIED`, `ON_SITE`, `MAINTENANCE`, `UNAVAILABLE`, `FREE`, `STATUS_NOT_DETERMINED`

### DeliveryType
`DELIVERY_TYPE_UNSPECIFIED`, `TRAILER_NEEDED`, `DELIVERY_AVAILABLE`, `SELF_PROPELLED`, `DELIVERY_NOT_DETERMINED`

## Architecture

```
Client
  │ REST
  ▼
Envoy Gateway :8000
  │ gRPC-JSON transcoding + JWT validation
  ▼
EquipmentGrpcServiceImpl
  │
  ├── UserContextInterceptor   (extracts x-user-id, x-user-role from headers)
  ├── EquipmentProfileService  (business logic)
  ├── UserServiceClient        (gRPC call to user-service for owner resolution)
  └── EquipmentProfileRepository (JPA + Specification API)
```

## Authorization

- JWT validation is performed by **Envoy** via JWKS from Keycloak
- `POST /api/equipment` requires a valid JWT with role `OWNER`
- `GET` endpoints are public — no token required
- Role check inside handler via `UserContextHolder` (extracted from Envoy-injected headers `x-user-id`, `x-user-role`)

## Inter-service Communication

`equipment-service` calls `user-service` via gRPC to resolve the owner's public UUID from Keycloak ID.

The `UserContextClientInterceptor` propagates `x-user-id` and `x-user-role` headers from the current gRPC context into outgoing requests.

```
equipment-service
  │ gRPC (UserContextClientInterceptor forwards headers)
  ▼
user-service :9091
```

## Database

| Property  | Value                  |
|-----------|------------------------|
| Engine    | PostgreSQL 16          |
| Port      | 5434                   |
| DB name   | heavyrent_equipment    |
| User      | equipment_service      |
| ORM       | Hibernate / Spring JPA |

Table: `equipment_profiles`

## Configuration

```yaml
# application.yml (key settings)
server.port: 8082
grpc.server.port: 9092

spring.datasource.url: jdbc:postgresql://localhost:5434/heavyrent_equipment

grpc.client.user-service.address: static://localhost:9091
grpc.client.user-service.negotiation-type: plaintext
```

## Running

```bash
# Start infrastructure first
docker compose up -d

# Run service
./gradlew :equipment-service:bootRun
```

## Tech Debt

| Item | Priority |
|------|----------|
| Migrate from `ddl-auto: update` to Flyway | High |
| `availableFrom` — use wrapper type in proto for nullable timestamps | Medium |
| Kafka events for equipment lifecycle (`equipment.created`, `equipment.status.changed`) | Medium |