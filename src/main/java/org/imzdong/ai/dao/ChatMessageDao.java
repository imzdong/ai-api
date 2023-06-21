package org.imzdong.ai.dao;

import org.imzdong.ai.model.Chat;
import org.imzdong.ai.model.ChatMessage;
import org.imzdong.ai.model.req.ChatMessageRequest;
import org.imzdong.ai.model.req.ChatRequest;

import java.util.List;

public interface ChatMessageDao {

    Chat addChat(ChatRequest request);
    ChatMessage addChatMessage(ChatMessageRequest request);
    List<ChatMessage> findMessagesByChatId(String chatId);
    Chat findByChatId(String chatId);
    List<Chat> listChatByUserId(String userId);
}
