package yuhan.pro.chatserver.domain.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ChatResponse(
    Long id,
    Long roomId,
    String senderName,
    Long senderId,
    String message,
    LocalDateTime createdAt
) {

}
