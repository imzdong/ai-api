package org.imzdong.ai.model.res;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ChatResponse {

    private String id;
    private String name;
    private String model;
    private String userId;
    private String userName;
    private String botUserId;
    private String botName;
    private Date createdDate;

    private List<ChatMessagesResponse> messages;

}
