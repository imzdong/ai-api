package org.imzdong.ai.model.res;

import lombok.Data;

import java.util.Date;

@Data
public class ChatMessagesResponse {

    private String id;
    private String chatId;
    private String userName;
    private String userId;
    private String content;
    private Date createdDate;

}
