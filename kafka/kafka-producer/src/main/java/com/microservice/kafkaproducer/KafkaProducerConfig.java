package com.microservice.kafkaproducer;


import com.microservice.appconfig.config.KafkaConfigData;
import com.microservice.appconfig.config.KafkaProducerConfigData;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


@Configuration
public class KafkaProducerConfig<K extends Serializable, V extends SpecificRecordBase>{
    private final KafkaConfigData kafkaConfigData;
    private final KafkaProducerConfigData kafkaProducerConfigData;

    public KafkaProducerConfig(KafkaConfigData kafkaConfigData, KafkaProducerConfigData kafkaProducerConfigData) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaProducerConfigData = kafkaProducerConfigData;
    }
    @Bean
    public Map<String, Object> producerConfig(){
        Map<String, Object> props= new HashMap<String, Object>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaConfigData.getBootstrapServers());
        props.put(kafkaConfigData.getSchemaRegistryUrlKey(),kafkaConfigData.getSchemaRegistryUrl());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,kafkaProducerConfigData.getKeySerializaerClass());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,kafkaProducerConfigData.getValueSerializerClass());
        props.put(ProducerConfig.BATCH_SIZE_CONFIG,kafkaProducerConfigData.getBatchSize() * kafkaProducerConfigData.getBatchSizeBoostFactor());
        props.put(ProducerConfig.LINGER_MS_CONFIG,kafkaProducerConfigData.getLingerMs() );
        props.put(ProducerConfig.ACKS_CONFIG,kafkaProducerConfigData.getAcks());
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,kafkaProducerConfigData.getRequestTimeoutMs());
        props.put(ProducerConfig.RETRIES_CONFIG,kafkaProducerConfigData.getRetryCount());
        System.out.println(props);
        return props;

    }

    @Bean
    public ProducerFactory<K,V>  producerFactory(){
        return new DefaultKafkaProducerFactory<K, V>(producerConfig());
    }

    @Bean
    public KafkaTemplate<K,V> kafkaTemplate(){
        return new KafkaTemplate<K, V>(producerFactory());
    }
}
