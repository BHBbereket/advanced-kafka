package com.microservice.commonconfig.config;

import com.microservice.appconfig.config.RetryConfigData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryConfig {
    private final RetryConfigData retryConfigData;

    public RetryConfig(RetryConfigData retryConfigData) {
        this.retryConfigData = retryConfigData;
    }

    @Bean
    public RetryTemplate retryTemplate(){
        RetryTemplate retryTemplate=new RetryTemplate();

        ExponentialBackOffPolicy exponentialBackOff=new ExponentialBackOffPolicy();
        exponentialBackOff.setInitialInterval(retryConfigData.getInitialIntervalMs());
        exponentialBackOff.setMaxInterval(retryConfigData.getMaxItervalMs());
        exponentialBackOff.setMultiplier(retryConfigData.getMultiplier());

        retryTemplate.setBackOffPolicy(exponentialBackOff);

        SimpleRetryPolicy simpleRetryPolicy= new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(retryConfigData.getMaxAttempt());
        retryTemplate.setRetryPolicy(simpleRetryPolicy);

        return retryTemplate;
    }
}
