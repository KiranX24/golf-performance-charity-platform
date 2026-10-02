# Digital Heroes

Full-stack trainee assignment implementation based on the supplied Digital Heroes PRD.

## Backend

`digital-heroes-backend/` is a Spring Boot 3.3.4 + Java 17 REST API using PostgreSQL/Supabase, Flyway, JPA, Spring Security and JWT.

### Run backend

Set these environment variables in STS:

```text
DB_HOST=your-supabase-db-host
DB_PORT=5432
DB_NAME=postgres
DB_USERNAME=postgres
DB_PASSWORD=your-password
JWT_SECRET=replace-with-a-secret-at-least-32-characters
FRONTEND_URL=http://localhost:5173
```

Optional demo seed credentials are enabled by default:

```text
demo@digitalheroes.local / Demo@12345
admin@digitalheroes.local / Admin@12345
```

Swagger:
`http://localhost:8080/swagger-ui.html`

### Main API areas

- `/api/auth/*`
- `/api/plans`
- `/api/subscriptions/*`
- `/api/scores`
- `/api/charities/*`
- `/api/draws`
- `/api/admin/draws/*`
- `/api/winners/*`
- `/api/admin/winners/*`
- `/api/dashboard`
- `/api/admin/*`

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Set `VITE_API_URL` if the backend is not at `http://localhost:8080/api`.

## Important

The database schema is managed by Flyway. Do not manually edit the Supabase tables to bypass migrations.

Production payment and proof-storage integrations need real Stripe/Supabase Storage credentials and server-side webhook verification. The included demo mode exists so the assignment can be exercised locally without payment credentials.
