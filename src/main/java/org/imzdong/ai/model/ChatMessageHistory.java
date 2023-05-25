package org.imzdong.ai.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

@Data
@Builder
public class ChatMessageHistory {

    @MongoId
    private String id;
    private String chatId;
    private String userName;
    private String userId;
    private String content;
    private Integer num;
    @JsonFormat( pattern ="yyyy-MM-dd HH:mm:ss", timezone ="GMT+8")
    private Date createdDate;


}
