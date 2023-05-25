package org.imzdong.ai.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoDBConfig {
    
    private String uri = "xx";

    @Bean
    public MongoClient initClient(){
        return MongoClients.create(uri);
    }

}
