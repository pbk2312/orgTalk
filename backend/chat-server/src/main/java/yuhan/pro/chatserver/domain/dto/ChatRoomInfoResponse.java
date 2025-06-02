package yuhan.pro.chatserver.domain.dto;

import lombok.Builder;
import yuhan.pro.chatserver.domain.entity.RoomType;

@Builder
public record ChatRoomInfoResponse(
    String name,
    String description,
    RoomType type,
    Long memberCount
) {

}
