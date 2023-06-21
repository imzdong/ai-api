package org.imzdong.ai.dao.impl;

import jakarta.annotation.Resource;
import org.imzdong.ai.dao.ChatMessageDao;
import org.imzdong.ai.model.Chat;
import org.imzdong.ai.model.ChatMessage;
import org.imzdong.ai.model.req.ChatMessageRequest;
import org.imzdong.ai.model.req.ChatRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ChatMessageDaoImpl implements ChatMessageDao {

    private static final String COLLECTION_NAME = "chat";
    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public Chat addChat(ChatRequest request) {
        Chat chat = Chat.builder()
                .id(request.getChatRoomId())
                .name(request.getChatRoomName())
                .userId(request.getUserId())
                .userName(request.getUserName())
                .botUserId(request.getBotUserId())
                .botName(request.getBotName())
                .model(request.getModel())
                .createdDate(new Date())
                .build();
        return mongoTemplate.insert(chat, COLLECTION_NAME);
    }

    @Override
    public List<Chat> listChatByUserId(String userId) {

        // 创建条件对象
        Criteria criteria = Criteria.where("userId").is(userId);
        // 创建查询对象，然后将条件对象添加到其中，然后根据指定字段进行排序
        Query query = new Query(criteria).with(Sort.by("num"));
        // 执行查询
        List<Chat> list = mongoTemplate.find(query, Chat.class, COLLECTION_NAME);
        // 输出结果
        return list;
    }

    @Override
    public ChatMessage addChatMessage(ChatMessageRequest request) {
        ChatMessage messageHistory = ChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .chatId(request.getChatId())
                .createdDate(new Date())
                .content(request.getMessage())
                .userId(request.getUserId())
                .userName(request.getUserName())
                .num(request.getNum())
                .build();
        return mongoTemplate.insert(messageHistory, COLLECTION_NAME);
    }

    /**
     * 根据【文档ID】查询集合中文档数据
     *
     * @return 文档信息
     */
    @Override
    public List<ChatMessage> findMessagesByChatId(String chatId) {

        // 创建条件对象
        Criteria criteria = Criteria.where("chatId").is(chatId);
        // 创建查询对象，然后将条件对象添加到其中，然后根据指定字段进行排序
        Query query = new Query(criteria).with(Sort.by("num"));
        // 执行查询
        List<ChatMessage> list = mongoTemplate.find(query, ChatMessage.class, COLLECTION_NAME);
        // 输出结果
        return list;
    }

    @Override
    public Chat findByChatId(String chatId) {
        // 执行查询
        return mongoTemplate.findById(chatId, Chat.class, COLLECTION_NAME);

    }



}
