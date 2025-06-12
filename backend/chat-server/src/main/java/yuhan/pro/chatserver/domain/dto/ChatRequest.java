package yuhan.pro.chatserver.domain.dto;

import java.time.LocalDateTime;
import yuhan.pro.chatserver.domain.entity.Language;
import yuhan.pro.chatserver.domain.entity.MessageType;

public record ChatRequest(
    String message,
    MessageType messageType,
    String codeContent,
    Language language,
    Long senderId,
    String senderName,
    LocalDateTime createdAt
) {

}
