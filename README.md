# SaaS Chatbot Platform

Backend for a SaaS chatbot platform built with Spring Boot and hexagonal architecture.

## Tech Stack

| Technology | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.10 |
| Spring Security | 6.x |
| Spring Data JPA | 6.x |
| PostgreSQL (Supabase) | 17.6 |
| JJWT | 0.12.6 |
| Lombok | - |

## Architecture

The project follows a **Hexagonal Architecture (Ports & Adapters)** with clear layer separation:

```
src/main/java/com/example/saas/chatbot/
├── domain/                              # Domain layer (no framework dependencies)
│   └── auth/
│       ├── model/
│       │   ├── User.java                # Domain entity
│       │   ├── Role.java                # Enum: USER, ADMIN
│       │   ├── AuthToken.java           # Value object (access + refresh token pair)
│       │   └── RefreshToken.java        # Refresh token with expiry and revocation
│       ├── port/
│       │   ├── in/
│       │   │   └── AuthUseCase.java     # Input port (login, register, logout, refresh)
│       │   └── out/
│       │       ├── UserRepositoryPort.java
│       │       ├── TokenProviderPort.java
│       │       ├── PasswordEncoderPort.java
│       │       ├── RefreshTokenRepositoryPort.java
│       │       └── TokenBlacklistPort.java
│       └── exception/
│           ├── InvalidCredentialsException.java
│           ├── UserAlreadyExistsException.java
│           └── InvalidTokenException.java
│
├── application/                         # Application layer (use cases, DTOs, error handling)
│   ├── service/auth/
│   │   └── AuthService.java            # Business logic (implements AuthUseCase)
│   └── shared/
│       ├── dto/
│       │   └── ApiResponse.java         # Standardized API response (RFC 7807)
│       └── exception/
│           └── GlobalExceptionHandler.java
│
└── infrastructure/                      # Infrastructure layer (frameworks, adapters)
    └── auth/
        ├── adapter/
        │   ├── in/
        │   │   ├── AuthController.java  # REST controller
        │   │   └── AuthRequest.java     # Request DTOs with Bean Validation
        │   └── out/
        │       ├── UserEntity.java
        │       ├── UserJpaRepository.java
        │       ├── UserRepositoryAdapter.java
        │       ├── RefreshTokenEntity.java
        │       ├── RefreshTokenJpaRepository.java
        │       └── RefreshTokenRepositoryAdapter.java
        ├── config/
        │   ├── SecurityConfig.java      # Spring Security filter chain
        │   └── BeanConfig.java          # Wires domain ports to infrastructure adapters
        └── security/
            ├── JwtService.java          # JWT generation/validation (implements TokenProviderPort)
            ├── JwtAuthenticationFilter.java  # Validates JWT on every request, auto-refreshes expired tokens
            ├── SpringPasswordEncoderAdapter.java
            ├── InMemoryTokenBlacklist.java
            └── CookieUtil.java          # HttpOnly, Secure, SameSite cookie management
```

```
    ┌──────────────────────────────────────────┐
    │          Infrastructure Layer            │
    │  Controllers  Adapters  Config  Security │
    └──────────────────┬───────────────────────┘
                       │
          ┌────────────┴────────────┐
          │                         │
    ┌─────▼──────┐         ┌───────▼────────┐
    │ Input Port │         │  Output Ports  │
    │(AuthUseCase)         │(Repository,    │
    │            │         │ TokenProvider, │
    │            │         │ Blacklist...)  │
    └─────┬──────┘         └───────┬────────┘
          │                        │
          │  ┌──────────────────┐  │
          └─>│ Application Layer│<─┘
             │  (AuthService)   │
             └────────┬─────────┘
                      │
             ┌────────▼─────────┐
             │   Domain Layer   │
             │ User / AuthToken │
             │ RefreshToken     │
             └──────────────────┘
```

## Authentication Flow

