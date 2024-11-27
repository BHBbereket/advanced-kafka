package com.microservice.kafkaadmin.config;


import com.microservice.appconfig.config.KafkaConfigData;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import java.util.HashMap;
import java.util.Map;

@EnableRetry
@Configuration
@AllArgsConstructor
public class KafkaAdminConfig {
    private final KafkaConfigData kafkaConfigData;


    @Bean
    public AdminClient adminClient(){
        Map<String,Object> adminConfiguration= new HashMap<String, Object>();
        adminConfiguration.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,kafkaConfigData.getBootstrapServers());
        return AdminClient.create(adminConfiguration);
    }
}
