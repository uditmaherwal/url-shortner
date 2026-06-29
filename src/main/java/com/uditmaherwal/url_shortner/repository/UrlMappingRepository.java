package com.uditmaherwal.url_shortner.repository;

import com.uditmaherwal.url_shortner.model.UrlMapping;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface UrlMappingRepository extends MongoRepository<UrlMapping, String> {
}
