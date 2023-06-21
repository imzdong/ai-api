package org.imzdong.ai.dao;

import org.imzdong.ai.model.Chat;
import org.imzdong.ai.model.ChatBotMessage;
import org.imzdong.ai.model.dto.ChatMessageDto;
import org.imzdong.ai.model.req.ChatRequest;

import java.util.List;

public interface ChatMessageDao {

    Chat addChat(ChatRequest request);
    ChatBotMessage addChatMessage(ChatMessageDto dto);
    List<ChatBotMessage> findMessagesByChatId(String chatId);
    Chat findByChatId(String chatId);
    List<Chat> findChatByUserId(String userId);
    Boolean delChat(String chatId);

}
