# HackGov Docker Setup Guide

## Prerequisites
- Docker 20.10+
- Docker Compose 2.0+

## Quick Start

### 1. Configure Environment Variables

Copy `.env.example` to `.env` and update with your configuration:

```bash
cp .env.example .env
```

Edit `.env` and set your email credentials:

```env
SPRING_EMAIL=your-email@gmail.com
SPRING_EMAIL_PASSWORD=your-app-password
```

### 2. Build and Start Services

```bash
# Build images
docker-compose build

# Start services in the background
docker-compose up -d

# View logs
docker-compose logs -f
```

### 3. Access Applications

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **Database**: localhost:5432

### 4. Stop Services

```bash
docker-compose down
```

## Service Details

### PostgreSQL Database
- **Port**: 5432
- **User**: `hackgov_user`
- **Password**: `hackgov_password`
- **Database**: `hackgov_db`
- **Data Volume**: `postgres_data` (persisted across restarts)

### Backend (Spring Boot)
- **Port**: 8080
- **Framework**: Spring Boot 3.x
- **Java**: 21
- **Build Tool**: Maven
- **Dependencies**: Automatically installed during build

### Frontend (React + Vite)
- **Port**: 5173
- **Framework**: React 19
- **Build Tool**: Vite
- **Server**: Nginx (serves built dist folder and proxies `/api` to backend)

## Common Commands

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f hackgov-backend
docker-compose logs -f hackgov-frontend
docker-compose logs -f hackgov-postgres
```

### Execute Commands in Container

```bash
# Backend Maven commands
docker-compose exec hackgov-backend mvn clean install

# Frontend npm commands
docker-compose exec hackgov-frontend npm install

# Database access
docker-compose exec hackgov-postgres psql -U hackgov_user -d hackgov_db
```

### Rebuild After Code Changes

```bash
# Rebuild specific service
docker-compose build hackgov-backend
docker-compose build hackgov-frontend

# Restart service
docker-compose up -d hackgov-backend
docker-compose up -d hackgov-frontend
```

### Remove Volumes (Reset Database)

```bash
# Remove all containers and volumes
docker-compose down -v
```

## Troubleshooting

### Backend fails to connect to database
- Ensure PostgreSQL is healthy: `docker-compose logs hackgov-postgres`
- Check database credentials in `.env`
- Verify `SPRING_DATASOURCE_URL` is set correctly

### Frontend can't reach backend
- Check backend is running: `docker-compose logs hackgov-backend`
- Verify the Nginx proxy in `nginx.conf` is pointing to `http://hackgov-backend:8080`
- Check network connectivity between containers

### Port Already in Use

Change ports in `docker-compose.yml`:

```yaml
ports:
  - "8081:8080"  # Backend on 8081 instead of 8080
  - "3000:5173"  # Frontend on 3000 instead of 5173
```

### Clean Up Everything

```bash
docker-compose down -v
docker system prune -a
```

## Production Considerations
- Use secrets management for sensitive data (credentials, API keys)
- Configure proper logging levels
- Use persistent volumes for database
- Implement backup strategy for database
- Configure resource limits in `docker-compose.yml`
- Use healthcheck endpoints

## Additional Resources
- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)