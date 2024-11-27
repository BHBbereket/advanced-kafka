package com.microservice.kafkaadmin.clients;

import com.microservice.appconfig.config.KafkaConfigData;
import com.microservice.appconfig.config.RetryConfigData;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Component
@AllArgsConstructor
public class KafkaAdminClient {
    private static final Logger log = LoggerFactory.getLogger(KafkaAdminClient.class);
    private final KafkaConfigData kafkaConfigData;
    private final AdminClient adminClient;
    private final RetryConfigData retryConfigData;
    private final RetryTemplate retryTemplate;
    private final WebClient webClient;


    public void createTopics() {
        CreateTopicsResult createTopicsResult;
        try {
            createTopicsResult = retryTemplate.execute(this::doCreateTopics);
        } catch (Throwable e) {
            throw new RuntimeException("reache Maz number of retry for creating kafka topic");
        }
        checkTopicCreated();
    }


    public void checkTopicCreated() {
        try {
           Collection<TopicListing> topics= getTopic();
           int retryCount = 1;
           Integer maxRetry = retryConfigData.getMaxAttempt();
           Double multiplier = retryConfigData.getMultiplier();
           Long sleepTimeMs = retryConfigData.getSleepTimeMs();

           for(String topic: kafkaConfigData.getTopicNameToCreate()){
               while (!isTopicCreated(topics,topic)){
                   checkMaxRetry(retryCount++,maxRetry);
                   sleep(sleepTimeMs);
                   sleepTimeMs = sleepTimeMs * Long.parseLong(String.valueOf(multiplier.doubleValue()));
                   topics = getTopic();

               }
           }
        } catch (Throwable e) {
            throw new RuntimeException("reache Maz number of retry for creating kafka topic");
        }
    }

    public void checkSchemaRegistry() throws Exception {
        int retryCount = 1;
        Integer maxRetry = retryConfigData.getMaxAttempt();
        Double multiplier = retryConfigData.getMultiplier();
        Long sleepTimeMs = retryConfigData.getSleepTimeMs();

        while (!getSchemaRegistryStatus().is2xxSuccessful()){
            try {
                checkMaxRetry(retryCount++,maxRetry);
                sleep(sleepTimeMs);
                sleepTimeMs = sleepTimeMs * Long.parseLong(String.valueOf(multiplier.doubleValue()));
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private HttpStatus getSchemaRegistryStatus() throws Exception {
        try {
            return (HttpStatus) webClient
                    .method(HttpMethod.GET)
                    .uri(kafkaConfigData.getSchemaRegistryUrl())
                    .exchange()
                    .map(ClientResponse::statusCode)
                    .block();
        }catch (Exception exe){
            throw new Exception("unable to know status of schema registry");
        }

    }

    private void checkMaxRetry(int retryCount, Integer maxRetry) {
        if (retryCount > maxRetry){
            throw  new RuntimeException("maxima retry exceeded");
        }
    }

    private void sleep(Long sleepTMs) throws InstantiationException {
        try{
           Thread.sleep(sleepTMs);
        }catch (InterruptedException exe){
            throw new InstantiationException("Error while sleeping for waiting new created topic");
        }
    }

    private boolean isTopicCreated(Collection<TopicListing> topics, String topic) {
        if(topics == null)
            return false;
        return topics.stream().anyMatch(topicname -> topicname.name().equals(topic));
    }

    private CreateTopicsResult doCreateTopics(RetryContext retryContext) {
        List<String> topicName = kafkaConfigData.getTopicNameToCreate();
        log.info(" creatting {} topics, attempt {}",topicName.size(),retryContext.getRetryCount());
        List<NewTopic> kafkatopics = topicName.stream().map(topic -> new NewTopic(
                topic.trim(),
                kafkaConfigData.getNumberOfPartisions(),
                kafkaConfigData.getReplicationFactor()
        )).collect(Collectors.toList());
        return adminClient.createTopics(kafkatopics);
    }

    private Collection<TopicListing> getTopic() throws Throwable{
        Collection<TopicListing> topicListings;
        topicListings=retryTemplate.execute(this::doGetTopics);
        return topicListings;
    }

    private Collection<TopicListing> doGetTopics(RetryContext retryContext) throws ExecutionException, InterruptedException {
        log.info("reading kafka topics");
        Collection<TopicListing> topics = adminClient.listTopics().listings().get();
        if(topics != null){
            topics.forEach(topic -> log.debug("topic with name {}",topic.name()));
        }
        return topics;
    }
}
