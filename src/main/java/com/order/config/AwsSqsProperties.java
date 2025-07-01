package com.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.sqs")
@Data
public class AwsSqsProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String region;
}

