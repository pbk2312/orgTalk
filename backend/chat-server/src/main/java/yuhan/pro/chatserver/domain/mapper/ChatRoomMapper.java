package yuhan.pro.chatserver.domain.mapper;

import yuhan.pro.chatserver.domain.dto.ChatRoomCreateRequest;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.entity.ChatRoomMember;

public class ChatRoomMapper {

  private ChatRoomMapper() {

  }

  public static ChatRoom fromChatRoomCreateRequest(ChatRoomCreateRequest request, Long ownerId) {
    return ChatRoom
        .builder()
        .ownerId(ownerId)
        .organizationId(request.organizationId())
        .name(request.name())
        .description(request.description())
        .type(request.type())
        .build();
  }

  public static ChatRoomMember fromMemberId(Long memberId, ChatRoom chatRoom) {
    return ChatRoomMember
        .builder()
        .chatRoom(chatRoom)
        .memberId(memberId)
        .build();
  }
}
