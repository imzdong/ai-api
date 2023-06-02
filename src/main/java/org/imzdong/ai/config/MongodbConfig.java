package org.imzdong.ai.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongodbConfig {
    
    @Value("${cloud.mongodb.url}")
    private String uri;

    @Bean
    public MongoClient initClient(){
        return MongoClients.create(uri);
    }

}
