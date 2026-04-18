# Tripocket API

## Uruchomienie projektu

1. Skopiuj plik `.env.example` jako `.env`:
   ```bash
   cp .env.example .env
   ```

2. Uruchom infrastrukturę (Postgres, Redis, Keycloak, Nginx):
   ```bash
   docker compose up -d
   ```

3. Uruchom aplikację:
   ```bash
   ./mvnw spring-boot:run
   ```

API będzie dostępne pod `http://localhost:8080/api`, Keycloak pod `http://localhost/auth`.
