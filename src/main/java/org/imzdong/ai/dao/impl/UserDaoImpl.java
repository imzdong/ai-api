package org.imzdong.ai.dao.impl;

import com.mongodb.client.result.UpdateResult;
import jakarta.annotation.Resource;
import org.imzdong.ai.dao.UserDao;
import org.imzdong.ai.model.User;
import org.imzdong.ai.model.req.UserRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.UUID;

@Repository
public class UserDaoImpl implements UserDao {

    private static final String COLLECTION_NAME_USER = "user";
    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public User addUser(UserRequest request) {
        User byUserName = findByUserName(request.getName());
        if(byUserName != null){
            return byUserName;
        }
        User db = User.builder()
                .name(request.getName())
                .id(UUID.randomUUID().toString())
                .createdDate(new Date())
                .delFlag(false)
                .build();
        return mongoTemplate.insert(db, COLLECTION_NAME_USER);
    }


    @Override
    public User findByUserId(String userId) {
        // 执行查询
        return mongoTemplate.findById(userId, User.class, COLLECTION_NAME_USER);
    }

    @Override
    public User findByUserName(String userName) {
        // 创建条件对象
        Criteria criteria = Criteria.where("name").is(userName);
        // 创建查询对象，然后将条件对象添加到其中，然后根据指定字段进行排序
        Query query = new Query(criteria);//.with(Sort.by("num"));
        // 执行查询
        return mongoTemplate.findOne(query, User.class, COLLECTION_NAME_USER);
    }

    @Override
    public Boolean delUser(String userId) {
        Query query = new Query(Criteria.where("id").is(userId));
        Update update = new Update().set("delFlag", true);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, COLLECTION_NAME_USER);
        return updateResult.getMatchedCount() == 1;
    }



}
