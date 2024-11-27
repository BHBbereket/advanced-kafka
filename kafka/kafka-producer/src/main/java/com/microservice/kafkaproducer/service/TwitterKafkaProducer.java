package com.microservice.kafkaproducer.service;

import com.microservice.kafka.avro.model.TwitterAvroModel;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.CompletableFuture;

@Service
public class TwitterKafkaProducer implements KafkaProducer<Long, TwitterAvroModel> {
    private static final Logger  log = LoggerFactory.getLogger(TwitterKafkaProducer.class);

    private final KafkaTemplate<Long, TwitterAvroModel> kafkaTemplate;

    public TwitterKafkaProducer(KafkaTemplate<Long, TwitterAvroModel> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PreDestroy
    public  void close(){
        if(kafkaTemplate != null){
            log.info(" Closing Kafka producer!");
            kafkaTemplate.destroy();
        }
    }

    @Override
    public void send(String topicName, Long key, TwitterAvroModel message) {
        log.info("Sending messge= '{}' to topic '{}'",message,topicName);
//          kafkaTemplate.send(topicName,key,message)
//        kafkaTemplate.send(topicName,key,message).addCallback(new ListenableFutureCallback<SendResult<Long, TwitterAvroModel>>(){
//            @Override
//            public void onSuccess(SendResult<Long, TwitterAvroModel> result) {
//                System.out.println("Message sent successfully to topic: " + result.getRecordMetadata().topic() +
//                        " partition: " + result.getRecordMetadata().partition() +
//                        " offset: " + result.getRecordMetadata().offset());
//            }
//
//            public void onFailure(Throwable ex) {
//                System.err.println("Failed to send message: " + ex.getMessage());
//            }
//        });
        CompletableFuture<SendResult<Long, TwitterAvroModel>> response= (CompletableFuture<SendResult<Long, TwitterAvroModel>>) kafkaTemplate.send(topicName,key,message);

        response.thenAccept(result ->{
            log.info("message published successfuly");
        }).exceptionally(exe ->{
            log.error("Erorr while publishing '{}'",exe.getMessage());
            return null;
        });


    }
}
