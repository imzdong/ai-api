package org.imzdong.ai.controller;

import org.imzdong.ai.model.Chat;
import org.imzdong.ai.model.req.ChatMessageRequest;
import org.imzdong.ai.model.req.ChatRequest;
import org.imzdong.ai.openai.model.completion.chat.ChatCompletionResult;
import org.imzdong.ai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenAiController {

    @Autowired
    private OpenAiService openAiService;

    @PostMapping(path = "/initChat")
    public Chat initChat(@RequestBody ChatRequest request){
        return openAiService.addChat(request);
    }

    @PostMapping(path = "/chat")
    public ChatCompletionResult chat(@RequestBody ChatMessageRequest request){
        return openAiService.chat(request);
    }

}
