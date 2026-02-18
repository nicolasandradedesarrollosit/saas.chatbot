# SaaS Chatbot Platform

Backend de una plataforma SaaS de chatbots construido con Spring Boot y arquitectura hexagonal.

## Tech Stack

| Tecnologia | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.10 |
| Spring Security | 6.x |
| Spring Data JPA | 6.x |
| PostgreSQL (Supabase) | 17.6 |
| JJWT | 0.12.6 |
| Lombok | - |

## Arquitectura

El proyecto sigue una **Arquitectura Hexagonal (Ports & Adapters)** con separacion clara de capas:

```
src/main/java/com/example/saas/chatbot/
├── domain/                    # Capa de dominio (sin dependencias externas)
│   ├── model/
│   │   ├── User.java          # Modelo de dominio
│   │   └── Role.java          # Enum: USER, ADMIN
│   └── port/
│       ├── in/
│       │   └── AuthUseCase.java       # Puerto de entrada
│       └── out/
│           └── UserRepositoryPort.java # Puerto de salida
│
├── application/               # Capa de aplicacion (casos de uso)
│   └── service/
│       └── AuthService.java   # Logica de negocio
│
└── infrastructure/            # Capa de infraestructura
    ├── adapter/
    │   ├── in/
    │   │   ├── AuthController.java    # REST Controller
    │   │   └── AuthRequest.java       # DTO
    │   └── out/
    │       ├── UserEntity.java        # Entidad JPA
    │       ├── UserJpaRepository.java # Spring Data Repository
    │       └── UserRepositoryAdapter.java # Adaptador del puerto
    ├── config/
    │   └── SecurityConfig.java        # Configuracion de seguridad
    └── security/
        └── JwtService.java            # Generacion y validacion JWT
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
    │ Input Port │         │  Output Port   │
    │(AuthUseCase)         │(UserRepository)│
    └─────┬──────┘         └───────┬────────┘
          │                        │
          │  ┌──────────────────┐  │
          └─>│ Application Layer│<─┘
             │  (AuthService)   │
             └────────┬─────────┘
                      │
             ┌────────▼─────────┐
             │   Domain Layer   │
             │   User / Role    │
             └──────────────────┘
```

## API Endpoints

### Autenticacion

| Metodo | Endpoint | Body | Respuesta |
|---|---|---|---|
| POST | `/api/auth/register` | `{ "email": "", "password": "" }` | `"Usuario registrado: {email}"` |
| POST | `/api/auth/login` | `{ "email": "", "password": "" }` | JWT token |

- `/api/auth/**` es publico
- El resto de endpoints requiere autenticacion via JWT

### Ejemplos (Postman)

**POST** `http://localhost:8080/api/auth/register`
> Body > raw > JSON

```json
{
  "email": "test@test.com",
  "password": "123456"
}
```

**POST** `http://localhost:8080/api/auth/login`
> Body > raw > JSON

```json
{
  "email": "test@test.com",
  "password": "123456"
}
```

**Endpoints protegidos:**
> Authorization > Bearer Token > pegar el JWT devuelto por `/login`

## Seguridad

- **Passwords**: encriptadas con BCrypt
- **Autenticacion**: JWT (HS256, expiracion 24hs)
- **Sesiones**: Stateless
- **Roles**: USER, ADMIN

## Base de Datos

PostgreSQL hosteado en **Supabase**. Hibernate maneja el schema automaticamente (`ddl-auto=update`).

**Tabla `users`:**

| Columna | Tipo | Restricciones |
|---|---|---|
| id | BIGINT | PK, auto-increment |
| email | VARCHAR | UNIQUE, NOT NULL |
| password | VARCHAR | NOT NULL |
| role | VARCHAR | ENUM (USER, ADMIN) |

## Setup

### Prerequisitos

- Java 21
- Maven (o usar el wrapper `./mvnw`)
- PostgreSQL (o cuenta en Supabase)

### Variables de entorno

Crear un archivo `.env` en la raiz del proyecto:

```env
DB_URL="jdbc:postgresql://<host>:<port>/<database>?sslmode=require"
DB_USERNAME=<usuario>
DB_PASSWORD=<password>
JWT_SECRET=<secreto-de-al-menos-32-caracteres>
JWT_EXPIRATION=86400000
```

### Ejecutar

```bash
# Cargar variables de entorno y correr
set -a && source .env && set +a && ./mvnw spring-boot:run
```

La app arranca en `http://localhost:8080`.

### Build

```bash
./mvnw clean package
```

## Licencia

Este proyecto es privado.
