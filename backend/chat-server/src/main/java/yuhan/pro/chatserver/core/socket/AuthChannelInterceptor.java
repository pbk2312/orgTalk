package yuhan.pro.chatserver.core.socket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import yuhan.pro.chatserver.sharedkernel.jwt.ChatMemberDetails;
import yuhan.pro.chatserver.sharedkernel.jwt.JwtAuthenticationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

  private final JwtAuthenticationService jwtAuthService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
        StompHeaderAccessor.class);

    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      String requestId = java.util.UUID.randomUUID().toString();
      long startTime = System.currentTimeMillis();
      log.info("Request ID: {} - WebSocket CONNECT intercepted", requestId);

      Authentication authentication = jwtAuthService.getAuthenticationFromStompHeader(accessor);

      if (authentication != null) {
        log.info("Request ID: {} - Valid JWT found. User: {}",
            requestId,
            ((ChatMemberDetails) authentication.getPrincipal()).getUsername());
        accessor.setUser(authentication);
      }
      long duration = System.currentTimeMillis() - startTime;
      log.info("Request ID: {} - Completed JWT check in {} ms", requestId, duration);
    }

    return message;
  }
}
