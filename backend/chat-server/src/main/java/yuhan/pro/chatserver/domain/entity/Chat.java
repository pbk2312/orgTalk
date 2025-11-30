package yuhan.pro.chatserver.domain.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import yuhan.pro.chatserver.sharedkernel.entity.BaseDocument;

@Getter
@Document(collection = "chat_messages")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@org.springframework.data.mongodb.core.index.CompoundIndex(
        name = "idx_roomid_createdat",
        def = "{'roomId': 1, 'createdAt': -1}"
)
@org.springframework.data.mongodb.core.index.CompoundIndex(
        name = "idx_roomid_createdat_senderid",
        def = "{'roomId': 1, 'createdAt': 1, 'senderId': 1}"
)
public class Chat extends BaseDocument {

    @Id
    private String id;

    private Long roomId;

    private String senderName;

    private Long senderId;

    private MessageType type;

    private String message;

    private String codeContent;

    private Language language;

    @Builder
    public Chat(Long roomId, String senderName, Long senderId, String message) {
        this.roomId = roomId;
        this.senderName = senderName;
        this.senderId = senderId;
        this.message = message;
    }
}
