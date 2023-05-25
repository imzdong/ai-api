package org.imzdong.ai.dao;

import org.imzdong.ai.model.Chat;
import org.imzdong.ai.model.ChatMessageHistory;
import org.imzdong.ai.model.req.ChatMessageRequest;
import org.imzdong.ai.model.req.ChatRequest;

import java.util.List;

public interface ChatHistoryDao {

    Chat addChat(ChatRequest request);
    ChatMessageHistory addChatMessage(ChatMessageRequest request);
    List<ChatMessageHistory> findMessagesByChatId(String chatId);
    Chat findByChatId(String chatId);
}
