# L-Split

A full-stack bill-splitting application. Users form groups, log expenses under named events, and track who owes whom.

## Tech Stack

- **Frontend**: React 18 + Vite + Bootstrap 5
- **Backend**: Java 21 + Spring Boot 3.4 + PostgreSQL
- **Auth**: JWT (jjwt 0.12.6)
- **DB Migrations**: Flyway
- **Deployment**: Vercel (FE) + Render (BE) + Aiven (DB)

## Local Development

### Prerequisites
- Java 21
- Node 22
- PostgreSQL 16 (or Aiven free tier)

### Backend

```bash
cd backend
# Set environment variables
export DB_URL=jdbc:postgresql://localhost:5432/lsplit
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=$(openssl rand -hex 32)

mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at http://localhost:5173 and proxies `/api` to http://localhost:8080.

## Environment Variables

### Backend (Render / local)

| Variable      | Description                                        |
|---------------|----------------------------------------------------|
| `DB_URL`      | PostgreSQL JDBC URL                                |
| `DB_USERNAME` | Database username                                  |
| `DB_PASSWORD` | Database password                                  |
| `JWT_SECRET`  | 32-byte hex secret (`openssl rand -hex 32`)        |
| `PORT`        | Server port (default 8080)                         |

### Frontend (Vercel)

| Variable            | Description                                               |
|---------------------|-----------------------------------------------------------|
| `VITE_API_BASE_URL` | Backend API URL (e.g. `https://your-app.onrender.com/api`) |

## CI/CD

GitHub Actions runs backend tests (`mvn test`) and frontend tests (`npm run test`) on every push.
Deployment to Render and Vercel is triggered on merge to `main` via deploy hooks.

GitHub secrets required: `RENDER_DEPLOY_HOOK`, `VERCEL_DEPLOY_HOOK`

## API

See `ai/API contract.md` for the full authoritative API contract.
