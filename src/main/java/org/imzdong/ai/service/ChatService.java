package org.imzdong.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.imzdong.ai.dao.ChatMessageDao;
import org.imzdong.ai.dao.UserDao;
import org.imzdong.ai.model.Chat;
import org.imzdong.ai.model.ChatBotMessage;
import org.imzdong.ai.model.User;
import org.imzdong.ai.model.dto.ChatMessageDto;
import org.imzdong.ai.model.req.ChatMessageRequest;
import org.imzdong.ai.model.req.ChatRequest;
import org.imzdong.ai.model.res.ChatMessagesResponse;
import org.imzdong.ai.model.res.ChatResponse;
import org.imzdong.ai.openai.api.OpenAiApi;
import org.imzdong.ai.openai.model.completion.chat.ChatCompletionChoice;
import org.imzdong.ai.openai.model.completion.chat.ChatCompletionRequest;
import org.imzdong.ai.openai.model.completion.chat.ChatCompletionResult;
import org.imzdong.ai.openai.model.completion.chat.ChatMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ChatService {

    @Autowired
    private OpenAiApi openAiApi;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ChatMessageDao chatMessageDao;

    private Map<String,User> idAndUsers = new HashMap<>();
    private Map<String,User> nameAndUsers = new HashMap<>();
    private Map<String,List<ChatMessage>> chatMessagesMap = new HashMap<>();
    private Map<String, Chat> chatMap = new HashMap<>();

    public Chat addChat(ChatRequest request){
        User byUserId = userDao.findByUserId(request.getUserId());
        User bot = userDao.findByUserName("OpenAI");
        request.setUserName(byUserId.getName());
        request.setBotUserId(bot.getId());
        request.setBotName(bot.getName());
        return chatMessageDao.addChat(request);
    }

    public Boolean delChat(String chatId){
        return chatMessageDao.delChat(chatId);
    }

    public List<Chat> listChat(String userId){
        return chatMessageDao.listChatByUserId(userId);
    }

    private String ROLE_SYSTEM = "system";
    private String ROLE_USER = "user";
    private String ROLE_ASSISTANT = "assistant";
    private String BOT_NAME = "OpenAI";

    public ChatMessagesResponse chat(String chatId,
                                     ChatMessageRequest request){
        List<ChatMessage> messages = chatMessagesMap.getOrDefault(chatId, new ArrayList<>());
        //'system', 'user', or 'assistant'
        String msg = request.getMessage();
        saveDbMsg(chatId, msg, ROLE_USER, request.getUserId(), null);

        Chat cachChat = getCachChat(chatId);

        messages.add(new ChatMessage(ROLE_USER, msg));
        ChatCompletionRequest userRequest = ChatCompletionRequest.builder()
                .model(cachChat.getModel())
                .messages(messages)
                .maxTokens(1024)
                .temperature(0.2)
                .n(1)
                .build();

        ChatCompletionResult userResponse = openAiApi.createChatCompletion(userRequest);
        ChatMessage message = userResponse.getChoices().get(0).getMessage();
        ChatMessagesResponse response = saveDbMsg(chatId, message.getContent(), message.getRole(), null, BOT_NAME);

        return response;
    }

    private ChatMessagesResponse saveDbMsg(String chatId, String msg,
                           String role, String userId,
                           String userName) {
        User user = getUser(userName, userId);
        ChatMessageDto userMsg = ChatMessageDto.builder()
                .role(role)
                .userName(user!=null?user.getName():"System")
                .chatId(chatId)
                .userId(user!=null?user.getId():"SystemId")
                .message(msg).build();
        ChatBotMessage botMessage = chatMessageDao.addChatMessage(userMsg);
        log.info("bot msg:{}", botMessage.getChatId());
        ChatMessagesResponse response = new ChatMessagesResponse();
        BeanUtils.copyProperties(botMessage, response);
        return response;
    }

    private Chat getCachChat(String chatId){
        Chat chat = chatMap.get(chatId);
        if(chat == null) {
            chat = chatMessageDao.findByChatId(chatId);
            chatMap.put(chatId, chat);
        }
        return chat;
    }

    private User getUser(String name, String id){
        if(StringUtils.hasText(name)){
            User user = nameAndUsers.get(name);
            if(user!=null){
                return user;
            }
            User byUserName = userDao.findByUserName(name);
            if(byUserName != null) {
                nameAndUsers.put(name, byUserName);
                idAndUsers.put(byUserName.getId(), byUserName);
            }
            return byUserName;
        }

        if(StringUtils.hasText(id)){
            User user = idAndUsers.get(id);
            if(user!=null){
                return user;
            }
            User byUserId = userDao.findByUserId(id);
            if(byUserId != null) {
                nameAndUsers.put(byUserId.getName(), byUserId);
                idAndUsers.put(id, byUserId);
            }
            return byUserId;
        }
        return null;
    }

    public ChatResponse getChatMessage(String chatId) {
        ChatResponse chatResponse = new ChatResponse();
        Chat byChatId = chatMessageDao.findByChatId(chatId);
        BeanUtils.copyProperties(byChatId, chatResponse);
        List<ChatBotMessage> messagesByChatId = chatMessageDao.findMessagesByChatId(chatId);
        List<ChatMessagesResponse> list = messagesByChatId.stream().map(m -> {
            ChatMessagesResponse response = new ChatMessagesResponse();
            BeanUtils.copyProperties(m, response);
            return response;
        }).toList();
        chatResponse.setMessages(list);
        return chatResponse;
    }
}
