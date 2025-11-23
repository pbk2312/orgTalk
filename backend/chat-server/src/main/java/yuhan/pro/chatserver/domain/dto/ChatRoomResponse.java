package yuhan.pro.chatserver.domain.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import yuhan.pro.chatserver.domain.entity.RoomType;

@Builder
public record ChatRoomResponse(
    Long id,
    String name,
    String description,
    RoomType type,
    Long memberCount,
    boolean joined,
    Long unreadCount,
    LocalDateTime createdAt,
    LatestMessage latestMessage) {

    @Builder
    public record LatestMessage(
        String message,
        String senderName,
        LocalDateTime createdAt
    ) {
    }
}
