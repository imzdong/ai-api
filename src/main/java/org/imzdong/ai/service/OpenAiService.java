package org.imzdong.ai.service;

import org.imzdong.ai.dao.ChatHistoryDao;
import org.imzdong.ai.model.Chat;
import org.imzdong.ai.model.ChatMessageHistory;
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

import java.util.ArrayList;
import java.util.List;

@Service
public class OpenAiService {

    @Autowired
    private OpenAiApi openAiApi;
    @Autowired
    private ChatHistoryDao chatHistoryDao;

    public Chat addChat(ChatRequest request){
        return chatHistoryDao.addChat(request);
    }

    public List<Chat> listChat(String userId){
        return chatHistoryDao.listChatByUserId(userId);
    }

    public ChatCompletionResult chat(ChatMessageRequest request){
        List<ChatMessage> messages = new ArrayList<>();
        String chatId = request.getChatId();
        List<ChatMessageHistory> byChatId = chatHistoryDao.findMessagesByChatId(chatId);
        if(!CollectionUtils.isEmpty(byChatId)){
            messages.addAll(byChatId.stream().map(m->{
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setContent(m.getContent());
                //'system', 'user', or 'assistant'
                chatMessage.setRole((m.getUserName().equals("chat-gpt")?"assistant":"user"));
                return chatMessage;
            }).toList());
            if(byChatId.size()>5){
                messages.add(new ChatMessage("user","简要总结下你和用户的对话，用作后续的上下文提示prompt，控制在200字以内"));
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
                messages.add(new ChatMessage("user",choice.getMessage().getContent()+"。"+ request.getMessage()));
            }else {
                messages.add(new ChatMessage("user", request.getMessage()));
            }
        }else {
            messages.add(new ChatMessage("user", request.getMessage()));
        }
        chatHistoryDao.addChatMessage(request);
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model(request.getModel())
                .messages(messages)
                .maxTokens(1024)
                .temperature(0.2)
                .build();
        ChatCompletionResult chatCompletion = openAiApi.createChatCompletion(completionRequest);
        ChatMessage message = chatCompletion.getChoices().get(0).getMessage();
        ChatMessageRequest gptRequest = ChatMessageRequest.builder()
                .message(message.getContent())
                .model(request.getModel())
                .num(request.getNum())
                .chatId(request.getChatId())
                .userId("openai-" + request.getModel())
                .userName("chat-gpt").build();
        chatHistoryDao.addChatMessage(gptRequest);
        return chatCompletion;
    }

    public ChatResponse getChatMessage(String chatId) {
        ChatResponse chatResponse = new ChatResponse();
        Chat byChatId = chatHistoryDao.findByChatId(chatId);
        BeanUtils.copyProperties(byChatId, chatResponse);
        List<ChatMessageHistory> messagesByChatId = chatHistoryDao.findMessagesByChatId(chatId);
        List<ChatMessagesResponse> list = messagesByChatId.stream().map(m -> {
            ChatMessagesResponse response = new ChatMessagesResponse();
            BeanUtils.copyProperties(m, response);
            return response;
        }).toList();
        chatResponse.setMessages(list);
        return chatResponse;
    }
}
