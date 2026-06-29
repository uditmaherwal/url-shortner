package com.uditmaherwal.url_shortner.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "url_counter")
public class Counter {
    @Id
    private String id;
    private long lastId;
}
