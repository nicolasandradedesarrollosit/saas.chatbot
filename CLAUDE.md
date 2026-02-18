# CLAUDE.md — Instrucciones de Arquitectura del Proyecto

## Arquitectura

**Hexagonal (Ports & Adapters)** con módulos de negocio dentro de cada capa.

La estructura se organiza **primero por capa hexagonal, luego por módulo de negocio**.

```
src/main/java/com/project/
├── domain/
│   ├── auth/
│   ├── product/
│   └── shared/
├── application/
│   ├── auth/
│   ├── product/
│   └── shared/
├── infrastructure/
│   ├── auth/
│   ├── product/
│   └── config/
└── ProjectApplication.java
```

---

## Regla de dependencias

```
application ──► domain ◄── infrastructure
```

- `domain/` NO importa nada de `application/` ni de `infrastructure/`.
- `domain/` NO importa Spring, JPA, Lombok, ni ningún framework.
- `application/` conoce al dominio para orquestar y exponer endpoints.
- `infrastructure/` conoce al dominio para implementar los puertos de salida.
- `application/` e `infrastructure/` NUNCA se importan entre sí directamente.

---

## Estructura detallada por capa

### `domain/` — Corazón de negocio (sin frameworks)

```
domain/
├── auth/
│   ├── model/
│   │   ├── User.java                        # Entidad de dominio
│   │   ├── Role.java                         # Enum o Value Object
│   │   └── AuthToken.java                    # Value Object
│   ├── port/
│   │   ├── in/                               # Puertos de entrada (casos de uso)
│   │   │   ├── RegisterUserUseCase.java
│   │   │   ├── LoginUseCase.java
│   │   │   └── RefreshTokenUseCase.java
│   │   └── out/                              # Puertos de salida (contratos)
│   │       ├── UserRepositoryPort.java
│   │       ├── PasswordEncoderPort.java
│   │       └── TokenProviderPort.java
│   ├── service/                              # Implementación de los use cases
│   │   └── AuthService.java
│   └── exception/
│       ├── UserAlreadyExistsException.java
│       └── InvalidCredentialsException.java
│
├── product/
│   ├── model/
│   │   └── Product.java
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateProductUseCase.java
│   │   │   └── GetProductUseCase.java
│   │   └── out/
│   │       └── ProductRepositoryPort.java
│   ├── service/
│   │   └── ProductService.java
│   └── exception/
│       └── ProductNotFoundException.java
│
└── shared/
    ├── model/
    │   └── BaseEntity.java                   # Si aplica (id, timestamps)
    └── exception/
        └── DomainException.java              # Excepción base de dominio
```

**Reglas de `domain/`:**
- Clases Java puras. Cero anotaciones de frameworks.
- Los modelos de dominio se crean con constructores, factory methods o builders manuales.
- Los puertos `in/` son interfaces que definen QUÉ puede hacer el sistema.
- Los puertos `out/` son interfaces que definen QUÉ necesita el sistema del exterior.
- Los `service/` implementan los puertos `in/` e inyectan puertos `out/` por constructor.
- Las excepciones son de dominio, no HTTP (sin status codes aquí).

---

### `application/` — Adaptadores de entrada (API REST, validación, DTOs)

```
application/
├── auth/
│   ├── controller/
│   │   └── AuthController.java               # @RestController
│   ├── dto/
│   │   ├── request/
│   │   │   ├── LoginRequest.java              # @Valid, Bean Validation
│   │   │   └── RegisterRequest.java
│   │   └── response/
│   │       └── AuthResponse.java
│   └── mapper/
│       └── AuthApplicationMapper.java         # DTO <-> Modelo de dominio
│
├── product/
│   ├── controller/
│   │   └── ProductController.java
│   ├── dto/
│   │   ├── request/
│   │   │   └── CreateProductRequest.java
│   │   └── response/
│   │       └── ProductResponse.java
│   └── mapper/
│       └── ProductApplicationMapper.java
│
└── shared/
    ├── dto/
    │   └── ApiErrorResponse.java
    └── exception/
        └── GlobalExceptionHandler.java        # @RestControllerAdvice
```

**Reglas de `application/`:**
- Aquí SÍ se usan anotaciones de Spring: `@RestController`, `@RequestMapping`, `@Valid`.
- Los controllers reciben DTOs, los mapean al dominio, invocan el use case, y devuelven DTOs.
- Los controllers NUNCA contienen lógica de negocio, solo orquestación.
- Los DTOs usan Bean Validation (`@NotBlank`, `@Email`, `@Size`).
- El `GlobalExceptionHandler` traduce excepciones de dominio a respuestas HTTP.
- Los mappers de esta capa convierten entre DTOs de request/response y modelos de dominio.

---

### `infrastructure/` — Adaptadores de salida (persistencia, seguridad, servicios externos)

