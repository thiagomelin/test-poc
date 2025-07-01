## 📋 Pré-requisitos

- Docker e Docker Compose
- Java 17+
- Maven 3.9+
- AWS CLI (para testes de carga)

## 🚀 Como Executar

### 1. Subir a Infraestrutura

```bash
# Subir MongoDB, Redis e LocalStack
docker-compose up -d
```

## 🔧 Configurações de Ambiente

### Variáveis de Ambiente

```bash
# MongoDB
SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/orderdb
MONGO_INITDB_ROOT_USERNAME=orderuser
MONGO_INITDB_ROOT_PASSWORD=orderpass

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# AWS SQS (LocalStack)
AWS_ENDPOINT=http://localhost:4566
AWS_ACCESS_KEY_ID=test
AWS_SECRET_ACCESS_KEY=test
AWS_REGION=us-east-1
```

### Criação da Fila
```bash
aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name calculate-orders-queue
```

## 🧪 Teste de Carga

### Scripts Disponíveis

O projeto inclui vários scripts para simular carga de mensagens simultâneas:

#### 1. Teste Simples (Recomendado)
```bash
# Enviar 100 mensagens com delay de 100ms entre elas
./scripts/simple-load-test.sh 100 100

# Enviar 1000 mensagens sem delay (máxima velocidade)
./scripts/simple-load-test.sh 1000 0
```

### Comandos
```bash
mongosh "mongodb://orderuser:orderpass@localhost:27017/orderdb?authSource=admin"
db.orders.find().pretty()
db.orders.deleteOne({'external_id': '123'})

aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url http://localhost:4566/000000000000/calculate-orders-queue --message-body '{"externalId":"123","customerId":"456","items":[{"productId":"1","productName": "Produto de teste", "quantity":2, "unitPrice": 500}]}'

docker exec -it order-redis redis-cli
```