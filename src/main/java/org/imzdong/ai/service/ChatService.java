package org.imzdong.ai.service;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;
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
import org.imzdong.ai.openai.model.completion.chat.ChatCompletionRequest;
import org.imzdong.ai.openai.model.completion.chat.ChatCompletionResult;
import org.imzdong.ai.openai.model.completion.chat.ChatMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatService {

    @Autowired
    private OpenAiApi openAiApi;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ChatMessageDao chatMessageDao;

    private Map<String, User> idAndUsers = new HashMap<>();
    private Map<String, User> nameAndUsers = new HashMap<>();
    private Map<String, List<ChatMessage>> chatMessagesMap = new HashMap<>();
    private Map<String, Chat> chatMap = new HashMap<>();
    private long MAX_TOKEN = 1024L;
    private String ROLE_SYSTEM = "system";
    private String ROLE_USER = "user";
    private String ROLE_ASSISTANT = "assistant";
    private String BOT_NAME = "OpenAI";
    private String SYSTEM_ASSISTANT_MSG = "You are a helpful assistant.";
    private String SYSTEM_SUMMARIZED_MSG = "请总结一下上面User和Assistant聊了些什么,字数控制在200字以内。";

    public Chat addChat(ChatRequest request) {
        User byUserId = userDao.findByUserId(request.getUserId());
        User bot = userDao.findByUserName("OpenAI");
        request.setUserName(byUserId.getName());
        request.setBotUserId(bot.getId());
        request.setBotName(bot.getName());
        return chatMessageDao.addChat(request);
    }

    public Boolean delChat(String chatId) {
        return chatMessageDao.delChat(chatId);
    }

    public List<Chat> listChat(String userId) {
        return chatMessageDao.findChatByUserId(userId);
    }

    public ChatMessagesResponse chat(String chatId,
                                     ChatMessageRequest request) {
        List<ChatMessage> messages = chatMessagesMap.getOrDefault(chatId, new ArrayList<>());
        if (CollectionUtils.isEmpty(messages)) {
            messages.add(new ChatMessage(ROLE_SYSTEM, SYSTEM_ASSISTANT_MSG));
            saveDbMsg(chatId, SYSTEM_ASSISTANT_MSG, ROLE_SYSTEM, null, BOT_NAME);
            chatMessagesMap.put(chatId, messages);
        }
        //'system', 'user', or 'assistant'
        String msg = request.getMessage();
        saveDbMsg(chatId, msg, ROLE_USER, request.getUserId(), null);
        messages.add(new ChatMessage(ROLE_USER, msg));
        ChatCompletionResult userResponse = sendOpenAiMsg(chatId, messages);
        ChatMessage message = userResponse.getChoices().get(0).getMessage();
        String botMsg = message.getContent();
        messages.add(new ChatMessage(message.getRole(), botMsg));
        ChatMessagesResponse response = saveDbMsg(chatId, botMsg, message.getRole(), null, BOT_NAME);
        return response;
    }

    //计算token
    //https://github.com/openai/openai-cookbook/blob/main/examples/How_to_count_tokens_with_tiktoken.ipynb
    private int countTokens(List<ChatMessage> newMessages, String model) {

        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        Optional<ModelType> modelType = ModelType.fromName(model);
        ModelType type = ModelType.GPT_3_5_TURBO;
        if(modelType.isPresent()){
            type = modelType.get();
        }
        Encoding enc = registry.getEncodingForModel(type);
        String msg = newMessages.stream().map(ChatMessage::getContent).collect(Collectors.joining());
        int tokens = enc.countTokens(msg);
        log.info("used tokens: {}", tokens);
        return tokens;
        //List<Integer> encoded = enc.encode("This is a sample sentence.");
        // encoded = [2028, 374, 264, 6205, 11914, 13]

        //String decoded = enc.decode(encoded);
        // decoded = "This is a sample sentence."

    }

    /**
     * Encoding name	OpenAI models
     * cl100k_base	gpt-4, gpt-3.5-turbo, text-embedding-ada-002
     * p50k_base	Codex models, text-davinci-002, text-davinci-003
     * r50k_base (or gpt2)	GPT-3 models like davinci
     */

    private ChatCompletionResult sendOpenAiMsg(String chatId, List<ChatMessage> messages) {
        Chat cachChat = getCachChat(chatId);
        String model = cachChat.getModel();
        int totalTokens = countTokens(messages, model);

        if (totalTokens > MAX_TOKEN) {
            messages.add(new ChatMessage(ROLE_SYSTEM, SYSTEM_SUMMARIZED_MSG));
            saveDbMsg(chatId, SYSTEM_SUMMARIZED_MSG, ROLE_SYSTEM, null, BOT_NAME);
            ChatCompletionResult summarizedResult = doSendOpenAiMsg(model, messages);
            ChatMessage summarizedMessage = summarizedResult.getChoices().get(0).getMessage();
            String content = summarizedMessage.getContent();
            saveDbMsg(chatId, content, ROLE_SYSTEM, null, BOT_NAME);
            chatMessagesMap.remove(chatId);
            List<ChatMessage> newMessages = new ArrayList<>();
            chatMessagesMap.put(chatId, newMessages);
            newMessages.add(new ChatMessage(ROLE_SYSTEM, SYSTEM_ASSISTANT_MSG));
            saveDbMsg(chatId, SYSTEM_ASSISTANT_MSG, ROLE_SYSTEM, null, BOT_NAME);
            newMessages.add(new ChatMessage(ROLE_SYSTEM, content));
            saveDbMsg(chatId, content, ROLE_SYSTEM, null, BOT_NAME);
            newMessages.add(new ChatMessage(ROLE_USER, messages.get(messages.size()-2).getContent()));
            messages = newMessages;
        }

        return doSendOpenAiMsg(model, messages);
    }

    private ChatCompletionResult doSendOpenAiMsg(String model, List<ChatMessage> messages){
        ChatCompletionRequest userRequest = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .maxTokens(1024)
                .temperature(0.2)
                .n(1)
                .build();
        return openAiApi.createChatCompletion(userRequest);
    }

    private ChatMessagesResponse saveDbMsg(String chatId, String msg,
                                           String role, String userId,
                                           String userName) {
        User user = getUser(userName, userId);
        ChatMessageDto userMsg = ChatMessageDto.builder()
                .role(role)
                .userName(user != null ? user.getName() : "System")
                .chatId(chatId)
                .userId(user != null ? user.getId() : "SystemId")
                .message(msg).build();
        ChatBotMessage botMessage = chatMessageDao.addChatMessage(userMsg);
        log.info("bot msg:{}", botMessage.getChatId());
        ChatMessagesResponse response = new ChatMessagesResponse();
        BeanUtils.copyProperties(botMessage, response);
        return response;
    }

    private Chat getCachChat(String chatId) {
        Chat chat = chatMap.get(chatId);
        if (chat == null) {
            chat = chatMessageDao.findByChatId(chatId);
            chatMap.put(chatId, chat);
        }
        return chat;
    }

    private User getUser(String name, String id) {
        if (StringUtils.hasText(name)) {
            User user = nameAndUsers.get(name);
            if (user != null) {
                return user;
            }
            User byUserName = userDao.findByUserName(name);
            if (byUserName != null) {
                nameAndUsers.put(name, byUserName);
                idAndUsers.put(byUserName.getId(), byUserName);
            }
            return byUserName;
        }

        if (StringUtils.hasText(id)) {
            User user = idAndUsers.get(id);
            if (user != null) {
                return user;
            }
            User byUserId = userDao.findByUserId(id);
            if (byUserId != null) {
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
        List<String> roles = Arrays.asList(ROLE_USER, ROLE_ASSISTANT);
        List<ChatMessagesResponse> list = messagesByChatId.stream()
                .filter(m -> roles.contains(m.getRole()))
                .map(m -> {
                    ChatMessagesResponse response = new ChatMessagesResponse();
                    BeanUtils.copyProperties(m, response);
                    return response;
                }).toList();
        chatResponse.setMessages(list);
        return chatResponse;
    }
}
