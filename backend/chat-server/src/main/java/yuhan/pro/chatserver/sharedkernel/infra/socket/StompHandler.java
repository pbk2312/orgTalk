package yuhan.pro.chatserver.sharedkernel.infra.socket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import yuhan.pro.chatserver.sharedkernel.jwt.JwtAuthenticationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtAuthenticationService jwtAuthService;

    @Override
    public Message<?> preSend(@Nullable Message<?> message, @Nullable MessageChannel channel) {
        if (message == null) {
            return null;
        }

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
                StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            long startTime = System.nanoTime();

            Authentication authentication = jwtAuthService.getAuthenticationFromStompHeader(
                    accessor);

            if (authentication != null) {
                accessor.setUser(authentication);

                long duration = (System.nanoTime() - startTime) / 1_000_000;
                if (duration > 50) {
                    log.warn("CONNECT 처리 지연: duration={}ms", duration);
                } else {
                    log.debug("CONNECT 처리 완료: duration={}ms", duration);
                }
            }
        } else if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            log.debug("SUBSCRIBE: destination={}", accessor.getDestination());
        }

        return message;
    }
}
