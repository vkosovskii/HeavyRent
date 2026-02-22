# HeavyRent

Platform for renting heavy equipment. Built on Spring Boot microservices with OAuth2/OIDC authentication.

## Architecture

```
┌─────────────────────────────────────────────┐
│            Client (Mobile / Web)            │
└─────────────────────┬───────────────────────┘
                      │
          ┌───────────▼───────────┐
          │      API Gateway      │  (planned)
          └───────────┬───────────┘
                      │
        ┌─────────────┼─────────────┐
        │                           │
┌───────▼────────┐       ┌──────────▼───────────┐
│  auth-service  │       │    user-service      │
│   :8080        │       │    :8081             │
│                │       │                      │
│ Authorization  │◄──────│ Resource Server      │
│ Server (OIDC)  │ JWKS  │ User profiles        │
└───────┬────────┘       └──────────┬───────────┘
        │                           │
        │ InMemory (MVP)            │ PostgreSQL
        │                           │ heavyrent_users
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| [auth-service](./auth-service/README.md) | 8080 | OAuth2 Authorization Server, issues JWT tokens |
| [user-service](./user-service/README.md) | 8081 | User profiles, Resource Server |

## Tech Stack

- **Java 21**
- **Spring Boot 4.0.2**
- **Spring Security 7.0.2**
- **Spring Authorization Server 7.0.2**
- **Spring Data JPA + Hibernate 7**
- **PostgreSQL 16**
- **Docker**
- **Gradle 9**
- **Lombok**

## Prerequisites

- Java 21+
- Docker
- Gradle 9+

## Quick Start

### 1. Clone the repository

```bash
git clone https://github.com/your-org/heavyrent.git
cd heavyrent
```

### 2. Start PostgreSQL

```bash
docker run --name heavyrent-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=heavyrent_users \
  -p 5432:5432 \
  -d postgres:16
```

### 3. Build all services

```bash
./gradlew build
```

### 4. Start auth-service

```bash
./gradlew :auth-service:bootRun
```

### 5. Start user-service (in a separate terminal)

```bash
./gradlew :user-service:bootRun
```

## Authentication Flow

See [auth-service README](./auth-service/README.md) for full OAuth2 flow documentation.

## Project Structure

```
HeavyRent/
├── auth-service/       # OAuth2 Authorization Server
├── user-service/       # User profile Resource Server
├── common/             # Shared utilities and models
├── build.gradle        # Root build config
└── settings.gradle     # Module declarations
```

## Development Status

| Feature | Status |
|---------|--------|
| OAuth2 Authorization Server | ✅ Done |
| User profiles API | ✅ Done |
| API Gateway | 🔜 Planned |
| Equipment service | 🔜 Planned |
| Rental service | 🔜 Planned |
| Production DB for auth | 🔜 Planned |