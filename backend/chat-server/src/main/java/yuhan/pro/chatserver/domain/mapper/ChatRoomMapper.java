package yuhan.pro.chatserver.domain.mapper;

import java.util.Set;
import yuhan.pro.chatserver.domain.dto.ChatMemberResponse;
import yuhan.pro.chatserver.domain.dto.ChatRoomCreateRequest;
import yuhan.pro.chatserver.domain.dto.ChatRoomCreateResponse;
import yuhan.pro.chatserver.domain.dto.ChatRoomInfoResponse;
import yuhan.pro.chatserver.domain.dto.ChatRoomResponse;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.entity.ChatRoomMember;

public class ChatRoomMapper {

  private ChatRoomMapper() {

  }

  public static ChatRoom fromChatRoomCreateRequest(ChatRoomCreateRequest request, Long ownerId,
      String encodedPassword) {
    return ChatRoom
        .builder()
        .ownerId(ownerId)
        .organizationId(request.organizationId())
        .name(request.name())
        .password(encodedPassword)
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

  public static ChatRoomCreateResponse toChatRoomCreateResponse(ChatRoom chatRoom) {
    return ChatRoomCreateResponse.builder()
        .id(chatRoom.getId())
        .build();
  }

  public static ChatRoomResponse toChatRoomResponse(ChatRoom chatRoom, Long memberId) {

    boolean joined = false;
    if (memberId != null) {
      for (ChatRoomMember member : chatRoom.getMembers()) {
        if (member.getMemberId().equals(memberId)) {
          joined = true;
          break;
        }
      }
    }

    return ChatRoomResponse.builder()
        .id(chatRoom.getId())
        .name(chatRoom.getName())
        .description(chatRoom.getDescription())
        .type(chatRoom.getType())
        .memberCount((long) chatRoom.getMembers().size())
        .lastMessageAt(chatRoom.getLastMessageAt())
        .joined(joined)
        .build();
  }


  public static ChatRoomInfoResponse toChatRoomInfoResponse(ChatRoom chatRoom,
      Set<ChatMemberResponse> members) {
    return ChatRoomInfoResponse.builder()
        .name(chatRoom.getName())
        .description(chatRoom.getDescription())
        .type(chatRoom.getType())
        .members(members)
        .build();
  }
}
