.PHONY: help build run test clean docker-build docker-run docker-stop init-localstack test-api

# Variáveis
APP_NAME = order-service
DOCKER_IMAGE = order-service:latest

help: ## Mostra esta ajuda
	@echo "Comandos disponíveis:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Compila o projeto
	@echo "🔨 Compilando o projeto..."
	mvn clean package -DskipTests

run: ## Executa a aplicação localmente
	@echo "🚀 Executando a aplicação..."
	java -jar target/order-service-1.0.0.jar

test: ## Executa os testes
	@echo "🧪 Executando testes..."
	mvn test

clean: ## Limpa arquivos compilados
	@echo "🧹 Limpando arquivos..."
	mvn clean

docker-build: ## Build da imagem Docker
	@echo "🐳 Construindo imagem Docker..."
	docker build -t $(DOCKER_IMAGE) .

docker-run: ## Executa com Docker Compose
	@echo "🐳 Executando com Docker Compose..."
	docker-compose up --build

docker-stop: ## Para os containers
	@echo "🛑 Parando containers..."
	docker-compose down

docker-logs: ## Mostra logs dos containers
	@echo "📋 Logs dos containers..."
	docker-compose logs -f

init-localstack: ## Inicializa LocalStack e cria filas SQS
	@echo "🚀 Inicializando LocalStack..."
	@chmod +x scripts/init-localstack.sh
	./scripts/init-localstack.sh

test-api: ## Testa as APIs da aplicação
	@echo "🧪 Testando APIs..."
	@chmod +x scripts/test-api.sh
	./scripts/test-api.sh

setup: ## Setup completo do ambiente
	@echo "🔧 Setup completo do ambiente..."
	@make docker-run
	@sleep 30
	@make init-localstack
	@sleep 10
	@make test-api

health: ## Verifica health da aplicação
	@echo "🏥 Verificando health..."
	curl -s http://localhost:8080/actuator/health | jq .

metrics: ## Mostra métricas da aplicação
	@echo "📊 Métricas da aplicação..."
	curl -s http://localhost:8080/actuator/prometheus | head -20

load-test: ## Teste de carga simples
	@echo "⚡ Executando teste de carga..."
	@for i in {1..100}; do \
		curl -s -X POST http://localhost:8080/api/v1/orders \
			-H "Content-Type: application/json" \
			-d '{"externalId":"LOAD-TEST-'$$i'","customerId":"CUST-1","items":[{"productId":"PROD-1","productName":"Test","quantity":1,"unitPrice":10.00}]}' > /dev/null; \
		echo "Pedido $$i enviado"; \
	done

clean-all: ## Limpa tudo (containers, imagens, volumes)
	@echo "🧹 Limpeza completa..."
	docker-compose down -v
	docker system prune -f
	mvn clean

logs-app: ## Logs da aplicação
	@echo "📋 Logs da aplicação..."
	docker-compose logs -f order-service

logs-mongodb: ## Logs do MongoDB
	@echo "📋 Logs do MongoDB..."
	docker-compose logs -f mongodb

logs-redis: ## Logs do Redis
	@echo "📋 Logs do Redis..."
	docker-compose logs -f redis

restart: ## Reinicia a aplicação
	@echo "🔄 Reiniciando aplicação..."
	docker-compose restart order-service

status: ## Status dos containers
	@echo "📊 Status dos containers..."
	docker-compose ps

# Comandos de Cache Redis
cache-status: ## Verifica status do cache Redis
	@echo "🗄️ Verificando status do cache Redis..."
	@echo "📊 Chaves no Redis:"
	docker exec order-redis redis-cli keys "order_cache:*" | wc -l
	@echo "📊 Memória usada:"
	docker exec order-redis redis-cli info memory | grep used_memory_human

cache-clear: ## Limpa todo o cache Redis
	@echo "🗄️ Limpando cache Redis..."
	curl -s -X DELETE http://localhost:8080/api/v1/orders/cache | jq .

