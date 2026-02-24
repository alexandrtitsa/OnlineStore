# 🛒 Online Store - Hexagonal Architecture Demo

![Build Status](https://github.com/yourusername/online-store/workflows/CI%2FCD%20Pipeline/badge.svg)
![Coverage](https://img.shields.io/badge/coverage-85%25-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)
![Docker](https://img.shields.io/badge/Docker-ready-blue.svg)

A production-ready e-commerce application built with **Hexagonal Architecture** (Ports & Adapters), demonstrating clean architecture principles and modern development practices.

## ✨ Features

### 🏗️ Architecture
- **Hexagonal Architecture** - Clean separation of concerns
- **Domain-Driven Design** - Rich domain model
- **CQRS Pattern** - Command Query Responsibility Segregation
- **Event-Driven** - Domain events for side effects
- **Repository Pattern** - Data access abstraction

### 🔐 Security
- **JWT Authentication** - Secure token-based auth
- **Role-Based Access Control** - USER & ADMIN roles
- **Password Encryption** - BCrypt hashing
- **Ownership Validation** - Users can only access their own data

### 📦 Features
- User registration & authentication
- Product catalog
- Order management with state machine
- Payment processing simulation
- Stock management with optimistic locking
- Order history & tracking

### 🛠️ Tech Stack
- **Java 21** - Latest LTS version
- **Spring Boot 3.2** - Application framework
- **PostgreSQL 16** - Primary database
- **Flyway** - Database migrations
- **Thymeleaf** - Server-side rendering
- **Bootstrap 5** - UI framework
- **Swagger/OpenAPI** - API documentation
- **Docker & Docker Compose** - Containerization
- **Testcontainers** - Integration testing
- **GitHub Actions** - CI/CD pipeline

---

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker & Docker Compose (optional)

### 1. Clone Repository
```bash
git clone https://github.com/yourusername/online-store.git
cd online-store
```

### 2. Run with Docker Compose (Recommended)
```bash
# Copy environment template
cp .env.example .env

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app
```

### 3. Or Run Locally
```bash
# Start PostgreSQL
docker-compose up -d postgres

# Run application
mvn spring-boot:run
```

### 4. Access Application
- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **pgAdmin**: http://localhost:5050 (admin@onlinestore.com / admin)

---

## 📖 Documentation

- [Project Structure](PROJECT_STRUCTURE.md) - Detailed architecture overview
- [Docker Guide](DOCKER.md) - Docker setup and usage
- [Deployment Guide](DEPLOYMENT.md) - Production deployment instructions
- [API Documentation](http://localhost:8080/swagger-ui.html) - Interactive API docs

---

## 🧪 Testing
```bash
# Run all tests
mvn clean test

# Run only unit tests
mvn test -Dtest=*Test,!*IntegrationTest

# Run only integration tests
mvn test -Dtest=*IntegrationTest

# Generate coverage report
mvn clean verify
# Open target/site/jacoco/index.html
```

### Test Coverage
- **Domain Layer**: 95%+
- **Application Layer**: 90%+
- **Infrastructure Layer**: 85%+
- **Web Layer**: 80%+
- **Overall**: 85%+

---

## 🔑 Default Credentials

### Test Users (Development)
- **User**: `user@example.com` / `password123`
- **Admin**: `admin@example.com` / `password123`

### pgAdmin
- **Email**: `admin@onlinestore.com`
- **Password**: `admin`

⚠️ **Change these in production!**

---

## 📊 API Examples

### 1. Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "password123"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### 3. Create Order
```bash
TOKEN="<your-access-token>"

curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "productId": "<product-uuid>",
        "quantity": 2
      }
    ]
  }'
```

### 4. Get My Orders
```bash
curl -X GET http://localhost:8080/api/orders/my-orders \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🏗️ Architecture Overview
```
┌─────────────────────────────────────────────────────┐
│                   Web Layer                          │
│  (Controllers, DTOs, Exception Handlers)             │
└───────────────────┬─────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────┐
│              Application Layer                        │
│        (Use Cases, Orchestration)                    │
└───────────────────┬─────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────┐
│               Domain Layer                            │
│  (Entities, Value Objects, Domain Events)            │
│              ⭐ CORE BUSINESS LOGIC                  │
└───────────────────┬─────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────┐
│           Infrastructure Layer                        │
│   (JPA, Security, Messaging, External Services)      │
└─────────────────────────────────────────────────────┘
```

### Key Principles
1. **Dependency Rule**: Dependencies point inward
2. **Ports & Adapters**: Infrastructure adapts to domain
3. **Framework Independence**: Business logic has no framework dependencies
4. **Testability**: Each layer can be tested in isolation

---

## 🔄 CI/CD Pipeline

Automated workflows on every push:

1. **Build & Test** - Maven build + all tests
2. **Code Quality** - SonarCloud analysis
3. **Security Scan** - Trivy + OWASP dependency check
4. **Docker Build** - Multi-platform images
5. **Deploy** - Automated deployment to staging/production

See [.github/workflows/](.github/workflows/) for details.

---

## 🐳 Docker Commands
```bash
# Start services
make up
# or
docker-compose up -d

# View logs
make logs
# or
docker-compose logs -f app

# Stop services
make down
# or
docker-compose down

# Rebuild and restart
make build && make up

# Access database
make shell-db
# or
docker-compose exec postgres psql -U postgres -d onlinestore

# Backup database
make backup-db

# Clean everything
make clean
```

---

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'feat: add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

### Commit Convention
We follow [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` - New feature
- `fix:`