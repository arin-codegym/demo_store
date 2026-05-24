# Demo Store

Full-stack e-commerce demo using Next.js, Spring Boot, PostgreSQL RDS, Redis, Docker Compose, Nginx, and GitHub Actions.

## Repository Layout

```text
apps/
  web/   # Next.js frontend
  api/   # Spring Boot backend
infra/   # production compose, Nginx, AWS notes
docs/    # architecture, deployment, database migration notes
```

## Documentation

- [Architecture](docs/architecture.md)
- [Deployment](docs/deployment.md)
- [Database migration](docs/database-migration.md)
- [Nginx reverse proxy](infra/nginx.md)
- [AWS RDS notes](infra/aws-rds-notes.md)

## Local Development

Frontend:

```bash
cd apps/web
npm install
npm run dev
```

Backend:

```bash
cd apps/api
./mvnw spring-boot:run
```

Create local `.env` files from examples and never commit real secrets.