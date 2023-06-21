package org.imzdong.ai.controller;

import org.imzdong.ai.model.Chat;
import org.imzdong.ai.model.req.ChatMessageRequest;
import org.imzdong.ai.model.req.ChatRequest;
import org.imzdong.ai.model.res.ChatMessagesResponse;
import org.imzdong.ai.model.res.ChatResponse;
import org.imzdong.ai.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping(path = "/chat")
    public Chat initChat(@RequestBody ChatRequest request){
        return chatService.addChat(request);
    }

    @DeleteMapping(path = "/chat/{chatId}")
    public Boolean delChat(@PathVariable(value = "chatId") String chatId){
        return chatService.delChat(chatId);
    }

    @GetMapping(path = "/chat/user/{userId}")
    public List<Chat> listChat(@PathVariable(value = "userId") String userId){
        return chatService.listChat(userId);
    }

    @GetMapping(path = "/chat/{chatId}/messages")
    public ChatResponse getChatMessage(@PathVariable(value = "chatId") String chatId){
        return chatService.getChatMessage(chatId);
    }

    @PostMapping(path = "/chat/{chatId}/message")
    public ChatMessagesResponse chat(@PathVariable(value = "chatId") String chatId,
                                     @RequestBody ChatMessageRequest request){
        return chatService.chat(chatId,request);
    }

}
