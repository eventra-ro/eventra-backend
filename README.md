# Eventra Backend

REST API pentru Eventra — hub de descoperire furnizori si planning evenimente din Romania.

## Stack
- Java 21 + Spring Boot 3.4.x
- PostgreSQL 15 (Supabase)
- Flyway, Spring Security, JWT, MapStruct, Lombok

## Rulare locala

```bash
git clone https://github.com/eventra-ro/eventra-backend.git
cd eventra-backend
cp src/main/resources/application-example.yml src/main/resources/application-local.yml
# Completeaza valorile in application-local.yml
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

API docs: http://localhost:8080/swagger-ui.html

## Branch strategy
- `main` Production
- `develop` Staging + default
- `feature/*` Features noi
- `fix/*` Bug fixes
