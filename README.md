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

#### 2. Teste Avançado com Threads
```bash
# Enviar 1000 mensagens usando 10 threads concorrentes
./scripts/load-test-bash.sh 1000 10

# Enviar 5000 mensagens usando 20 threads
./scripts/load-test-bash.sh 5000 20
```

#### 3. Monitor de Processamento
```bash
# Monitorar processamento em tempo real (atualiza a cada 5 segundos)
./scripts/monitor-processing.sh

# Monitorar com intervalo personalizado (2 segundos)
./scripts/monitor-processing.sh 2
```

#### 4. Gerar Relatório
```bash
# Gerar relatório detalhado do teste
./scripts/generate-report.sh

# Gerar relatório com nome personalizado
./scripts/generate-report.sh meu-relatorio.txt
```

### Exemplos de Uso

#### Teste Básico
```bash
# 1. Subir infraestrutura
docker-compose up -d

# 2. Executar teste simples
./scripts/simple-load-test.sh 500 50

# 3. Monitorar processamento
./scripts/monitor-processing.sh
```

#### Teste de Alta Carga
```bash
# 1. Executar teste com muitas mensagens simultâneas
./scripts/load-test-bash.sh 2000 20

# 2. Em outro terminal, monitorar
./scripts/monitor-processing.sh

# 3. Gerar relatório final
./scripts/generate-report.sh
```

### Limpeza de Dados de Teste
```bash
# Remover todos os pedidos de teste
mongosh "mongodb://orderuser:orderpass@localhost:27017/orderdb?authSource=admin" --eval "db.orders.deleteMany({external_id: /^LOAD_TEST_/})"
```

### Comandos
```bash
mongosh "mongodb://orderuser:orderpass@localhost:27017/orderdb?authSource=admin"
db.orders.find().pretty()
db.orders.deleteOne({'external_id': '123'})

aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url http://localhost:4566/000000000000/calculate-orders-queue --message-body '{"externalId":"123","customerId":"456","items":[{"productId":"1","productName": "Produto de teste", "quantity":2, "unitPrice": 500}]}'

docker exec -it order-redis redis-cli
```