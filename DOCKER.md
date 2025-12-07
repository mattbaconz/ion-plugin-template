# Docker Setup for IonAPI Template

Quick setup for testing the plugin with MySQL and Redis using Docker.

## 🚀 Quick Start

### Start All Services
```bash
docker-compose up -d
```

This starts:
- **MySQL** on port 3306
- **Redis** on port 6379
- **Paper Server** on port 25565 (optional)

### Build and Deploy Plugin
```bash
./gradlew shadowJar
docker-compose restart paper
```

### View Logs
```bash
# All services
docker-compose logs -f

# Just Paper server
docker-compose logs -f paper

# Just MySQL
docker-compose logs -f mysql
```

### Stop Services
```bash
docker-compose down
```

### Clean Everything (including data)
```bash
docker-compose down -v
```

## 📋 Service Details

### MySQL
- **Host**: localhost
- **Port**: 3306
- **Database**: minecraft
- **User**: iontemplate
- **Password**: iontemplate
- **Root Password**: password

### Redis
- **Host**: localhost
- **Port**: 6379
- **No password** (default)

### Paper Server
- **Port**: 25565
- **RCON Port**: 25575
- **RCON Password**: minecraft
- **Version**: 1.20.4
- **Memory**: 2GB

## ⚙️ Configuration

Update your `config.yml` to use Docker services:

```yaml
database:
  type: mysql
  mysql:
    host: localhost  # or 'mysql' if running plugin in Docker
    port: 3306
    database: minecraft
    username: iontemplate
    password: iontemplate

redis:
  enabled: true
  host: localhost  # or 'redis' if running plugin in Docker
  port: 6379
  password: ""
```

## 🔧 Useful Commands

### Connect to MySQL
```bash
docker exec -it iontemplate-mysql mysql -u iontemplate -piontemplate minecraft
```

### Connect to Redis CLI
```bash
docker exec -it iontemplate-redis redis-cli
```

### Execute RCON Command
```bash
docker exec iontemplate-paper rcon-cli --password minecraft "say Hello!"
```

### View Plugin Folder
```bash
docker exec iontemplate-paper ls -la /data/plugins
```

### Copy Plugin Manually
```bash
docker cp build/libs/IonTemplatePlugin-1.0.0.jar iontemplate-paper:/data/plugins/
docker-compose restart paper
```

## 🐛 Troubleshooting

### MySQL Connection Refused
Wait for MySQL to be fully ready:
```bash
docker-compose logs mysql | grep "ready for connections"
```

### Plugin Not Loading
Check Paper logs:
```bash
docker-compose logs paper | grep -i "iontemplate"
```

### Reset Database
```bash
docker-compose down -v
docker-compose up -d mysql
```

### Port Already in Use
Change ports in `docker-compose.yml`:
```yaml
ports:
  - "3307:3306"  # Use 3307 instead of 3306
```

## 📊 Monitoring

### Check Service Health
```bash
docker-compose ps
```

### Resource Usage
```bash
docker stats
```

### Database Size
```bash
docker exec iontemplate-mysql mysql -u root -ppassword -e "
  SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
  FROM information_schema.tables
  WHERE table_schema = 'minecraft'
  GROUP BY table_schema;
"
```

## 🔒 Production Notes

**⚠️ This setup is for DEVELOPMENT ONLY!**

For production:
- Use strong passwords
- Enable MySQL authentication
- Configure Redis password
- Use proper networking
- Set up backups
- Use environment variables for secrets

## 📚 Additional Resources

- [Docker Compose Docs](https://docs.docker.com/compose/)
- [MySQL Docker Image](https://hub.docker.com/_/mysql)
- [Redis Docker Image](https://hub.docker.com/_/redis)
- [itzg/minecraft-server](https://github.com/itzg/docker-minecraft-server)
