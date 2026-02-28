# HeavyRent — Dev Commands

## Docker


### Поднять весь стек
```bash
   docker compose up -d
```

### Поднять конкретный сервис
```bash
   docker compose up -d kafka
```
```bash
   docker compose up -d keycloak
```

### Остановить всё
```bash
   docker compose down
```
### Перезапустить конкретный сервис
```bash
   docker compose restart keycloak
```
### Логи конкретного сервиса
```bash
   docker compose logs -f keycloak
```
```bash
   docker compose logs -f kafka
```
### Фильтрация логов
```bash
   docker compose logs keycloak | grep -i "kafka"
```
```bash
   docker compose logs keycloak | grep -i "error\|kafka\|producer" | tail -20
```
```bash
   docker compose logs keycloak | grep -i "register" | tail -10
```

---

## Gradle
### Собрать Keycloak Kafka плагин

```bash
  ./gradlew :infrastructure:keycloak-kafka-plugin:jar
```
### Собрать Keycloak Kafka плагин Собрать конкретный сервис
```bash
   ./gradlew :user-service:build
```
```bash
   ./gradlew :equipment-service:build
```

### Собрать Keycloak Kafka плагинСобрать весь проект
```bash
   ./gradlew build
```
### Собрать Keycloak Kafka плагин Запустить тесты
```bash 
  ./gradlew test
```
```bash
   ./gradlew :user-service:test
```

---

## Keycloak — Admin API


### Получить admin токен (master realm)
```bash
  TOKEN=$(curl -s -X POST 'http://localhost:8080/realms/master/protocol/openid-connect/token' \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    -d 'client_id=admin-cli&username=admin&password=admin&grant_type=password' \
    | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

### Проверить что токен получен

   echo "Token received: ${TOKEN:0:20}..."

### Создать пользователя

  curl -s -o /dev/null -w "%{http_code}" \
    -X POST 'http://localhost:8080/admin/realms/heavyrent/users' \
    -H "Authorization: Bearer $TOKEN" \
    -H 'Content-Type: application/json' \
    -d '{
          "username": "alex.renter",
          "email": "alex@heavyrent.com",
          "firstName": "Alex",
          "lastName": "Renter",
          "enabled": true,
          "emailVerified": true,
          "realmRoles": ["RENTER"],
          "attributes": {
             "phone": ["+7-999-999-99-99"]
          },
          "credentials": [{"type": "password", "value": "Test1234!", "temporary": false}]
        }'

### Получить список пользователей

  curl -s 'http://localhost:8080/admin/realms/heavyrent/users' \
    -H "Authorization: Bearer $TOKEN" | jq .
```

---

## Keycloak — OAuth2


### Получить JWT токен (password flow, только для тестирования)
```bash
  curl -X POST 'http://localhost:8080/realms/heavyrent/protocol/openid-connect/token' \
   -H 'Content-Type: application/x-www-form-urlencoded' \
   -d 'grant_type=password&client_id=heavyrent-web&username=test&password=Test1234!'
```

---

## Kafka


### Слушать топик user.registered
```bash
  docker exec -it heavyrent-kafka kafka-console-consumer \
    --bootstrap-server kafka:9092 \
    --topic user.registered \
     --from-beginning
```
### Список топиков
```bash
  docker exec -it heavyrent-kafka kafka-topics \
    --bootstrap-server kafka:9092 \
    --list
```
### Создать топик вручную
```bash
  docker exec -it heavyrent-kafka kafka-topics \
    --bootstrap-server kafka:9092 \
    --create \
    --topic user.registered \
    --partitions 1 \
    --replication-factor 1
```
### Отправить сообщение в топик вручную
```bash
  docker exec -it heavyrent-kafka kafka-console-producer \
    --bootstrap-server kafka:9092 \
    --topic user.registered
```

---

## PostgreSQL


### Подключиться к keycloak-db
```bash
  docker exec -it heavyrent-keycloak-db psql -U keycloak -d keycloak
```
### Подключиться к user-db
```bash
  docker exec -it heavyrent-user-db psql -U user_service -d heavyrent_users
```
### Подключиться к equipment-db
```bash
  docker exec -it heavyrent-equipment-db psql -U equipment_service -d heavyrent_equipment
```

---

## Полный цикл — пересобрать плагин и перезапустить Keycloak

```bash
  ./gradlew :infrastructure:keycloak-kafka-plugin:jar && \
  docker compose restart keycloak && \
  docker compose logs -f keycloak | grep -i kafka
```