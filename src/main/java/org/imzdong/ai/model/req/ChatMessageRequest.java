package org.imzdong.ai.model.req;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageRequest {

    private String message;
    private String userId;
    private String chatId;
    private String userName;
    private Integer num;
    private String model;

}
