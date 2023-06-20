package org.imzdong.ai.dao.impl;

import jakarta.annotation.Resource;
import org.imzdong.ai.dao.UserDao;
import org.imzdong.ai.model.User;
import org.imzdong.ai.model.req.UserRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.UUID;

@Repository
public class UserDaoImpl implements UserDao {

    private static final String COLLECTION_NAME = "chat";
    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public User addUser(UserRequest request) {
        User db = User.builder()
                .name(request.getName())
                .id(UUID.randomUUID().toString())
                .createdDate(new Date())
                .build();
        return mongoTemplate.insert(db, COLLECTION_NAME);
    }


    @Override
    public User findByUserId(String chatId) {
        // 执行查询
        return mongoTemplate.findById(chatId, User.class, COLLECTION_NAME);

    }



}
