package yuhan.pro.chatserver.domain.mapper;

import yuhan.pro.chatserver.domain.dto.ChatRequest;
import yuhan.pro.chatserver.domain.dto.ChatResponse;
import yuhan.pro.chatserver.domain.entity.Chat;
import yuhan.pro.chatserver.domain.entity.ChatRoom;

public class ChatMapper {

  private ChatMapper() {
    // 인스턴스화 방지
  }

  public static ChatResponse toChatResponse(ChatRoom chatRoom, Chat chat
  ) {

    return ChatResponse.builder()
        .id(chat.getId())
        .roomId(chatRoom.getId())
        .senderName(chat.getSenderName())
        .senderId(chat.getSenderId())
        .message(chat.getMessage())
        .build();
  }

  public static Chat fromRequest(ChatRequest request, ChatRoom room, String userName,
      Long memberId) {
    return Chat.builder()
        .room(room)
        .senderName(userName)
        .senderId(memberId)
        .message(request.message())
        .build();
  }
}
