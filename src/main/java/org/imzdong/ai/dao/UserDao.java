package org.imzdong.ai.dao;

import org.imzdong.ai.model.User;
import org.imzdong.ai.model.req.UserRequest;

import java.util.List;

public interface UserDao {

    User addUser(UserRequest request);

    User findByUserId(String userId);
}
