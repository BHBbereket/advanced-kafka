package com.microservice.twittertokafka;

import twitter4j.TwitterException;

public interface StreamRunner {
    void start() throws TwitterException;
}
