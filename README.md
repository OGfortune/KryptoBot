# 🤖 KryptoBot - Cryptocurrency & Stock Price Alert Bot

A powerful Telegram bot for monitoring cryptocurrency and stock prices with customizable alerts. Built with Spring Boot, PostgreSQL, and Redis.

![Status](https://img.shields.io/badge/status-active-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-green)
![License](https://img.shields.io/badge/license-MIT-blue)

## ✨ Features

- 🪙 **Real-time Crypto Prices** - Get current prices for Bitcoin, Ethereum, Solana, and more
- 🔔 **Price Alerts** - Set custom alerts when prices reach your target
- 💾 **Alert Management** - Create, view, and delete alerts easily
- ⚡ **Caching** - Redis-based caching for fast price lookups
- 📱 **Telegram Integration** - Simple and intuitive bot commands
- 🔐 **Secure** - Environment-based secrets, no hardcoded credentials
- 🐳 **Docker Ready** - Full Docker and docker-compose support
- 📈 **24h Change Tracking** - See price changes over the last 24 hours

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.0.2 |
| **Database** | PostgreSQL 15 |
| **Cache** | Redis 7 |
| **ORM** | Hibernate/JPA |
| **Build** | Maven + Jib |
| **APIs** | CoinGecko, Alpha Vantage |
| **Containerization** | Docker, docker-compose |

## 📋 Prerequisites

### For Local Development

- **Java 21+** - [Download](https://adoptium.net/)
- **Maven 3.8+** - [Download](https://maven.apache.org/)
- **Docker Desktop** - [Download](https://www.docker.com/products/docker-desktop)
- **Git** - [Download](https://git-scm.com/)

### Telegram Setup

1. Create a Telegram bot via [@BotFather](https://t.me/botfather)
2. Get your bot token
3. Get your bot username

### API Keys

1. **CoinGecko** - Free API for crypto prices
    - Website: https://www.coingecko.com/en/api
    - No key required for free tier (10-50 calls/min)

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/ogfortune/kryptobot.git
cd kryptobot
```

### 2. Create Environment File

```bash
cp .env.example .env
```

Edit `.env` with your values:

```properties
# Telegram
TELEGRAM_BOT_TOKEN=your_telegram_bot_token_here
TELEGRAM_BOT_USERNAME=your_bot_username

# Database
POSTGRES_USERNAME=kryptobot
POSTGRES_PASSWORD=your_secure_password_here

# API Keys
COIN_GECKO_KEY=your_coingecko_key (optional)
```

### 3. Run with Docker Compose (Recommended)

```bash
# Build the image
mvn clean compile jib:dockerBuild

# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f kryptobot
```

### 4. Run Locally (Development)

```bash
# Start only databases
docker run -d --name postgres \
  -e POSTGRES_DB=kryptobot \
  -e POSTGRES_USER=kryptobot \
  -e POSTGRES_PASSWORD=kryptobot123 \
  -p 5432:5432 \
  postgres:16-alpine

docker run -d --name redis \
  -p 6379:6379 \
  redis:7-alpine

# Run the app
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### 5. Test the Bot

Send messages to your bot on Telegram:

```
/price BTC
/setalert BTC above 50000
/alerts
/deletealert 1
```

## 📖 Usage Guide

### Commands

| Command | Description | Example |
|---------|-------------|---------|
| `/price <symbol>` | Get current price | `/price BTC` |
| `/setalert <symbol> <above/below> <price>` | Create price alert | `/setalert BTC above 50000` |
| `/alerts` | View your active alerts | `/alerts` |
| `/deletealert <number>` | Delete an alert by number | `/deletealert 1` |
| `/help` | Show available commands | `/help` |

### Example Workflow

```
User: /price BTC

Bot: 🪙 BTC
     💰 Price: $45,000.00
     📈 24h Change: +2.50%

User: /setalert BTC above 50000

Bot: ✅ Alert Created!
     🪙 BTC above $50000
     You'll be notified when the price is triggered.

User: /alerts

Bot: 🔔 Your Active Alerts:
     1. 🪙 BTC above $50000
        Created 2 days ago
     
     To delete: /deletealert <number>

User: /deletealert 1

Bot: ✅ Alert Deleted
     BTC above $50000
```

## 🏗️ Project Structure

```
kryptobot/
├── src/main/java/com/oghenemalu/kryptobot/
│   ├── user/                          # User management
│   │   ├── UserEntity.java
│   │   ├── UserRepository.java
│   │   └── UserService.java
│   ├── alert/                         # Alert management
│   │   ├── AlertEntity.java
│   │   ├── AlertRepository.java
│   │   ├── AlertService.java
│   │   └── dto/
│   │       └── AlertDTO.java
│   ├── price/                         # Price fetching
│   │   ├── PriceService.java
│   │   └── dto/
│   │       └── PriceDTO.java
│   ├── bot/                           # Telegram bot
│   │   ├── MyTelegramBot.java
│   │   ├── MenuService.java
│   │   ├── MessageFormatter.java
│   │   └── handlers/
│   │       ├── AlertsHandler.java
│   │       ├── PriceHandler.java
│   │       └── CommandHandler.java
│   ├── scheduler/                     # Background jobs
│   │   └── AlertScheduler.java
│   ├── config/                        # Configuration
│   │   └── BotConfig.java
│   ├── enums/
│   │   ├── AssetType.java
│   │   └── ConditionType.java
│   └── KryptobotApplication.java
├── src/main/resources/
│   ├── application.yaml               # Main config (Docker)
│   ├── application-dev.yaml           # Dev config (localhost)
│   └── application-prod.yaml          # Production config
├── docker-compose.yml
├── pom.xml
├── .env.example
├── README.md
└── .gitignore
```

## 🔧 Configuration

### application.yaml (Docker + Local)

```yaml
spring:
  application:
    name: KryptoBot
    version: 1.0.0

  config:
    import: optional:file:.env[.properties]

  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST:localhost}:5432/kryptobot
    driver-class-name: org.postgresql.Driver
    username: ${POSTGRES_USERNAME}
    password: ${POSTGRES_PASSWORD}
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
      connection-timeout: 20000
      idle-timeout: 30000
      max-lifetime: 1800000
      auto-commit: true

  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
    show-sql: true
    open-in-view: false
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
          fetch_size: 50
        order_inserts: true
        order_updates: true

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: 6379
  cache:
    type: redis

server:
  port: 8080

telegram:
  bot:
    token: ${TELEGRAM_BOT_TOKEN}
    username: ${TELEGRAM_BOT_USERNAME}

coingecko:
  api-key: ${COIN_GECKO_KEY}
  timeout: 10000


management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus, metrics

  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true
  metrics:
    enable:
      jvm: true
      process: true
      system: true

app:
  name: KryptoBot
  description: Telegram bot for cryptocurrency tracking
  version: 1.0.0
```



## 🐳 Docker Setup

### Building the Image

```bash
# Build with Jib
mvn clean compile jib:dockerBuild

# Tag image
docker tag oghenemalu/kryptobot:latest oghenemalu/kryptobot:v1.0.0

# Push to DockerHub
docker login
docker push oghenemalu/kryptobot:latest
```

### Running with Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Clean up (remove volumes)
docker-compose down -v
```

### Health Check

```bash
# Check if app is healthy
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

## 🔐 Security

### Secrets Management

- ✅ **Never commit secrets** - Use `.env.example` as template
- ✅ **Environment variables** - All secrets via `.env` file
- ✅ **.gitignore** - `.env` is excluded from version control
- ✅ **Docker secrets** - Use environment variables in docker-compose
- ✅ **Secure passwords** - Use strong, randomly generated passwords

### Generate Secure Passwords

```bash
# Generate random 32-character password
openssl rand -base64 32

# Example output:
# x7K9mP2wL8qR5vN3jT6bY4gH1fD9cE7aZ2sW8xQ0pM4=
```

## 📊 Architecture

```
┌─────────────────┐
│   Telegram      │
│   User          │
└────────┬────────┘
         │ /commands
         ▼
┌─────────────────────────────────────┐
│   Spring Boot Application           │
│  ┌─────────────────────────────┐    │
│  │ Telegram Bot Handler        │    │
│  │ ├─ AlertsHandler            │    │
│  │ ├─ PriceHandler             │    │
│  │ └─ CallbackHandler          │    │
│  └────────────┬────────────────┘    │
│               │                      │
│  ┌────────────┴──────────────────┐  │
│  │                               │  │
│  ▼                               ▼  │
│ ┌──────────────┐    ┌──────────────┐│
│ │AlertService  │    │PriceService  ││
│ └──────┬───────┘    └──────┬───────┘│
│        │                   │        │
│        │                   ▼        │
│        │            ┌──────────────┐│
│        │            │Redis Cache   ││
│        │            └──────────────┘│
│        │                   ▲        │
│        │                   │        │
│        ▼                   │        │
│ ┌────────────────────────┐ │       │
│ │  AlertScheduler        │ │       │
│ │ (Check every 1 minute) ├─┘       │
│ └────────────────────────┘         │
│        │                            │
│        ▼                            │
│ ┌────────────────────────┐          │
│ │ External APIs          │          │
│ │ ├─ CoinGecko (Crypto)  │          │
│ │ └─ Alpha Vantage (Stock)           │
│ └────────────────────────┘          │
└─────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│   PostgreSQL Database               │
│  ┌─────────────┐   ┌──────────────┐ │
│  │ Users       │   │ Alerts       │ │
│  │ ├─ user_id  │   │ ├─ id        │ │
│  │ ├─ chat_id  │   │ ├─ user_id   │ │
│  │ └─ username │   │ ├─ symbol    │ │
│  └─────────────┘   │ ├─ price     │ │
│                    │ └─ condition │ │
│                    └──────────────┘ │
└─────────────────────────────────────┘
```

## 🔄 Alert Checking Flow

```
1. AlertScheduler runs every 60 seconds
   │
2. Fetch all active alerts from database
   │
3. Group alerts by symbol (BTC, ETH, etc.)
   │
4. For each symbol:
   ├─ Fetch current price from external API
   ├─ Cache the price (10 minutes)
   └─ Check all alerts for that symbol
      │
      └─ For each alert:
         ├─ Check if condition met (ABOVE/BELOW)
         │
         ├─ If triggered:
         │  ├─ Send Telegram notification
         │  └─ Deactivate alert
         │
         └─ If not triggered:
            └─ Keep active (check again next minute)
```

## 🧪 Testing

### Manual Testing

```bash
# Check PostgreSQL connection
docker exec kryptobot-postgres psql -U kryptobot -d kryptobot -c "SELECT 1"

# Check Redis connection
docker exec kryptobot-redis redis-cli ping

# Check application health
curl http://localhost:8080/actuator/health

# View application logs
docker-compose logs -f kryptobot
```

### Test Commands on Telegram

```
/price BTC
/price ETH


/setalert BTC above 40000
/setalert ETH below 1500

/alerts

/deletealert 1
```

## 📈 Performance Considerations

### Caching Strategy

- **Crypto Prices**: Cached for 10 minutes (API limit: 10-30 calls/min)
- **Redis TTL**: Configurable via `spring.cache.redis.time-to-live`

### Database Optimization

- **Connection Pool**: HikariCP with 5 connections
- **Indexes**: `idx_alerts_user_active` on (user_id, is_active)
- **Batch Size**: 20 statements (configurable)

### Alert Checking

- **Frequency**: Every 60 seconds (configurable)
- **Grouped Queries**: Minimizes API calls by grouping alerts by symbol
- **Async Notifications**: Sent asynchronously to not block checks

## 🚀 Deployment

### To Heroku

```bash
# Create Heroku app
heroku create your-app-name

# Add PostgreSQL addon
heroku addons:create heroku-postgresql:hobby-dev

# Add Redis addon
heroku addons:create heroku-redis:premium-0

# Set environment variables
heroku config:set TELEGRAM_BOT_TOKEN=your_token
heroku config:set TELEGRAM_BOT_USERNAME=your_username

# Deploy
git push heroku main
```

### To AWS

```bash
# Push image to ECR
aws ecr create-repository --repository-name kryptobot
aws ecr get-login-password | docker login --username AWS --password-stdin [YOUR_ECR_URL]
docker tag oghenemalu/kryptobot:latest [YOUR_ECR_URL]/kryptobot:latest
docker push [YOUR_ECR_URL]/kryptobot:latest

# Deploy with ECS or EKS
# Configure RDS PostgreSQL and ElastiCache Redis
# Update environment variables in task definition
```

## 🤝 Contributing

Contributions are welcome! Here's how to contribute:

### 1. Fork the Repository

```bash
git clone https://github.com/yourusername/kryptobot.git
cd kryptobot
```

### 2. Create a Feature Branch

```bash
git checkout -b feature/amazing-feature
```

### 3. Make Your Changes

- Follow Java naming conventions
- Add comments for complex logic
- Update tests if applicable

### 4. Commit Your Changes

```bash
git commit -m "Add amazing feature"
```

### 5. Push to Your Fork

```bash
git push origin feature/amazing-feature
```

### 6. Open a Pull Request

- Describe your changes clearly
- Reference any related issues
- Wait for review

## 📝 Code Style

- **Java**: Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- **Naming**:
    - Classes: PascalCase (e.g., `AlertService`)
    - Variables: camelCase (e.g., `targetPrice`)
    - Constants: UPPER_SNAKE_CASE (e.g., `DEFAULT_TIMEOUT`)
- **Comments**: Use `//` for inline, `/** */` for methods

## 🐛 Troubleshooting

### PostgreSQL Connection Failed

```
Error: java.net.UnknownHostException: postgres
```

**Solution**: Ensure you're using `localhost` for local development and `postgres` for Docker:

```yaml
# Local and Docker development (application.yaml)
#POSTGRES_HOST should equal postgres in .env
url: jdbc:postgresql://${POSTGRES_HOST:localhost}:5432/kryptobot

```

### Redis Connection Failed

```
Error: Unable to connect to Redis
```

**Solution**: Check Redis is running:

```bash
docker ps | grep redis
docker logs kryptobot-redis
```

### Bot Not Receiving Messages

```
Bot doesn't respond to commands
```

**Solution**:
1. Verify bot token is correct in `.env`
2. Check application logs: `docker-compose logs -f kryptobot`
3. Verify bot is registered: `curl https://api.telegram.org/bot<TOKEN>/getMe`

### Out of Memory

```
java.lang.OutOfMemoryError: Java heap space
```

**Solution**: Increase JVM memory in `application.yaml`:

```yaml
server:
  tomcat:
    max-threads: 200
```

Or in docker-compose.yml:

```yaml
kryptobot:
  environment:
    JAVA_OPTS: "-Xmx512m -Xms256m"
```

## 📚 Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Telegram Bot API](https://core.telegram.org/bots/api)
- [CoinGecko API](https://www.coingecko.com/en/api)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/documentation)
- [Docker Documentation](https://docs.docker.com/)


## 👨‍💻 Author

**Oghenemalu Ighoiye**

- GitHub: [@OGFortune](https://github.com/OGFortune)

- LinkedIn: https://www.linkedin.com/in/oghenemalu-fortune-ighoiye-3bb916186/)



**Made with ❤️ by Oghenemalu Ighoiye**

⭐ If this project helped you, please give it a star!
