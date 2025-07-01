#!/bin/bash

# Script de Teste de Carga Concorrente para SQS
# Envia mensagens duplicadas e não duplicadas em paralelo usando AWS CLI
# Uso: ./concurrent-load-test.sh [total_mensagens] [delay_ms]

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
DELAY_MS=${2:-0}  # Delay entre mensagens em milissegundos (0 para máximo concorrência)

# Percentual de mensagens duplicadas
DUPLICATE_PERCENT=30

echo -e "${BLUE}🚀 Teste de Carga Concorrente - Sistema de Pedidos${NC}"
echo -e "${BLUE}====================================================${NC}"
echo -e "${YELLOW}📊 Configuração:${NC}"
echo -e "   Total de mensagens: ${GREEN}$TOTAL_MESSAGES${NC}"
echo -e "   Delay entre mensagens: ${GREEN}${DELAY_MS}ms${NC}"
echo -e "   Percentual duplicadas: ${GREEN}${DUPLICATE_PERCENT}%${NC}"
echo -e "   Início: $(date '+%H:%M:%S')"
echo ""

# Verificar AWS CLI
if ! command -v aws &> /dev/null; then
    echo -e "${RED}❌ AWS CLI não encontrado. Instale o AWS CLI primeiro.${NC}"
    exit 1
fi

# Verificar LocalStack
echo -e "${YELLOW}🔍 Verificando LocalStack...${NC}"
if ! curl -s "$SQS_ENDPOINT" > /dev/null; then
    echo -e "${RED}❌ LocalStack não está rodando${NC}"
    echo -e "${YELLOW}💡 Execute: docker-compose up -d${NC}"
    exit 1
fi
echo -e "${GREEN}✅ LocalStack está rodando${NC}"

# Verificar fila
echo -e "${YELLOW}🔍 Verificando fila SQS...${NC}"
if ! aws --endpoint-url="$SQS_ENDPOINT" sqs get-queue-url --queue-name calculate-orders-queue > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Criando fila...${NC}"
    aws --endpoint-url="$SQS_ENDPOINT" sqs create-queue --queue-name calculate-orders-queue
    echo -e "${GREEN}✅ Fila criada${NC}"
else
    echo -e "${GREEN}✅ Fila encontrada${NC}"
fi

# Função para gerar mensagem única
generate_simple_message() {
    local order_id=$1
    local products=(
        '{"productId":"1","productName":"Notebook Dell","quantity":1,"unitPrice":3500.00}'
        '{"productId":"2","productName":"Mouse Wireless","quantity":2,"unitPrice":89.90}'
        '{"productId":"3","productName":"Teclado Mecânico","quantity":1,"unitPrice":299.90}'
    )
    local product_index=$((RANDOM % 3))
    local selected_product="${products[$product_index]}"
    local customer_id="CUST_$((1000 + RANDOM % 9000))"
    local message="{\"externalId\":\"LOAD_TEST_$(printf %06d $order_id)\",\"customerId\":\"$customer_id\",\"items\":[$selected_product]}"
    echo "$message"
}

# Função para enviar mensagem ao SQS
send_message_aws() {
    local message_body="$1"
    local message_id="$2"

    aws --endpoint-url="$SQS_ENDPOINT" sqs send-message \
        --queue-url "$QUEUE_URL" \
        --message-body "$message_body" \
        --message-attributes "LoadTest={StringValue=true,DataType=String},MessageId={StringValue=$message_id,DataType=String}" > /dev/null 2>&1

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Mensagem $message_id enviada${NC}"
        return 0
    else
        echo -e "${RED}❌ Falha ao enviar mensagem $message_id${NC}"
        return 1
    fi
}

main() {
    echo -e "${BLUE}🚀 Iniciando envio concorrente de mensagens...${NC}"

    local total_duplicates=$((TOTAL_MESSAGES * DUPLICATE_PERCENT / 100))
    local total_unique=$((TOTAL_MESSAGES - total_duplicates))

    echo -e "${YELLOW}Enviando $total_duplicates mensagens DUPLICADAS e $total_unique mensagens ÚNICAS.${NC}"

    # Enviar mensagens únicas
    for ((i=1; i<=total_unique; i++)); do
        message=$(generate_simple_message $i)
        send_message_aws "$message" "MSG_$i" &
        if [ $DELAY_MS -gt 0 ]; then
            sleep $(echo "scale=3; $DELAY_MS / 1000" | bc)
        fi
    done

    # Enviar mensagens duplicadas (em pares com mesmo externalId)
    local pairs=$((total_duplicates / 2))
    for ((i=1; i<=pairs; i++)); do
        local dup_external_id="LOAD_TEST_DUP_$(printf %06d $i)"
        local message="{\"externalId\":\"$dup_external_id\",\"customerId\":\"DUPLICATE_CUST\",\"items\":[{\"productId\":\"99\",\"productName\":\"Produto Duplicado\",\"quantity\":1,\"unitPrice\":10.00}]}"
        send_message_aws "$message" "DUP_${i}_1" &
        send_message_aws "$message" "DUP_${i}_2" &
    done

    wait

    echo -e "${BLUE}🏁 Envio concorrente finalizado.${NC}"
    echo -e "${YELLOW}⏱️  Finalizado em: $(date '+%H:%M:%S')${NC}"
}

main
