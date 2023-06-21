package org.imzdong.ai.service;

import org.imzdong.ai.dao.ChatMessageDao;
import org.imzdong.ai.dao.UserDao;
import org.imzdong.ai.model.Chat;
import org.imzdong.ai.model.ChatMessage;
import org.imzdong.ai.model.User;
import org.imzdong.ai.model.req.ChatMessageRequest;
import org.imzdong.ai.model.req.ChatRequest;
import org.imzdong.ai.model.res.ChatMessagesResponse;
import org.imzdong.ai.model.res.ChatResponse;
import org.imzdong.ai.openai.api.OpenAiApi;
import org.imzdong.ai.openai.model.completion.chat.ChatCompletionChoice;
import org.imzdong.ai.openai.model.completion.chat.ChatCompletionRequest;
import org.imzdong.ai.openai.model.completion.chat.ChatCompletionResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class OpenAiService {

    @Autowired
    private OpenAiApi openAiApi;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ChatMessageDao chatMessageDao;

    public Chat addChat(ChatRequest request){
        User byUserId = userDao.findByUserId(request.getUserId());
        User bot = userDao.findByUserName("OpenAI");
        request.setUserName(byUserId.getName());
        request.setBotUserId(bot.getId());
        request.setBotName(bot.getName());
        return chatMessageDao.addChat(request);
    }

    public List<Chat> listChat(String userId){
        return chatMessageDao.listChatByUserId(userId);
    }

    public ChatCompletionResult chat(ChatMessageRequest request){
        List<org.imzdong.ai.openai.model.completion.chat.ChatMessage> messages = new ArrayList<>();
        String chatId = request.getChatId();
        List<ChatMessage> byChatId = chatMessageDao.findMessagesByChatId(chatId);
        if(!CollectionUtils.isEmpty(byChatId)){
            messages.addAll(byChatId.stream().map(m->{
                org.imzdong.ai.openai.model.completion.chat.ChatMessage chatMessage = new org.imzdong.ai.openai.model.completion.chat.ChatMessage();
                chatMessage.setContent(m.getContent());
                //'system', 'user', or 'assistant'
                chatMessage.setRole((m.getUserName().equals("chat-gpt")?"assistant":"user"));
                return chatMessage;
            }).toList());
            if(byChatId.size()>5){
                messages.add(new org.imzdong.ai.openai.model.completion.chat.ChatMessage("user","简要总结下你和用户的对话，用作后续的上下文提示prompt，控制在200字以内"));
                ChatCompletionRequest zj = ChatCompletionRequest.builder()
                        .model(request.getModel())
                        .messages(messages)
                        .maxTokens(1024)
                        .temperature(0.2)
                        .n(1)
                        .build();
                ChatCompletionResult zjResult = openAiApi.createChatCompletion(zj);
                messages = new ArrayList<>();
                ChatCompletionChoice choice = zjResult.getChoices().get(0);
                messages.add(new org.imzdong.ai.openai.model.completion.chat.ChatMessage("user",choice.getMessage().getContent()+"。"+ request.getMessage()));
            }else {
                messages.add(new org.imzdong.ai.openai.model.completion.chat.ChatMessage("user", request.getMessage()));
            }
        }else {
            messages.add(new org.imzdong.ai.openai.model.completion.chat.ChatMessage("user", request.getMessage()));
        }
        chatMessageDao.addChatMessage(request);
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model(request.getModel())
                .messages(messages)
                .maxTokens(1024)
                .temperature(0.2)
                .build();
        ChatCompletionResult chatCompletion = openAiApi.createChatCompletion(completionRequest);
        org.imzdong.ai.openai.model.completion.chat.ChatMessage message = chatCompletion.getChoices().get(0).getMessage();
        ChatMessageRequest gptRequest = ChatMessageRequest.builder()
                .message(message.getContent())
                .model(request.getModel())
                .num(request.getNum())
                .chatId(request.getChatId())
                .userId("openai-" + request.getModel())
                .userName("chat-gpt").build();
        chatMessageDao.addChatMessage(gptRequest);
        return chatCompletion;
    }

    public ChatResponse getChatMessage(String chatId) {
        ChatResponse chatResponse = new ChatResponse();
        Chat byChatId = chatMessageDao.findByChatId(chatId);
        BeanUtils.copyProperties(byChatId, chatResponse);
        List<ChatMessage> messagesByChatId = chatMessageDao.findMessagesByChatId(chatId);
        List<ChatMessagesResponse> list = messagesByChatId.stream().map(m -> {
            ChatMessagesResponse response = new ChatMessagesResponse();
            BeanUtils.copyProperties(m, response);
            return response;
        }).toList();
        chatResponse.setMessages(list);
        return chatResponse;
    }
}
