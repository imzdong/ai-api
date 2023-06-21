package org.imzdong.ai.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageDto {

    private String chatId;
    private String userName;
    private String message;
    private String userId;
    private String role;

}
