package org.imzdong.ai.controller;

import org.imzdong.ai.model.Chat;
import org.imzdong.ai.model.req.ChatMessageRequest;
import org.imzdong.ai.model.req.ChatRequest;
import org.imzdong.ai.model.res.ChatResponse;
import org.imzdong.ai.openai.model.completion.chat.ChatCompletionResult;
import org.imzdong.ai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai")
public class OpenAiController {

    @Autowired
    private OpenAiService openAiService;

    @PostMapping(path = "/chat")
    public Chat initChat(@RequestBody ChatRequest request){
        return openAiService.addChat(request);
    }

    @GetMapping(path = "/chat/user/{userId}")
    public List<Chat> listChat(@PathVariable(value = "userId") String userId){
        return openAiService.listChat(userId);
    }

    @GetMapping(path = "/chat/{chatId}/messages")
    public ChatResponse getChatMessage(@PathVariable(value = "chatId") String chatId){
        return openAiService.getChatMessage(chatId);
    }

    @PostMapping(path = "/chat/message")
    public ChatCompletionResult chat(@RequestBody ChatMessageRequest request){
        return openAiService.chat(request);
    }

}
