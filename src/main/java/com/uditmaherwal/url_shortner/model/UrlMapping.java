package com.uditmaherwal.url_shortner.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "url_mappings")
public class UrlMapping {

    @Id
    private String shortKey;
    private String originalUrl;
    private LocalDateTime createdAt;
}