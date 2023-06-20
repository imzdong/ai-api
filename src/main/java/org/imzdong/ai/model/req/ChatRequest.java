package org.imzdong.ai.model.req;

import lombok.Data;

@Data
public class ChatRequest {

    private String chatRoomName;
    private String chatRoomId;
    private String userId;
    private String model;

}