### Login
`POST /api/auth/login` → Sets `access_token` (15 min) and `refresh_token` (7 days) as HttpOnly cookies.

### Auto-refresh (middleware)
The `JwtAuthenticationFilter` runs on every request:
1. Extracts access token from `Authorization` header or `access_token` cookie
2. If valid and not blacklisted → sets SecurityContext, continues
3. If expired → reads `refresh_token` cookie → generates new token pair → sets new cookies + `X-New-Access-Token` header
4. If no token or refresh invalid → returns 401

### Logout
`POST /api/auth/logout` (requires authentication) → Blacklists access token, revokes all refresh tokens in DB, clears both cookies.

## API Endpoints

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | No | Register a new user |
| POST | `/api/auth/login` | No | Login and receive tokens in cookies |
| POST | `/api/auth/logout` | Yes | Invalidate tokens and clear cookies |

### Standardized Response Format

All API responses follow RFC 7807 Problem Details:

**Success:**
```json
{
  "title": "Login Successful",
  "status": 200,
  "detail": "Authentication completed"
}
```

**Validation Error (400):**
```json
{
  "type": "validation",
  "title": "Validation Failed",
  "status": 400,
  "detail": "One or more fields are invalid",
  "fieldErrors": [
    { "field": "email", "message": "Must be a valid email" },
    { "field": "password", "message": "Must be at least 8 characters" }
  ]
}
```

**Authentication Error (401):**
```json
{
  "type": "auth/invalid-credentials",
  "title": "Authentication Failed",
  "status": 401,
  "detail": "Invalid email or password"
}
```

**Conflict Error (409):**
```json
{
  "type": "auth/user-already-exists",
  "title": "Registration Failed",
  "status": 409,
  "detail": "Email already registered: user@example.com"
}
```

### Request Validation

| Field | Rules |
|---|---|
| `email` | Required, must be a valid email |
| `password` | Required, minimum 8 characters |

## Security

- **Passwords**: Encrypted with BCrypt
- **Authentication**: JWT (HS256) via HttpOnly cookies
- **Access token**: 15 minutes expiration
- **Refresh token**: 7 days expiration, stored in DB, supports revocation
- **Token blacklist**: In-memory with scheduled hourly cleanup
- **Sessions**: Stateless
- **Cookies**: HttpOnly, Secure, SameSite=Strict
- **Roles**: USER, ADMIN

## Database

PostgreSQL hosted on **Supabase**. Hibernate manages the schema automatically (`ddl-auto=update`).

**Table `users`:**

| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, auto-increment |
| email | VARCHAR | UNIQUE, NOT NULL |
| password | VARCHAR | NOT NULL |
| role | VARCHAR | ENUM (USER, ADMIN) |

**Table `refresh_tokens`:**

| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, auto-increment |
| token | VARCHAR(512) | UNIQUE, NOT NULL |
| user_email | VARCHAR | NOT NULL |
| expires_at | TIMESTAMP | NOT NULL |
| revoked | BOOLEAN | NOT NULL |

## API Testing

HTTPYac test files are located in `http/auth.http`. They cover:

- Registration: success, validation errors, duplicate email
- Login: success, wrong credentials, validation errors
- Logout: with and without authentication
- Protected endpoints: without token, invalid token, blacklisted token

## Setup

### Prerequisites

- Java 21
- Maven (or use the wrapper `./mvnw`)
- PostgreSQL (or a Supabase account)

### Environment Variables

Create a `.env` file in the project root:

```env
DB_URL="jdbc:postgresql://<host>:<port>/<database>?sslmode=require"
DB_USERNAME=<username>
DB_PASSWORD=<password>
JWT_SECRET=<secret-at-least-32-characters>
JWT_EXPIRATION=900000
```

### Run

```bash
set -a && source .env && set +a && ./mvnw spring-boot:run
```

The app starts at `http://localhost:8080`.

### Build

```bash
./mvnw clean package
```

## License

This project is private.
