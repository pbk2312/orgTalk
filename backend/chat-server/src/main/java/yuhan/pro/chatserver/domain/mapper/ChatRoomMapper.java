package yuhan.pro.chatserver.domain.mapper;

import yuhan.pro.chatserver.domain.dto.ChatRoomCreateRequest;
import yuhan.pro.chatserver.domain.dto.ChatRoomResponse;
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

  public static ChatRoomResponse toChatRoomResponse(ChatRoom chatRoom) {
    return ChatRoomResponse.builder()
        .id(chatRoom.getId())
        .name(chatRoom.getName())
        .description(chatRoom.getDescription())
        .type(chatRoom.getType())
        .memberCount((long) chatRoom.getMembers().size())
        .lastMessageAt(chatRoom.getLastMessageAt())
        .lastMessage(chatRoom.getLastMessage())
        .build();
  }
}