```
infrastructure/
├── auth/
│   ├── persistence/
│   │   ├── entity/
│   │   │   └── UserEntity.java                # @Entity JPA
│   │   ├── repository/
│   │   │   └── JpaUserRepository.java         # extends JpaRepository
│   │   ├── mapper/
│   │   │   └── UserPersistenceMapper.java     # Entity <-> Modelo de dominio
│   │   └── adapter/
│   │       └── UserRepositoryAdapter.java     # Implementa UserRepositoryPort
│   └── security/
│       ├── JwtTokenProvider.java              # Implementa TokenProviderPort
│       ├── SpringPasswordEncoder.java         # Implementa PasswordEncoderPort
│       └── JwtAuthenticationFilter.java       # Filtro de Spring Security
│
├── product/
│   ├── persistence/
│   │   ├── entity/
│   │   │   └── ProductEntity.java
│   │   ├── repository/
│   │   │   └── JpaProductRepository.java
│   │   ├── mapper/
│   │   │   └── ProductPersistenceMapper.java
│   │   └── adapter/
│   │       └── ProductRepositoryAdapter.java
│
└── config/
    ├── SecurityConfig.java                    # Configuración Spring Security
    ├── JwtConfig.java                         # Properties de JWT
    ├── CorsConfig.java                        # Configuración CORS
    └── BeanConfig.java                        # @Bean para conectar puertos con adapters
```

**Reglas de `infrastructure/`:**
- Aquí viven TODAS las dependencias de frameworks: JPA, Spring Security, clientes HTTP, etc.
- Las `entity/` son clases JPA (`@Entity`, `@Table`, `@Column`) separadas del modelo de dominio.
- Los `adapter/` implementan los puertos `out/` del dominio e inyectan los repos JPA.
- Los `mapper/` de esta capa convierten entre entidades JPA y modelos de dominio.
- `config/` contiene la configuración transversal que no pertenece a un módulo específico.
- `BeanConfig` registra los `@Bean` que conectan las interfaces del dominio con sus implementaciones.

---

## Flujo de una request (ejemplo: Login)

```
1. HTTP POST /api/auth/login
2. AuthController recibe LoginRequest (DTO)
3. AuthApplicationMapper convierte DTO → modelo de dominio
4. Controller invoca LoginUseCase.login(email, password)
5. AuthService (implementa LoginUseCase):
   a. Llama UserRepositoryPort.findByEmail(email)
   b. Llama PasswordEncoderPort.matches(raw, encoded)
   c. Llama TokenProviderPort.generateToken(user)
   d. Retorna AuthToken (value object de dominio)
6. Controller mapea AuthToken → AuthResponse (DTO)
7. HTTP 200 con AuthResponse
```

---

## Convenciones de código

### Nombrado
- Interfaces de puertos de entrada: `[Accion][Entidad]UseCase` → `RegisterUserUseCase`
- Interfaces de puertos de salida: `[Entidad][Recurso]Port` → `UserRepositoryPort`
- Implementaciones de infra: `[Entidad][Tecnología]Adapter` → `UserRepositoryAdapter`
- Servicios de dominio: `[Módulo]Service` → `AuthService`
- Entidades JPA: `[Entidad]Entity` → `UserEntity`
- DTOs: `[Acción]Request` / `[Entidad]Response` → `LoginRequest`, `AuthResponse`

### Inyección de dependencias
- Siempre por constructor, nunca `@Autowired` en campos.
- Los servicios de dominio reciben puertos `out/` por constructor (Java puro, sin `@Autowired`).
- La conexión se hace en `BeanConfig` con métodos `@Bean`.

### Ejemplo de BeanConfig

```java
@Configuration
public class BeanConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder) {
        return new AuthService(userRepository, passwordEncoder);
    }

    @Bean
    public LoginUseCase loginUseCase(
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder,
            TokenProviderPort tokenProvider) {
        return new AuthService(userRepository, passwordEncoder, tokenProvider);
    }
}
```

### Validaciones
- Validación de formato/estructura → DTOs con Bean Validation (`application/`)
- Validación de reglas de negocio → Servicios de dominio (`domain/`)
- Constraints de base de datos → Entidades JPA (`infrastructure/`)

### Manejo de errores
- El dominio lanza excepciones propias que extienden `DomainException`.
- `GlobalExceptionHandler` en `application/shared/` las captura y devuelve `ApiErrorResponse`.
- Nunca lanzar `ResponseStatusException` ni devolver `ResponseEntity` desde el dominio.

---

## Módulo nuevo: checklist

Al crear un nuevo módulo (ej: `order`):

1. `domain/order/model/` → Crear entidades y value objects de dominio
2. `domain/order/port/in/` → Definir interfaces de casos de uso
3. `domain/order/port/out/` → Definir interfaces de puertos de salida
4. `domain/order/service/` → Implementar casos de uso
5. `domain/order/exception/` → Excepciones de dominio del módulo
6. `infrastructure/order/persistence/entity/` → Entidad JPA
7. `infrastructure/order/persistence/repository/` → JpaRepository
8. `infrastructure/order/persistence/mapper/` → Mapper entity ↔ domain
9. `infrastructure/order/persistence/adapter/` → Implementar puertos out
10. `application/order/dto/` → Request y Response DTOs
11. `application/order/mapper/` → Mapper DTO ↔ domain
12. `application/order/controller/` → REST controller
13. `infrastructure/config/BeanConfig.java` → Registrar beans del módulo

---

## Stack tecnológico

- Java 21
- Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA + PostgreSQL (Supabase)
- Maven
- Bean Validation (Jakarta)