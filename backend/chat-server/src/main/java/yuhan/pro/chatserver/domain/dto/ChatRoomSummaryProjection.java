package yuhan.pro.chatserver.domain.dto;

import java.time.LocalDateTime;

public interface ChatRoomSummaryProjection {

  Long getId();

  String getName();

  String getDescription();

  String getType();

  LocalDateTime getCreatedAt();
}
