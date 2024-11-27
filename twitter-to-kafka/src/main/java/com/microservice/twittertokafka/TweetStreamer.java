package com.microservice.twittertokafka;


import com.microservice.appconfig.config.TwitterTokafkaConfigData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TweetStreamer implements StreamRunner {
    private static final Logger log= LoggerFactory.getLogger(TweetStreamer.class);

    private  final TwitterTokafkaConfigData twitterTokafkaConfigData;

    private  final TwitterKakfaStatusListener twitterKakfaStatusListener;
    private final BlockingDeque<Tweet> tweetqueue= new LinkedBlockingDeque<>();
    private static final Random random=new Random();

    private final String[] words = {"bereket", "Bob", "Charlie","xy","xx","yafet","abee","mogi"};
    private final String tweetAsJson =
            "{\"created_at\":\"{0}\","+
            "\"id\":\"{1}\"," +
            "\"text\":\" {2}\"," +
                    "\"user\": {" +
                    "\"id\":\"{3}\""+
                    "}}" ;

    private static final DateTimeFormatter TWITTER_STATUS_DATE_FORMAT = DateTimeFormatter.ofPattern(
                    "EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).withLocale(Locale.ENGLISH);

    public  TweetStreamer(TwitterTokafkaConfigData twitterTokafkaConfigData, TwitterKakfaStatusListener twitterKakfaStatusListener){
        this.twitterTokafkaConfigData = twitterTokafkaConfigData;
        this.twitterKakfaStatusListener = twitterKakfaStatusListener;
    }

    public Tweet getTweet() throws InterruptedException {
        return tweetqueue.take();
    }

    @Override
    public void start() throws TwitterException {
        String[] keywords= twitterTokafkaConfigData.getTwitterKeywords().toArray(new String[0]);
        int mintweetlenth=twitterTokafkaConfigData.getMockMinTweetLength();
        int maxtweetlenth=twitterTokafkaConfigData.getMockMaxTweetLength();
        long sleeptime=twitterTokafkaConfigData.getMockSleepMs();
        log.info("Starting mock filterring twitter stream for keywords {}", Arrays.toString(keywords));
        simulateTwitter(keywords, mintweetlenth, maxtweetlenth, sleeptime);
    }

    private void simulateTwitter(String[] keywords, int mintweetlenth, int maxtweetlenth, long sleeptime) {
        Executors.newScheduledThreadPool(1).submit(()->{
                    try{
                        while (true){
                            String formattedTweetAsJson=getFormattedTweet(keywords, mintweetlenth, maxtweetlenth);
                            System.out.println(formattedTweetAsJson);
                            Status status= TwitterObjectFactory.createStatus(formattedTweetAsJson);
                            twitterKakfaStatusListener.onStatus(status);
                            Sleep(sleeptime);
                        }
                    }catch (TwitterException e){
                        log.error(e.getMessage());
                    }

                }
        );
    }


    private void Sleep(long sleeptime) {
        try{
            Thread.sleep(sleeptime);
        }catch(Exception exe){
            throw new TwitterKafkaRuntimeExcetpion("Error while generating Tweets");
        }
    }

    private String getFormattedTweet(String[] keywords, int mintweetlenth, int maxtweetlenth) {
        String str= ZonedDateTime.now().format(TWITTER_STATUS_DATE_FORMAT).trim();
        String[] params= new String[]{
                str,
                String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)),
                getRandomTweetContent(keywords,mintweetlenth,maxtweetlenth),
                String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE))
        };
        return formatedTweet(params);
    }

    private String formatedTweet(String[] params) {
        String tweet=tweetAsJson;
        for(int i=0; i< params.length; i++){
            tweet=tweet.replace("{"+i+"}", params[i]);
        }
        return tweet;
    }

    private String getRandomTweetContent(String[] keywords, int mintweetlenth, int maxtweetlenth) {
        StringBuilder tweet=new StringBuilder();
        int tweetlength=random.nextInt((maxtweetlenth-mintweetlenth+1)+mintweetlenth);
        ramdomTweetContent(keywords, tweet, tweetlength);
        return tweet.toString();
    }

    private void ramdomTweetContent(String[] keywords, StringBuilder tweet, int tweetlength) {
        int i=0;
        while (i< tweetlength){
            tweet.append(words[random.nextInt(words.length)]).append(" ");
            if(i%2== tweetlength /2){
                tweet.append(keywords[random.nextInt(keywords.length)]).append(" ");
            }
            i++;
        }
    }
}
