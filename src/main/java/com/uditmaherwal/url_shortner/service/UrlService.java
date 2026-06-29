package com.uditmaherwal.url_shortner.service;

import com.mongodb.internal.client.model.FindOptions;
import com.uditmaherwal.url_shortner.model.Counter;
import com.uditmaherwal.url_shortner.model.UrlMapping;
import com.uditmaherwal.url_shortner.repository.UrlMappingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlMappingRepository urlMappingRepository;
    private final ConcurrentLinkedQueue<String> keyQueue = new ConcurrentLinkedQueue<>();
    private final MongoTemplate mongoTemplate;

    @PostConstruct
    private void initQueue(){
        refillQueue(getCounter());
    }

    private long getCounter(){
        Counter counter = mongoTemplate
                .findAndModify(
                        new Query(Criteria.where("_id").is("url_counter")),
                        new Update().inc("lastId", 1000),
                        FindAndModifyOptions.options().returnNew(true),
                        Counter.class);
        if (counter != null) {
            return counter.getLastId();
        }
        Counter newCounter = new Counter();
        newCounter.setId("url_counter");
        newCounter.setLastId(10000000);
        mongoTemplate.save(newCounter);
        return newCounter.getLastId();
    }

    private void refillQueue(long id){
        long lastId = id+1000;

        for(long i = id; i < lastId; i++){
            keyQueue.add(encode(i));
        }
    }

    private String encode(long id) {
        String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder shortUrl = new StringBuilder();

        while (id > 0) {
            int remainder = (int) (id % 62);
            shortUrl.append(ALPHABET.charAt(remainder));
            id = id / 62;
        }

        return shortUrl.reverse().toString();
    }

    public String getShortUrl(String originalUrl){
        if(keyQueue.isEmpty()){
            synchronized (this){
                if(keyQueue.isEmpty()){
                    long lastId = getCounter();
                    refillQueue(lastId);
                }
            }
        }

        String shortUrl = keyQueue.poll();
        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);
        mapping.setShortKey(shortUrl);
        urlMappingRepository.save(mapping);
        return shortUrl;
    }

    @Async
    public void incrementClickCount(String shortKey){
        mongoTemplate.updateFirst(
                new Query(Criteria.where("shortKey").is(shortKey)),
                new Update().inc("clickCount", 1),
                UrlMapping.class
        );
    }

    public String redirect(String shortKey){
        UrlMapping mapping = mongoTemplate.findOne(new Query(Criteria.where("shortKey").is(shortKey)),  UrlMapping.class);
        if(mapping == null){
            return "";
        }
        incrementClickCount(shortKey);
        return mapping.getOriginalUrl();
    }
}
