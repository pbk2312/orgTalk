package yuhan.pro.chatserver.domain.controller;

import java.security.Principal;
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

  @MessageMapping("/chat/{roomId}")
  @SendTo("/topic/room/{roomId}")
  public ChatResponse sendChat(
      ChatRequest chatRequest,
      @DestinationVariable("roomId") Long roomId,
      Principal principal
  ) {
    return chatService.saveChat(chatRequest, roomId, principal);
  }
}
