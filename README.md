# Workout Tracker — Backend

App personal para registrar entrenamientos y calcular récords con contexto de fatiga.

## Stack

- Spring Boot 3 + Java 21
- MongoDB (Spring Data Mongo)
- Seguridad con JWT
- Docker para local, desplegable en Railway/Render

## Arranque local

```bash
# 1. Levanta MongoDB
docker compose up -d

# 2. Arranca la app
./mvnw spring-boot:run
# o
mvn spring-boot:run
```

La API queda en `http://localhost:8080`.

## Variables de entorno (producción)

| Variable | Descripción |
|---|---|
| `SPRING_DATA_MONGODB_URI` | Connection string de MongoDB Atlas |
| `APP_JWT_SECRET` | Secreto JWT (mínimo 32 caracteres) |
| `PORT` | Puerto (Railway lo inyecta automáticamente) |

## Endpoints principales

### Auth (público)
- `POST /api/auth/register` — `{ username, password }`
- `POST /api/auth/login` — `{ username, password }` → devuelve `{ token, userId, username }`

El resto de endpoints requieren cabecera `Authorization: Bearer <token>`.

### Ejercicios
- `GET /api/exercises`
- `POST /api/exercises`
- `PUT /api/exercises/{id}`
- `DELETE /api/exercises/{id}` (soft delete)

### Días de entrenamiento (plantilla)
- `GET /api/training-days`
- `POST /api/training-days`
- `PUT /api/training-days/{id}`
- `DELETE /api/training-days/{id}`

### Sesiones
- `GET /api/sessions`
- `GET /api/sessions/{id}`
- `POST /api/sessions` → devuelve `{ session, newRecords }`
- `PUT /api/sessions/{id}`
- `DELETE /api/sessions/{id}` (recalcula récords afectados)

### Récords
- `GET /api/records/{exerciseId}?windowMonths=6&position=1`
  - `windowMonths`: 0 = histórico
  - `position`: opcional, filtra por posición del ejercicio (contexto de fatiga)

### Configuración
- `GET /api/config`
- `PUT /api/config`

## Tipos de récord calculados

- **MAX_WEIGHT** — mayor peso levantado
- **BEST_VOLUME** — mayor peso × reps en una serie
- **ESTIMATED_1RM** — 1RM estimado (fórmula Epley)
- **Por rango de reps** — récord dentro de cada rango configurado
- Todos filtrables por ventana temporal y posición de fatiga

## Despliegue en Railway

1. Sube el proyecto a GitHub
2. En Railway: New Project → Deploy from GitHub repo
3. Railway detecta el `Dockerfile`
4. Añade las variables de entorno (`SPRING_DATA_MONGODB_URI`, `APP_JWT_SECRET`)
5. Cada push a `main` despliega automáticamente

Para MongoDB usa **MongoDB Atlas** (tier gratuito de 512MB) y pega su connection string en `SPRING_DATA_MONGODB_URI`.
