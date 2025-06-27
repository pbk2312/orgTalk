package yuhan.pro.chatserver.domain.dto;

import java.time.LocalDateTime;
import yuhan.pro.chatserver.domain.entity.RoomType;

public record ChatRoomSummary(
    Long id,
    String name,
    String description,
    RoomType type,
    LocalDateTime createdAt
) {

  public static ChatRoomSummary fromProjection(
      yuhan.pro.chatserver.domain.dto.ChatRoomSummaryProjection p) {
    return new ChatRoomSummary(
        p.getId(),
        p.getName(),
        p.getDescription(),
        RoomType.valueOf(p.getType()),
        p.getCreatedAt()
    );
  }
}
