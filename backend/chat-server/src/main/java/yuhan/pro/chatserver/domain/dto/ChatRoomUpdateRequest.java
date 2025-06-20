package yuhan.pro.chatserver.domain.dto;

import yuhan.pro.chatserver.domain.entity.RoomType;

public record ChatRoomUpdateRequest(
    String name,
    String description,
    RoomType type,
    String password // PRIVATE 전환 시 필수
) {

}
