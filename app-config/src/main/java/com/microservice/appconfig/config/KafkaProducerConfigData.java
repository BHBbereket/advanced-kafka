package com.microservice.appconfig.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka-producer-config")
public class KafkaProducerConfigData {
    private String keySerializaerClass;

    private String valueSerializerClass;

    private String compressionType;

    private String acks;

    private Integer batchSize;

    private Integer batchSizeBoostFactor;
    private Integer lingerMs;

    private Integer requestTimeoutMs;

    private Integer retryCount;



}
