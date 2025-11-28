package yuhan.pro.chatserver.domain.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import yuhan.pro.chatserver.domain.dto.ChatPageResponse;
import yuhan.pro.chatserver.domain.dto.ChatRequest;
import yuhan.pro.chatserver.domain.mapper.ChatMapper;
import yuhan.pro.chatserver.domain.service.ChatService;
import yuhan.pro.chatserver.sharedkernel.infra.redis.RedisPubSubService;
import yuhan.pro.chatserver.sharedkernel.jwt.ChatMemberDetails;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 관련 API")
public class ChatController {

    private final ChatService chatService;
    private final RedisPubSubService redisPubSubService;

    @MessageMapping("/chat.{roomId}")
    public void sendChat(
            ChatRequest incoming,
            @DestinationVariable("roomId") Long roomId,
            Principal principal
    ) {
        ChatMemberDetails userDetails = getUserDetails(
                (UsernamePasswordAuthenticationToken) principal);

        ChatRequest enriched = ChatMapper.enrichChatRequest(
                incoming,
                userDetails.getMemberId(),
                userDetails.getNickName()
        );

        redisPubSubService.publishChatMessage(roomId, enriched);

        chatService.saveChatAsync(enriched, roomId);
    }

    @Operation(
            summary = "채팅 히스토리 조회 (커서 페이징)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "채팅 히스토리 조회 성공"),
                    @ApiResponse(responseCode = "404", description = "해당 채팅방이 없습니다.")
            }
    )
    @GetMapping("/api/chat/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    public ChatPageResponse getChats(
            @PathVariable Long roomId,
            @RequestParam(required = false) LocalDateTime cursor,
            @RequestParam(defaultValue = "6") int size
    ) {
        return chatService.getChatsByCursor(roomId, cursor, size);
    }

    private static ChatMemberDetails getUserDetails(
            UsernamePasswordAuthenticationToken principal) {
        return (ChatMemberDetails) principal.getPrincipal();
    }
}
