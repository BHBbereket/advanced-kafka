package com.microservice.twittertokafka;


import com.microservice.appconfig.config.KafkaConfigData;
import com.microservice.kafka.avro.model.TwitterAvroModel;
import com.microservice.kafkaproducer.service.TwitterKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.StatusAdapter;

@Component
public class TwitterKakfaStatusListener extends StatusAdapter{
    private static final Logger log= LoggerFactory.getLogger(TwitterKakfaStatusListener.class);
    private final KafkaConfigData kafkaConfigData;
    private final TwitterKafkaProducer kafkaProducer;
    private final TwitterToAvroTransformer twitterToAvroTransformer;

    public TwitterKakfaStatusListener(KafkaConfigData kafkaConfigData, TwitterKafkaProducer kafkaProducer, TwitterToAvroTransformer twitterToAvroTransformer) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaProducer = kafkaProducer;
        this.twitterToAvroTransformer = twitterToAvroTransformer;
    }

    @Override
    public void onStatus(Status status){
        log.info("Received status with text {} from topic {}",status.getText(),kafkaConfigData.getTopicName());
        TwitterAvroModel twitterAvroModel = twitterToAvroTransformer.getTwitterAvroModelFromStatus(status);
        kafkaProducer.send( kafkaConfigData.getTopicName(), twitterAvroModel.getId(),twitterAvroModel);

    }
}
