package org.imzdong.ai.dao.impl;

import com.mongodb.client.result.UpdateResult;
import jakarta.annotation.Resource;
import org.imzdong.ai.dao.ChatMessageDao;
import org.imzdong.ai.model.Chat;
import org.imzdong.ai.model.ChatBotMessage;
import org.imzdong.ai.model.dto.ChatMessageDto;
import org.imzdong.ai.model.req.ChatRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Repository
public class ChatMessageDaoImpl implements ChatMessageDao {

    private static final String COLLECTION_NAME_CHAT = "chat";
    private static final String COLLECTION_NAME_CHAT_MSG = "chat_msg";
    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public Chat addChat(ChatRequest request) {
        String chatRoomId = request.getChatRoomId();
        String chatRoomName = request.getChatRoomName();
        List<Chat> chats = listChatByParams(chatRoomId, chatRoomName);
        if(!CollectionUtils.isEmpty(chats)){
            if(chats.size()>1){
                throw new RuntimeException("id and name both has room");
            }
            return chats.get(0);
        }
        Chat chat = Chat.builder()
                .id(chatRoomId)
                .name(chatRoomName)
                .userId(request.getUserId())
                .userName(request.getUserName())
                .botUserId(request.getBotUserId())
                .botName(request.getBotName())
                .model(request.getModel())
                .createdDate(new Date())
                .delFlag(false)
                .build();
        return mongoTemplate.insert(chat, COLLECTION_NAME_CHAT);
    }

    public List<Chat> listChatByParams(String chatId, String chatName) {

        // 创建条件对象
        Criteria delCri = Criteria.where("delFlag").is(false);

        Criteria id = Criteria.where("id").is(chatId);
        Criteria name = Criteria.where("name").is(chatName);
        Criteria orOperator = new Criteria().orOperator(id, name);

        Criteria finalCriteria = new Criteria().andOperator(orOperator, delCri);

        // 创建查询对象，然后将条件对象添加到其中，然后根据指定字段进行排序
        Query query = new Query(finalCriteria).with(Sort.by("num"));
        // 执行查询
        List<Chat> list = mongoTemplate.find(query, Chat.class, COLLECTION_NAME_CHAT);
        // 输出结果
        return list;
    }

    @Override
    public List<Chat> findChatByUserId(String userId) {

        // 创建条件对象
        Criteria criteria = Criteria.where("userId").is(userId);
        Criteria delCri = Criteria.where("delFlag").is(false);
        // 创建查询对象，然后将条件对象添加到其中，然后根据指定字段进行排序
        Query query = new Query(criteria).addCriteria(delCri).with(Sort.by("num"));
        // 执行查询
        List<Chat> list = mongoTemplate.find(query, Chat.class, COLLECTION_NAME_CHAT);
        // 输出结果
        return list;
    }

    @Override
    public Chat findByChatId(String chatId) {
        // 执行查询
        return mongoTemplate.findById(chatId, Chat.class, COLLECTION_NAME_CHAT);

    }

    @Override
    public Boolean delChat(String chatId) {
        Query query = new Query(Criteria.where("id").is(chatId));
        Update update = new Update().set("delFlag", true);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, COLLECTION_NAME_CHAT);
        if(updateResult.getMatchedCount()==1){
            Query queryMsg = new Query(Criteria.where("chatId").is(chatId));
            Update updateMsg = new Update().set("delFlag", true);
            UpdateResult result = mongoTemplate.updateFirst(queryMsg, updateMsg, COLLECTION_NAME_CHAT_MSG);
            return result.getModifiedCount() > 0;
        }
        return updateResult.getMatchedCount() == 1;
    }

    @Override
    public ChatBotMessage addChatMessage(ChatMessageDto dto) {
        ChatBotMessage messageHistory = ChatBotMessage.builder()
                .id(UUID.randomUUID().toString())
                .chatId(dto.getChatId())
                .createdDate(new Date())
                .content(dto.getMessage())
                .userId(dto.getUserId())
                .userName(dto.getUserName())
                .role(dto.getRole())
                .num(System.currentTimeMillis())
                .delFlag(false)
                .build();
        return mongoTemplate.insert(messageHistory, COLLECTION_NAME_CHAT_MSG);
    }

    /**
     * 根据【文档ID】查询集合中文档数据
     *
     * @return 文档信息
     */
    @Override
    public List<ChatBotMessage> findMessagesByChatId(String chatId) {

        // 创建条件对象
        Criteria criteria = Criteria.where("chatId").is(chatId);
        Criteria delCri = Criteria.where("delFlag").is(false);
        // 创建查询对象，然后将条件对象添加到其中，然后根据指定字段进行排序
        Query query = new Query(criteria).addCriteria(delCri).with(Sort.by("num"));
        // 执行查询
        List<ChatBotMessage> list = mongoTemplate.find(query, ChatBotMessage.class, COLLECTION_NAME_CHAT_MSG);
        // 输出结果
        return list;
    }


}
