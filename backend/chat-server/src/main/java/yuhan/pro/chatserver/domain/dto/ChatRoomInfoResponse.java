package yuhan.pro.chatserver.domain.dto;

import java.util.Set;
import lombok.Builder;
import yuhan.pro.chatserver.domain.entity.RoomType;

@Builder
public record ChatRoomInfoResponse(
    String name,
    String description,
    RoomType type,
    Long ownerId,
    Set<ChatMemberResponse> members
) {

}
