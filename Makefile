.PHONY: help build up down logs clean test

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build Docker images
	docker-compose build

up: ## Start all services
	docker-compose up -d

down: ## Stop all services
	docker-compose down

logs: ## Show logs
	docker-compose logs -f app

logs-db: ## Show database logs
	docker-compose logs -f postgres

restart: ## Restart application
	docker-compose restart app

clean: ## Clean up containers and volumes
	docker-compose down -v
	docker system prune -f

test: ## Run tests
	mvn clean test

prod-up: ## Start production environment
	docker-compose -f docker-compose.prod.yml up -d

prod-down: ## Stop production environment
	docker-compose -f docker-compose.prod.yml down

prod-logs: ## Show production logs
	docker-compose -f docker-compose.prod.yml logs -f app

status: ## Show container status
	docker-compose ps

shell-app: ## Shell into application container
	docker-compose exec app sh

shell-db: ## Shell into database container
	docker-compose exec postgres psql -U postgres -d onlinestore

backup-db: ## Backup database
	docker-compose exec postgres pg_dump -U postgres onlinestore > backup_$(shell date +%Y%m%d_%H%M%S).sql

restore-db: ## Restore database (usage: make restore-db FILE=backup.sql)
	docker-compose exec -T postgres psql -U postgres onlinestore < $(FILE)