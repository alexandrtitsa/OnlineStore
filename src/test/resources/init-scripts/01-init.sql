-- Create additional databases if needed
-- CREATE DATABASE onlinestore_test;

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Initial setup completed
SELECT 'Database initialized successfully!' as message;
```

---

### 8. .dockerignore

**Create:** `.dockerignore`
```
# Maven
target/
!.mvn/wrapper/maven-wrapper.jar
.mvn/

# IDE
.idea/
*.iml
.vscode/
*.swp
*.swo

# Git
.git/
.gitignore

# Logs
logs/
*.log

# OS
.DS_Store
Thumbs.db

# Docker
Dockerfile
docker-compose*.yml
.dockerignore

# Environment
.env
.env.local

# Tests
src/test/

# Documentation
README.md
docs/