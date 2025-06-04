package yuhan.pro.chatserver.domain.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import yuhan.pro.chatserver.domain.entity.Language;
import yuhan.pro.chatserver.domain.entity.MessageType;

@Builder
public record ChatResponse(
    Long id,
    Long roomId,
    String senderName,
    Long senderId,
    String message,
    LocalDateTime createdAt,
    MessageType messageType,
    String codeContent,
    Language language
) {

}
