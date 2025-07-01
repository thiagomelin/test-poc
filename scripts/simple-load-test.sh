#!/bin/bash

# Script Simples de Teste de Carga para SQS
# Usa apenas bash e AWS CLI - sem dependências especiais
# Uso: ./simple-load-test.sh [total_mensagens] [delay_ms]

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configurações
SQS_ENDPOINT="http://localhost:4566"
QUEUE_URL="http://sqs.sa-east-1.localhost.localstack.cloud:4566/000000000000/calculate-orders-queue"

# Parâmetros do teste
TOTAL_MESSAGES=${1:-100}
DELAY_MS=${2:-100}  # Delay entre mensagens em milissegundos

echo -e "${BLUE}🚀 Teste de Carga Simples - Sistema de Pedidos${NC}"
echo -e "${BLUE}=============================================${NC}"
echo -e "${YELLOW}📊 Configuração:${NC}"
echo -e "   Total de mensagens: ${GREEN}$TOTAL_MESSAGES${NC}"
echo -e "   Delay entre mensagens: ${GREEN}${DELAY_MS}ms${NC}"
echo -e "   Início: $(date '+%H:%M:%S')"
echo ""

# Verificar se o AWS CLI está instalado
if ! command -v aws &> /dev/null; then
    echo -e "${RED}❌ AWS CLI não encontrado. Instale o AWS CLI primeiro.${NC}"
    exit 1
fi

# Verificar se o LocalStack está rodando
echo -e "${YELLOW}🔍 Verificando LocalStack...${NC}"
if ! curl -s "$SQS_ENDPOINT" > /dev/null; then
    echo -e "${RED}❌ LocalStack não está rodando${NC}"
    echo -e "${YELLOW}💡 Execute: docker-compose up -d${NC}"
    exit 1
fi
echo -e "${GREEN}✅ LocalStack está rodando${NC}"

# Verificar se a fila existe
echo -e "${YELLOW}🔍 Verificando fila SQS...${NC}"
if ! aws --endpoint-url="$SQS_ENDPOINT" sqs get-queue-url --queue-name calculate-orders-queue > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Criando fila...${NC}"
    aws --endpoint-url="$SQS_ENDPOINT" sqs create-queue --queue-name calculate-orders-queue
    echo -e "${GREEN}✅ Fila criada${NC}"
else
    echo -e "${GREEN}✅ Fila encontrada${NC}"
fi

# Função para gerar uma mensagem simples
generate_simple_message() {
    local order_id=$1

    # Produtos fixos para simplicidade
    local products=(
        '{"productId":"1","productName":"Notebook Dell","quantity":1,"unitPrice":3500.00}'
        '{"productId":"2","productName":"Mouse Wireless","quantity":2,"unitPrice":89.90}'
        '{"productId":"3","productName":"Teclado Mecânico","quantity":1,"unitPrice":299.90}'
    )

    # Seleciona um produto aleatório
    local product_index=$((RANDOM % 3))
    local selected_product="${products[$product_index]}"

    # Gera customer ID
    local customer_id="CUST_$((1000 + RANDOM % 9000))"

    # Monta a mensagem
    local message="{\"externalId\":\"LOAD_TEST_$(printf %06d $order_id)\",\"customerId\":\"$customer_id\",\"items\":[$selected_product]}"

    echo "$message"
}

# Função para enviar mensagem via AWS CLI
send_message_aws() {
    local message_body="$1"
    local message_id="$2"

    echo -e "${YELLOW}DEBUG - MessageId: $message_id${NC}"
    echo -e "${YELLOW}DEBUG - Message Body:${NC}"
    echo "$message_body"

    aws --endpoint-url="$SQS_ENDPOINT" sqs send-message \
        --queue-url "$QUEUE_URL" \
        --message-body "$message_body" \
        --message-attributes "LoadTest={StringValue=true,DataType=String},MessageId={StringValue=$message_id,DataType=String}"

    local exit_code=$?

    echo -e "${YELLOW}DEBUG - AWS CLI exit code: $exit_code${NC}"

    if [ $exit_code -eq 0 ]; then
        echo "SUCCESS"
    else
        echo -e "${RED}ERROR SQS Send${NC}"
        echo "FAILED"
    fi
}


# Função principal
main() {
    echo -e "${BLUE}🚀 Iniciando envio de mensagens...${NC}"
    echo -e "${BLUE}=====================================${NC}"

    set +e

    local start_time=$(date +%s)
    local successful=0
    local failed=0

    for ((i=1; i<=TOTAL_MESSAGES; i++)); do
        # Gera mensagem
        local message
        message=$(generate_simple_message $i)

       # Envia mensagem
       result=$(send_message_aws "$message" $i)

       if [[ "$result" == *"SUCCESS"* ]]; then
           ((successful++))
           echo -e "${GREEN}✅ Mensagem $i enviada${NC}"
       else
           ((failed++))
           echo -e "${RED}❌ Falha na mensagem $i${NC}"
       fi

        # Progresso a cada 10 mensagens
        if ((i % 10 == 0)); then
            echo -e "${YELLOW}📊 Progresso: $i/$TOTAL_MESSAGES (${successful} sucessos, ${failed} falhas)${NC}"
        fi

        # Delay entre mensagens
        if [ $DELAY_MS -gt 0 ]; then
            sleep $(echo "scale=3; $DELAY_MS / 1000" | bc -l 2>/dev/null || echo "0.1")
        fi
    done

    set -e

    local end_time=$(date +%s)
    local duration=$((end_time - start_time))

    echo -e "${BLUE}=====================================${NC}"
    echo -e "${GREEN}🏁 Teste concluído!${NC}"
    echo -e "${YELLOW}⏱️  Duração: ${GREEN}${duration}s${NC}"
    echo -e "${GREEN}✅ Sucessos: $successful${NC}"
    echo -e "${RED}❌ Falhas: $failed${NC}"

    if [ $TOTAL_MESSAGES -gt 0 ]; then
        local success_rate
        success_rate=$(echo "scale=1; $successful * 100 / $TOTAL_MESSAGES" | bc -l 2>/dev/null || echo "0")
        echo -e "${YELLOW}📊 Taxa de sucesso: ${GREEN}${success_rate}%${NC}"
    fi
}

# Executar teste
main

echo ""
echo -e "${BLUE}💡 Próximos passos:${NC}"
echo -e "   - Monitorar logs: docker-compose logs -f order-service"
echo -e "   - Verificar fila: aws --endpoint-url=http://localhost:4566 sqs get-queue-attributes --queue-url $QUEUE_URL --attribute-names All"
echo -e "   - Verificar MongoDB: mongosh \"mongodb://orderuser:orderpass@localhost:27017/orderdb?authSource=admin\" --eval \"db.orders.find({external_id: /^LOAD_TEST_/}).count()\""
