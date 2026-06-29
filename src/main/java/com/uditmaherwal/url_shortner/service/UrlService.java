package com.uditmaherwal.url_shortner.service;

import com.uditmaherwal.url_shortner.model.Counter;
import com.uditmaherwal.url_shortner.repository.UrlMappingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

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
        assert counter != null;
        return counter.getLastId();
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

    private void getShortUrl(String originalUrl){
        if(keyQueue.isEmpty()){
            synchronized (this){
                if(keyQueue.isEmpty()){
                    long lastId = getCounter();
                    refillQueue(lastId);
                }
            }
        }

        String shortUrl = keyQueue.poll();
    }
}
