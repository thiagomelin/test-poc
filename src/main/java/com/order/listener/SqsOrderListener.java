package com.order.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.actions.command.CreateOrderCommand;
import com.order.resources.request.OrderRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsOrderListener {

    private final CreateOrderCommand createOrderCommand;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.calculate-order-queue}")
    private String queueUrl;

    @PostConstruct
    public void startListener() {
        CompletableFuture.runAsync(this::pollMessages);
    }

    private void pollMessages() {
        while (true) {
            try {
                ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(5)
                        .build();

                List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

                for (Message msg : messages) {
                    processMessage(msg);
                }
            } catch (Exception e) {
                log.error("[SqsOrderListener] error.polling.message");
                log.error("[SqsOrderListener] Error: {}", e.getMessage(), e);
            }
        }
    }

    private void processMessage(Message message) {
        try {
            String body = message.body();
            log.info("[SqsOrderListener] Processing message SQS: {}", body);

            OrderRequest orderRequest = objectMapper.readValue(body, OrderRequest.class);

                try {
                    createOrderCommand.receiveOrder(orderRequest);
                    log.info("[SqsOrderListener] Process message: {}", orderRequest.externalId());

                    deleteMessage(message);
                } catch (Exception e) {
                    log.error("Erro ao processar pedido via SQS: {}", e.getMessage(), e);
                    deleteMessage(message);
                    //DLQ
                }
        } catch (Exception e) {
            log.error("[SqsOrderListener] error.retrieving.message");
            log.error("[SqsOrderListener] Error: {}", e.getMessage(), e);
        }
    }

    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();

        log.info("[SqsOrderListener] Removing message {} from queue", message.messageId());
        sqsClient.deleteMessage(deleteRequest);
    }
}
