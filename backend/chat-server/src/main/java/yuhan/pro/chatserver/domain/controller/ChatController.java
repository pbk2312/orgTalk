package yuhan.pro.chatserver.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import yuhan.pro.chatserver.domain.dto.ChatRequest;
import yuhan.pro.chatserver.domain.dto.ChatResponse;
import yuhan.pro.chatserver.domain.service.ChatService;

@Controller
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  // 클라이언트가 "/send/chat/{roomId}" 로 메시지를 보냄
  @MessageMapping("/chat/{roomId}")
  @SendTo("/room/{roomId}")
  public ChatResponse sendChat(
      ChatRequest chatRequest,
      @DestinationVariable("roomId") Long roomId
  ) {
    return chatService.saveChat(chatRequest, roomId);
  }
}
