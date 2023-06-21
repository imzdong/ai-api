package org.imzdong.ai.dao;

import org.imzdong.ai.model.User;
import org.imzdong.ai.model.req.UserRequest;


public interface UserDao {

    User addUser(UserRequest request);

    User findByUserId(String userId);

    User findByUserName(String userName);
}
