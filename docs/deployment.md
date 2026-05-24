# Deployment

This project is designed to deploy to a single EC2 instance using Docker Compose and Nginx.

## Expected Layout On EC2

```text
/home/ubuntu/app/
  docker-compose.yml
  .env
```

The `.env` file is created from GitHub Actions secrets and must not be committed.

## Runtime Environment

Important variables:

```env
FRONTEND_TAG=latest
BACKEND_TAG=latest
FRONTEND_IMAGE=your-dockerhub/store-nextjs-frontend
BACKEND_IMAGE=your-dockerhub/store-springboot-backend

FRONTEND_URL=https://example.com
NEXT_PUBLIC_BACKEND_URL=https://example.com/api/backend
API_EXTERNAL=http://backend:8080/api/backend

SPRING_DATASOURCE_URL=jdbc:postgresql://<rds-endpoint>:5432/<db>?sslmode=require
SPRING_DATASOURCE_USERNAME=<username>
SPRING_DATASOURCE_PASSWORD=<password>
```

## Manual Deploy

```bash
cd /home/ubuntu/app
docker compose pull
docker compose up -d
docker compose ps
```

## Health Checks

```bash
curl -i http://localhost:8080/api/backend/product/fetchAllProducts
curl -i https://example.com/api/backend/product/fetchAllProducts
curl -i https://example.com
```

## GitHub Actions Notes

For a monorepo, workflows should live at root:

```text
.github/workflows/frontend.yml
.github/workflows/backend.yml
```

Each workflow must set the correct working directory:

```yaml
defaults:
  run:
    working-directory: apps/web
```

or:

```yaml
defaults:
  run:
    working-directory: apps/api
```