# 🐳 Docker Setup Guide

## Quick Start

### Development Environment
```bash
# Start everything
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop everything
docker-compose down
```

### Production Environment
```bash
# Copy environment template
cp .env.example .env

# Edit .env with production values
nano .env

# Start production stack
docker-compose -f docker-compose.prod.yml up -d
```

## Available Services

| Service | Port | URL |
|---------|------|-----|
| Application | 8080 | http://localhost:8080 |
| Swagger UI | 8080 | http://localhost:8080/swagger-ui.html |
| PostgreSQL | 5432 | localhost:5432 |
| pgAdmin | 5050 | http://localhost:5050 |

### pgAdmin Credentials
- Email: `admin@onlinestore.com`
- Password: `admin`

## Commands (with Makefile)
```bash
make help           # Show all available commands
make build          # Build Docker images
make up             # Start services
make down           # Stop services
make logs           # Show application logs
make clean          # Clean up everything
make shell-app      # Access app container
make shell-db       # Access database
make backup-db      # Backup database
```

## Without Makefile
```bash
# Build
docker-compose build

# Start
docker-compose up -d

# Logs
docker-compose logs -f app

# Stop
docker-compose down

# Clean volumes
docker-compose down -v
```

## Testing the Application

1. **Check health:**
```bash
   curl http://localhost:8080/actuator/health
```

2. **Register user:**
```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"password123"}'
```

3. **Access Swagger UI:**
```
   http://localhost:8080/swagger-ui.html
```

## Database Access

### Using psql
```bash
docker-compose exec postgres psql -U postgres -d onlinestore
```

### Using pgAdmin
1. Open http://localhost:5050
2. Login with credentials above
3. Add New Server:
    - Name: `OnlineStore`
    - Host: `postgres`
    - Port: `5432`
    - Username: `postgres`
    - Password: `postgres`

## Backup & Restore

### Backup
```bash
docker-compose exec postgres pg_dump -U postgres onlinestore > backup.sql
```

### Restore
```bash
cat backup.sql | docker-compose exec -T postgres psql -U postgres onlinestore
```

## Troubleshooting

### Port already in use
```bash
# Check what's using port 8080
lsof -i :8080

# Change port in docker-compose.yml
ports:
  - "8081:8080"
```

### Database connection issues
```bash
# Check PostgreSQL logs
docker-compose logs postgres

# Restart database
docker-compose restart postgres
```

### Application won't start
```bash
# Check application logs
docker-compose logs app

# Rebuild without cache
docker-compose build --no-cache app
```

## Environment Variables

Required for production:
- `POSTGRES_PASSWORD` - Database password
- `JWT_SECRET` - JWT signing secret (min 256 bits)

Optional:
- `JWT_ACCESS_TOKEN_EXPIRATION_MINUTES` (default: 15)
- `JWT_REFRESH_TOKEN_EXPIRATION_DAYS` (default: 7)
- `APP_PORT` (default: 8080)

## Development Tips

### Hot reload (not in Docker)
```bash
# Run locally with auto-reload
mvn spring-boot:run
```

### Rebuild after code changes
```bash
docker-compose build app
docker-compose up -d app
```

### Watch logs in real-time
```bash
docker-compose logs -f --tail=100 app
```

## Production Deployment

1. **Set environment variables:**
```bash
   cp .env.example .env
   # Edit .env with secure values
```

2. **Build and start:**
```bash
   docker-compose -f docker-compose.prod.yml build
   docker-compose -f docker-compose.prod.yml up -d
```

3. **Check status:**
```bash
   docker-compose -f docker-compose.prod.yml ps
   docker-compose -f docker-compose.prod.yml logs app
```

4. **Monitor:**
```bash
   # Health check
   curl http://localhost:8080/actuator/health
   
   # Metrics
   curl http://localhost:8080/actuator/metrics
```

## Security Notes

⚠️ **Never commit:**
- `.env` file
- Production passwords
- JWT secrets

✅ **Always:**
- Use strong passwords in production
- Change default pgAdmin credentials
- Use HTTPS in production
- Regularly update dependencies