package com.microservice.twittertokafka;

public class TwitterKafkaRuntimeExcetpion extends RuntimeException{
    public TwitterKafkaRuntimeExcetpion(){
        super();
    }
    public TwitterKafkaRuntimeExcetpion(String message){
        super(message);
    }
    public TwitterKafkaRuntimeExcetpion(String message,Throwable cause){
        super(message,cause);
    }
}
