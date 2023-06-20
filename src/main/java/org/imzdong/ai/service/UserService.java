package org.imzdong.ai.service;

import org.imzdong.ai.dao.UserDao;
import org.imzdong.ai.model.User;
import org.imzdong.ai.model.req.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public User addUser(UserRequest request){
        return userDao.addUser(request);
    }


}
