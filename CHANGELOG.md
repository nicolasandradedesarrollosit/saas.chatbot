# Changelog

## [0.2.0] - 2026-02-18

### ✨ Added
- **JWT Authentication Filter** — `JwtAuthenticationFilter` validates tokens on every request and sets `SecurityContext`
- **Auto-refresh mechanism** — When access token expires, the filter automatically refreshes using the refresh token cookie, returning new tokens via `Set-Cookie` and `X-New-Access-Token` header
- **Refresh tokens** — Full refresh token lifecycle with persistence (`RefreshTokenEntity`, `RefreshTokenJpaRepository`, `RefreshTokenRepositoryAdapter`)
- **Token blacklist** — In-memory blacklist (`InMemoryTokenBlacklist`) with scheduled cleanup for invalidated access tokens
- **Cookie-based refresh tokens** — `CookieUtil` manages HttpOnly, Secure, SameSite=Strict cookies for refresh tokens
- **Domain ports** — `TokenProviderPort`, `PasswordEncoderPort`, `RefreshTokenRepositoryPort`, `TokenBlacklistPort`
- **Domain models** — `AuthToken` (access + refresh pair), `RefreshToken` (with expiry and revocation)
- **BeanConfig** — Wires domain services with infrastructure adapters via `@Bean`
- **Scheduled tasks** — `@EnableScheduling` for periodic token cleanup

### 🔄 Changed
- **AuthUseCase** — `login()` now returns `AuthToken` (access + refresh), `logout()` accepts access + refresh tokens, added `refresh()` method
- **AuthService** — Refactored to depend on domain ports instead of Spring classes directly, removed `@Service` annotation (wired via `BeanConfig`)
- **JwtService** — Now implements `TokenProviderPort`, added `generateAccessToken()`, `generateRefreshToken()`, `isTokenExpired()`
- **AuthController** — Login returns access token in body + refresh token in cookie, logout reads tokens from header/cookie and clears cookie
- **AuthRequest** — Removed `RequestLogout` record (logout no longer needs a request body)
- **SecurityConfig** — Injects `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`

### 🏗️ Architecture
- **SpringPasswordEncoderAdapter** — Bridges Spring's `PasswordEncoder` with domain `PasswordEncoderPort`
- Follows hexagonal architecture: domain layer has zero framework dependencies

## [0.1.0] - 2026-02-17

### ✨ Added
- Initial project setup with hexagonal architecture
- Basic auth module: login, register
- Spring Security + JWT integration
- Spring Data JPA + PostgreSQL (Supabase)
