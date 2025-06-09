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
        .codeContent(chat.getCodeContent())
        .messageType(chat.getType())
        .createdAt(chat.getCreatedAt())
        .language(chat.getLanguage())
        .build();
  }

  public static Chat fromRequest(ChatRequest req, Long roomId) {
    return Chat.builder()
        .roomId(roomId)
        .senderName(req.senderName())
        .senderId(req.senderId())
        .message(req.message())
        .type(req.messageType())
        .codeContent(req.codeContent())
        .language(req.language())
        .build();
  }
}
