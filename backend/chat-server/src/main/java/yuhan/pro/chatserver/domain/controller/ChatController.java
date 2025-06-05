package yuhan.pro.chatserver.domain.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import yuhan.pro.chatserver.domain.dto.ChatRequest;
import yuhan.pro.chatserver.domain.dto.ChatResponse;
import yuhan.pro.chatserver.domain.service.ChatService;

@Controller
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 관련 API")
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


  @Operation(summary = "채팅 히스토리 조회")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "채팅 히스토리 조회 성공"),
      @ApiResponse(responseCode = "404", description = "해당 하는 채팅방이 없습니다.")
  })
  @ResponseBody
  @GetMapping("/api/chat/{roomId}")
  @ResponseStatus(HttpStatus.OK)
  public List<ChatResponse> getChats(
      @PathVariable Long roomId
  ) {
    return chatService.getAllChats(roomId);
  }
}
