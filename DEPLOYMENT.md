# 🚀 Deployment Guide

## Prerequisites

- Docker & Docker Compose installed
- Server with SSH access
- Domain name configured (optional)
- SSL certificate (production)

---

## 🔧 Local Development

### 1. Clone Repository
```bash
git clone https://github.com/yourusername/online-store.git
cd online-store
```

### 2. Setup Environment
```bash
cp .env.example .env
# Edit .env with your values
```

### 3. Run with Docker Compose
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

### 4. Access Application
- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **pgAdmin**: http://localhost:5050

---

## 🌐 Production Deployment

### Option 1: Docker Compose on VPS

#### Step 1: Prepare Server
```bash
# SSH into server
ssh user@your-server.com

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

#### Step 2: Deploy Application
```bash
# Create application directory
sudo mkdir -p /opt/online-store
cd /opt/online-store

# Clone repository
git clone https://github.com/yourusername/online-store.git .

# Setup environment
cp .env.example .env
nano .env  # Edit with production values

# Start services
docker-compose -f docker-compose.prod.yml up -d

# Check status
docker-compose -f docker-compose.prod.yml ps
docker-compose -f docker-compose.prod.yml logs app
```

#### Step 3: Setup Nginx Reverse Proxy
```bash
# Install Nginx
sudo apt install nginx

# Create Nginx config
sudo nano /etc/nginx/sites-available/onlinestore
```
```nginx
server {
    listen 80;
    server_name onlinestore.com www.onlinestore.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```
```bash
# Enable site
sudo ln -s /etc/nginx/sites-available/onlinestore /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

#### Step 4: Setup SSL with Let's Encrypt
```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx

# Get certificate
sudo certbot --nginx -d onlinestore.com -d www.onlinestore.com

# Auto-renewal
sudo systemctl enable certbot.timer
```

---

### Option 2: AWS ECS / Azure Container Apps

#### AWS ECS Deployment

1. **Build and Push Image**
```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Build image
docker build -t online-store .

# Tag image
docker tag online-store:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/online-store:latest

# Push to ECR
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/online-store:latest
```

2. **Create ECS Task Definition**
```json
{
  "family": "online-store",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "containerDefinitions": [
    {
      "name": "app",
      "image": "<account-id>.dkr.ecr.us-east-1.amazonaws.com/online-store:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        }
      ],
      "secrets": [
        {
          "name": "POSTGRES_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:db-password"
        },
        {
          "name": "JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:jwt-secret"
        }
      ]
    }
  ]
}
```

3. **Create ECS Service**
```bash
aws ecs create-service \
  --cluster online-store-cluster \
  --service-name online-store-service \
  --task-definition online-store:1 \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx],securityGroups=[sg-xxx],assignPublicIp=ENABLED}"
```

---

### Option 3: Kubernetes

#### k8s-deployment.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: online-store
spec:
  replicas: 3
  selector:
    matchLabels:
      app: online-store
  template:
    metadata:
      labels:
        app: online-store
    spec:
      containers:
      - name: app
        image: yourusername/online-store:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://postgres-service:5432/onlinestore"
        envFrom:
        - secretRef:
            name: online-store-secrets
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: online-store-service
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: online-store
```
```bash
# Apply deployment
kubectl apply -f k8s-deployment.yaml

# Check status
kubectl get pods
kubectl get services
```

---

## 🔐 Security Checklist

- [ ] Change default passwords
- [ ] Use strong JWT secret (min 256 bits)
- [ ] Enable HTTPS/SSL
- [ ] Configure firewall rules
- [ ] Setup database backups
- [ ] Enable application logging
- [ ] Setup monitoring (optional)
- [ ] Configure rate limiting
- [ ] Regular security updates

---

## 📊 Monitoring

### Health Check Endpoints
```bash
# Application health
curl http://localhost:8080/actuator/health

# Detailed health (admin only)
curl http://localhost:8080/actuator/health \
  -H "Authorization: Bearer <admin-token>"

# Metrics
curl http://localhost:8080/actuator/metrics
```

### Log Management
```bash
# Docker logs
docker-compose logs -f app

# Application logs (inside container)
docker-compose exec app tail -f /app/logs/application.log

# Database logs
docker-compose logs postgres
```

---

## 🔄 Backup & Restore

### Database Backup
```bash
# Manual backup
docker-compose exec postgres pg_dump -U postgres onlinestore > backup_$(date +%Y%m%d).sql

# Automated daily backup (cron)
0 2 * * * cd /opt/online-store && docker-compose exec -T postgres pg_dump -U postgres onlinestore > /backups/backup_$(date +\%Y\%m\%d).sql
```

### Database Restore
```bash
# Restore from backup
cat backup_20260122.sql | docker-compose exec -T postgres psql -U postgres onlinestore
```

---

## 🔧 Troubleshooting

### Application won't start
```bash
# Check logs
docker-compose logs app

# Common issues:
# 1. Database not ready -> wait 30s and restart
# 2. Port already in use -> change port in docker-compose.yml
# 3. Missing environment variables -> check .env file
```

### Database connection failed
```bash
# Test database connection
docker-compose exec app ping postgres

# Check database logs
docker-compose logs postgres

# Restart database
docker-compose restart postgres
```

### High memory usage
```bash
# Check resource usage
docker stats

# Restart application
docker-compose restart app

# Adjust memory limits in docker-compose.yml
```

---

## 📈 Scaling

### Horizontal Scaling
```yaml
# docker-compose.prod.yml
services:
  app:
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '1'
          memory: 1G
```

### Load Balancing
Use Nginx or cloud load balancer to distribute traffic across multiple instances.

---

## 🎯 Post-Deployment Checklist

- [ ] Application accessible via domain
- [ ] HTTPS working correctly
- [ ] Database backups configured
- [ ] Monitoring setup (optional)
- [ ] CI/CD pipeline configured
- [ ] Documentation updated
- [ ] Team notified

---

## 📞 Support

For issues or questions:
- GitHub Issues: https://github.com/yourusername/online-store/issues
- Email: support@onlinestore.com