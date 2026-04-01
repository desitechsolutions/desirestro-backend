# 🍽️ DesiRestro Backend

Production-grade, multi-tenant restaurant management REST API built with **Spring Boot 4.0**, **Java 21**, and **MySQL**.  
Multiple restaurant owners can register on the platform and each has fully isolated data (tables, menu, KOTs, billing, staff, inventory).

---

## 📑 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Multi-Tenancy Design](#multi-tenancy-design)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Local Development](#local-development)
  - [Docker Compose](#docker-compose)
- [Configuration Profiles](#configuration-profiles)
- [Database Migrations](#database-migrations)
- [Security](#security)
- [API Documentation](#api-documentation)
- [Health & Monitoring](#health--monitoring)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     HTTP Clients                            │
│          (POS Frontend / Mobile App / Admin Panel)          │
└───────────────────────────┬─────────────────────────────────┘
                            │  HTTPS
                ┌───────────▼──────────┐
                │  Spring Boot API      │
                │  (DesiRestro Backend) │
                │                       │
                │  JwtAuthenticationFilter ──► TenantContext (ThreadLocal)
                │          │                         │
                │  RestaurantFilterAspect ◄───────────┘
                │  (AOP — enables Hibernate tenant filter)
                │          │
                │  Domain Services & Controllers
                └───────────┬──────────┘
                            │
                ┌───────────▼──────────┐
                │        MySQL          │
                │  (row-level tenancy)  │
                └───────────────────────┘
```

---

## Multi-Tenancy Design

DesiRestro uses **row-level tenancy** — all domain tables carry a `restaurant_id` FK that points to the `restaurant` table (the tenant root).

### How it works end-to-end

| Step | Component | Action |
|------|-----------|--------|
| 1 | `POST /api/auth/register` (with `restaurantName`) | Creates `Restaurant` + `OWNER` user atomically |
| 2 | `POST /api/auth/login` | Returns JWT with `restaurantId` claim embedded |
| 3 | `JwtAuthenticationFilter` | Extracts `restaurantId` from JWT → sets `TenantContext` (ThreadLocal) |
| 4 | `RestaurantFilterAspect` (AOP) | Wraps every Spring Data repository call → enables Hibernate `restaurantFilter` with `restaurantId` |
| 5 | Hibernate filter | Appends `WHERE restaurant_id = ?` to every query automatically |
| 6 | `RestaurantEntityListener` (JPA lifecycle) | Auto-populates `restaurant` FK on every entity before persist/update |
| 7 | `finally` block in filter | Always clears `TenantContext` to prevent thread-local leaks |

### Special cases

| Repository | Behaviour |
|-----------|-----------|
| `UserRepository` | `@SkipRestaurantFilter` — authentication must find users across all tenants |
| `RefreshTokenRepository` | `@SkipRestaurantFilter` — tokens are not tenant-scoped |
| `MenuItemIngredientRepository` | `@SkipRestaurantFilter` — junction table; isolation via parent MenuItem |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 4.0.1 |
| Language | Java 21 |
| Database | MySQL 8.4 (prod), H2 (tests) |
| Migrations | Flyway |
| Auth | JWT (jjwt 0.12.6) + Refresh Tokens |
| ORM | Spring Data JPA + Hibernate 6 |
| Multi-tenancy | Hibernate Filters + AOP |
| Documentation | SpringDoc OpenAPI 2.8.9 (Swagger UI) |
| Monitoring | Spring Boot Actuator |
| Containerization | Docker + Docker Compose |
| Build | Maven 3.9 |

---

## Project Structure

```
src/main/java/com/dts/restro/
├── common/
│   ├── ApiResponse.java              # Unified response envelope
│   ├── TenantContext.java            # ThreadLocal for current restaurant ID
│   ├── annotation/
│   │   └── SkipRestaurantFilter.java # Bypass tenant filter for specific repos
│   ├── aspect/
│   │   └── RestaurantFilterAspect.java # AOP — enables Hibernate tenant filter
│   ├── entity/
│   │   ├── BaseEntity.java           # ID + audit timestamps
│   │   ├── RestaurantAwareEntity.java # + restaurant FK + Hibernate filter
│   │   └── package-info.java         # @FilterDef declaration
│   ├── listener/
│   │   └── RestaurantEntityListener.java # JPA lifecycle — auto-set restaurant
│   └── util/
│       └── SpringContext.java        # Static Spring bean access (for JPA listeners)
├── config/
│   ├── OpenApiConfig.java            # Swagger / OpenAPI setup
│   └── SecurityConfig.java           # JWT security + CORS
├── controller/
│   ├── AuthController.java           # /api/auth/**
│   ├── RestaurantController.java     # /api/restaurant (owner profile)
│   ├── MenuController.java           # /api/menu/**
│   └── ...
├── dto/
│   ├── AuthRequest.java
│   ├── AuthResponse.java             # + restaurantId, restaurantName
│   ├── RegisterRequest.java          # + restaurantName fields
│   ├── ChangePasswordRequest.java
│   └── restaurant/
│       ├── RestaurantRegistrationRequest.java
│       ├── RestaurantResponse.java
│       └── RestaurantUpdateRequest.java
├── entity/
│   ├── Restaurant.java               # Tenant root
│   ├── User.java                     # + restaurant FK + isActive
│   ├── Role.java                     # SUPER_ADMIN, OWNER, ADMIN, CAPTAIN, KITCHEN, CASHIER, STAFF
│   └── ... (all extend RestaurantAwareEntity)
├── exception/
│   ├── GlobalExceptionHandler.java   # Centralised error handling
│   ├── ResourceNotFoundException.java
│   ├── BusinessValidationException.java
│   ├── DuplicateResourceException.java
│   └── InsufficientStockException.java
├── repository/
│   └── ... (tenant repos get filter; @SkipRestaurantFilter bypasses it)
├── security/
│   ├── JwtUtil.java                  # + restaurantId claim generation/extraction
│   ├── JwtAuthenticationFilter.java  # + TenantContext population
│   └── CustomUserDetailsService.java
└── service/
    ├── AuthService.java              # register (with restaurant), authenticate, changePassword
    ├── RefreshTokenService.java      # createRefreshToken, rotateRefreshToken, deleteBy*
    ├── RestaurantService.java        # getCurrentRestaurant, updateCurrentRestaurant
    └── ...
```

---

## API Endpoints

### Authentication (`/api/auth`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/auth/register` | Public | Register restaurant owner + create restaurant tenant |
| `POST` | `/api/auth/login` | Public | Login — returns access token + sets refresh cookie |
| `POST` | `/api/auth/refresh` | Cookie | Rotate refresh token → new access token |
| `POST` | `/api/auth/logout` | Cookie | Revoke refresh token + clear cookie |
| `POST` | `/api/auth/change-password` | JWT | Change password (invalidates all refresh tokens) |

### Restaurant Profile (`/api/restaurant`)

| Method | Path | Role | Description |
|--------|------|------|-------------|
| `GET` | `/api/restaurant` | OWNER/ADMIN | Get own restaurant profile |
| `PUT` | `/api/restaurant` | OWNER/ADMIN | Update restaurant profile |

### Menu (`/api/menu`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/menu` | Public | All categories with available items |
| `GET` | `/api/menu/available-items` | Public | Flat list of available items (with search) |

> All other endpoints (tables, KOT, bills, inventory, staff) require a valid JWT and automatically scope to the authenticated user's restaurant.

---

## Getting Started

### Prerequisites

- Java 21
- Maven 3.9+
- MySQL 8.4 (or Docker)

### Local Development

```bash
# 1. Clone the repo
git clone https://github.com/desitechsolutions/desirestro-backend.git
cd desirestro-backend

# 2. Create a local .env (never commit this)
cp .env.example .env
# Edit .env with your MySQL credentials

# 3. Create the MySQL database
mysql -u root -p -e "CREATE DATABASE desi_restro_db CHARACTER SET utf8mb4;"
mysql -u root -p -e "CREATE USER 'desi_restro'@'localhost' IDENTIFIED BY 'DesiRestro@1234';"
mysql -u root -p -e "GRANT ALL PRIVILEGES ON desi_restro_db.* TO 'desi_restro'@'localhost';"

# 4. Run the application (dev profile active by default)
./mvnw spring-boot:run

# The API will be available at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui.html
```

### Docker Compose

```bash
# Start everything (MySQL + backend)
docker compose up --build

# Rebuild only the backend
docker compose up --build desirestro-backend

# Stop
docker compose down

# Stop and remove volumes
docker compose down -v
```

---

## Configuration Profiles

| Profile | Activation | Database | Logging | Swagger |
|---------|-----------|----------|---------|---------|
| `dev` | Default | MySQL localhost | DEBUG | Enabled |
| `prod` | `SPRING_PROFILES_ACTIVE=prod` | Via env vars | INFO/WARN | Disabled |
| `test` | Test suite | H2 in-memory | INFO | Enabled |

### Key Environment Variables (Production)

| Variable | Description |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
| `JWT_SECRET` | Base64 encoded HMAC secret (min 256 bits) |
| `APP_ALLOWED_ORIGINS` | Comma-separated CORS origins |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `PORT` | Server port (default 8080) |

Generate a secure JWT secret:
```bash
openssl rand -base64 64
```

---

## Database Migrations

Flyway manages all schema changes in `src/main/resources/db/migration/`.

| Version | Description |
|---------|-------------|
| V1 | Initial schema (users, staff, menu, tables, parties, KOT, bills, inventory) |
| V2 | Align staff with users table |
| V3 | Seed initial data |
| V4 | Multi-tenancy — add `restaurant` table + `restaurant_id` FK to all domain tables |

---

## Security

- **JWT access tokens** — 24-hour expiry, contain `role` and `restaurantId` claims
- **Refresh tokens** — 7-day expiry, stored in DB, rotated on every refresh, cleared on logout/password-change
- **HTTP-only cookies** — refresh token is delivered as a `Secure; HttpOnly; SameSite=Strict` cookie
- **Row-level tenant isolation** — Hibernate filter ensures every query is scoped to the authenticated restaurant
- **Password hashing** — BCrypt
- **Public endpoints** — only auth, Swagger UI, actuator health, and GET menu are public
- **Role-based access** — `@PreAuthorize` used for owner-only operations

---

## API Documentation

Swagger UI is available in `dev` profile at:  
**http://localhost:8080/swagger-ui.html**

OpenAPI JSON spec:  
**http://localhost:8080/v3/api-docs**

---

## Health & Monitoring

Spring Boot Actuator endpoints:

| Endpoint | URL | Auth required |
|---------|-----|--------------|
| Health | `/actuator/health` | No |
| Info | `/actuator/info` | No |
| Metrics | `/actuator/metrics` | Yes |
