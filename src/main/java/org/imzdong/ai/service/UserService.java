package org.imzdong.ai.service;

import org.imzdong.ai.dao.ChatMessageDao;
import org.imzdong.ai.dao.UserDao;
import org.imzdong.ai.model.Chat;
import org.imzdong.ai.model.User;
import org.imzdong.ai.model.req.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private ChatMessageDao chatMessageDao;

    public User addUser(UserRequest request){
        return userDao.addUser(request);
    }


    public Boolean delUser(String userId) {
        List<Chat> chats = chatMessageDao.findChatByUserId(userId);
        if(!CollectionUtils.isEmpty(chats)) {
            chats.forEach(m->chatMessageDao.delChat(m.getId()));
        }
        return userDao.delUser(userId);
    }
}
