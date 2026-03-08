
# HeavyRent — Dev CLI Commands

---

# Docker

## Start Full Stack

```bash
cd deploy
```

```bash
docker compose up -d
```

---

## Start Individual Services

```bash
docker compose up -d kafka
```

```bash
docker compose up -d keycloak
```

```bash
docker compose up -d equipment-db
```

---

## Reset Stack (remove volumes)

```bash
docker compose down -v
```

```bash
docker compose up -d
```

---

## Restart Services

```bash
docker compose restart keycloak
```

```bash
docker compose restart envoy
```

---

## Logs

```bash
docker compose logs -f keycloak
```

```bash
docker compose logs -f kafka
```

```bash
docker compose logs keycloak | grep -i kafka
```

```bash
docker compose logs keycloak | grep -i "error\|kafka\|producer" | tail -20
```

```bash
docker compose logs keycloak | grep -i "register" | tail -10
```

---

# Gradle

## Generate gRPC Descriptor

```bash
./gradlew :grpc-contracts:generateProto
```

---

## Build Keycloak Plugin

```bash
./gradlew :infrastructure:keycloak-kafka-plugin:jar
```

---

## Build Services

```bash
./gradlew :user-service:build
```

```bash
./gradlew :equipment-service:build
```

```bash
./gradlew build
```

---

## Run Services

```bash
./gradlew :user-service:bootRun
```

```bash
./gradlew :equipment-service:bootRun
```

---

## Run Tests

```bash
./gradlew test
```

```bash
./gradlew :user-service:test
```

---

# Keycloak

## Get Admin Token

```bash
TOKEN=$(curl -s -X POST 'http://localhost:8080/realms/master/protocol/openid-connect/token'   -H 'Content-Type: application/x-www-form-urlencoded'   -d 'client_id=admin-cli&username=admin&password=admin&grant_type=password'   | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)
```

---

## Create User via Admin API

```bash
curl -s -X POST 'http://localhost:8080/admin/realms/heavyrent/users'   -H "Authorization: Bearer $TOKEN"   -H 'Content-Type: application/json'   -d '{
    "username": "test.owner",
    "email": "owner@heavyrent.com",
    "firstName": "Owner",
    "lastName": "Test",
    "enabled": true,
    "emailVerified": true,
    "attributes": {"phone": ["+7-999-000-11-22"]},
    "credentials": [{"type": "password", "value": "Test1234!", "temporary": false}]
  }'
```

---

## Get JWT Token (password flow)

```bash
curl -X POST 'http://localhost:8080/realms/heavyrent/protocol/openid-connect/token'   -H 'Content-Type: application/x-www-form-urlencoded'   -d 'grant_type=password&client_id=heavyrent-web&username=test&password=Test1234!'
```

---

# Kafka

## List Topics

```bash
docker exec -it heavyrent-kafka kafka-topics   --bootstrap-server kafka:9092   --list
```

---

## Consume Topic

```bash
docker exec -it heavyrent-kafka kafka-console-consumer   --bootstrap-server kafka:9092   --topic user.registered   --from-beginning
```

---

## Create Topic

```bash
docker exec -it heavyrent-kafka kafka-topics   --bootstrap-server kafka:9092   --create   --topic user.registered   --partitions 1   --replication-factor 1
```

---

## Produce Message

```bash
docker exec -it heavyrent-kafka kafka-console-producer   --bootstrap-server kafka:9092   --topic user.registered
```

---

# PostgreSQL

## Connect to Keycloak DB

```bash
docker exec -it heavyrent-keycloak-db psql -U keycloak -d keycloak
```

---

## Connect to User DB

```bash
docker exec -it heavyrent-user-db psql -U user_service -d heavyrent_users
```

---

## Connect to Equipment DB

```bash
docker exec -it heavyrent-equipment-db psql -U equipment_service -d heavyrent_equipment
```