cache-info: ## Informações detalhadas do cache
	@echo "🗄️ Informações do cache Redis..."
	@echo "📊 Total de chaves de pedidos:"
	docker exec order-redis redis-cli keys "order_cache:*" | wc -l
	@echo "📊 Chaves de lock:"
	docker exec order-redis redis-cli keys "order_lock:*" | wc -l
	@echo "📊 Chaves de processamento:"
	docker exec order-redis redis-cli keys "order_processing:*" | wc -l
	@echo "📊 TTL de uma chave de exemplo:"
	docker exec order-redis redis-cli ttl "order_cache:1" 2>/dev/null || echo "Nenhuma chave de exemplo encontrada"

cache-test: ## Testa funcionalidades de cache
	@echo "🧪 Testando funcionalidades de cache..."
	@echo "1. Criando pedido de teste..."
	ORDER_RESPONSE=$$(curl -s -X POST http://localhost:8080/api/v1/orders \
		-H "Content-Type: application/json" \
		-d '{"externalId":"CACHE-TEST-001","customerId":"CUST-1","items":[{"productId":"PROD-1","productName":"Test","quantity":1,"unitPrice":10.00}]}'); \
	ORDER_ID=$$(echo "$$ORDER_RESPONSE" | jq -r '.id'); \
	echo "Pedido criado com ID: $$ORDER_ID"; \
	sleep 5; \
	echo "2. Verificando cache antes da consulta:"; \
	curl -s http://localhost:8080/api/v1/orders/$$ORDER_ID/cache-status | jq .; \
	echo "3. Fazendo primeira consulta (deve ir ao banco):"; \
	curl -s http://localhost:8080/api/v1/orders/$$ORDER_ID | jq . | head -5; \
	echo "4. Verificando cache após consulta:"; \
	curl -s http://localhost:8080/api/v1/orders/$$ORDER_ID/cache-status | jq .; \
	echo "5. Fazendo segunda consulta (deve vir do cache):"; \
	curl -s http://localhost:8080/api/v1/orders/$$ORDER_ID | jq . | head -5; \
	echo "✅ Teste de cache concluído!"

redis-cli: ## Acessa CLI do Redis
	@echo "🗄️ Acessando CLI do Redis..."
	docker exec -it order-redis redis-cli

redis-monitor: ## Monitora comandos Redis em tempo real
	@echo "🗄️ Monitorando comandos Redis..."
	docker exec order-redis redis-cli monitor

# Comandos do MongoDB
mongodb-cli: ## Acessa CLI do MongoDB
	@echo "🗄️ Acessando CLI do MongoDB..."
	docker exec -it order-mongodb mongosh -u orderuser -p orderpass orderdb

mongodb-status: ## Verifica status do MongoDB
	@echo "🗄️ Verificando status do MongoDB..."
	docker exec order-mongodb mongosh -u orderuser -p orderpass orderdb --eval "db.stats()"

mongodb-collections: ## Lista coleções do MongoDB
	@echo "🗄️ Listando coleções do MongoDB..."
	docker exec order-mongodb mongosh -u orderuser -p orderpass orderdb --eval "show collections"

mongodb-indexes: ## Lista índices do MongoDB
	@echo "🗄️ Listando índices do MongoDB..."
	docker exec order-mongodb mongosh -u orderuser -p orderpass orderdb --eval "db.orders.getIndexes()"

mongodb-count: ## Conta documentos na coleção orders
	@echo "🗄️ Contando documentos na coleção orders..."
	docker exec order-mongodb mongosh -u orderuser -p orderpass orderdb --eval "db.orders.countDocuments()"

mongodb-sample: ## Mostra amostra de documentos
	@echo "🗄️ Mostrando amostra de documentos..."
	docker exec order-mongodb mongosh -u orderuser -p orderpass orderdb --eval "db.orders.find().limit(3).pretty()"

mongodb-clear: ## Limpa todos os documentos da coleção orders
	@echo "🗄️ Limpando coleção orders..."
	docker exec order-mongodb mongosh -u orderuser -p orderpass orderdb --eval "db.orders.deleteMany({})" 