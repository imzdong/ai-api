package org.imzdong.ai.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongodbConfig {
    
    @Value("${cloud.mongodb.url}")
    private String uri;

    @Value("${cloud.mongodb.dbName}")
    private String dbName;

    @Bean
    public MongoClient initClient(){
        return MongoClients.create(uri);
    }

    @Bean
    public MongoTemplate initTemplate(MongoClient client){
        MongoDatabase database = client.getDatabase(dbName);
        return new MongoTemplate(client, database.getName());
    }



}
