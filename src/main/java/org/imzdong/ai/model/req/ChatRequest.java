package org.imzdong.ai.model.req;

import lombok.Data;

@Data
public class ChatRequest {

    private String chatName;
    private String userId;
    private String model;

}
