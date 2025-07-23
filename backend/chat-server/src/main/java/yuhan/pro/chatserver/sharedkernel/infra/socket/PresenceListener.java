package yuhan.pro.chatserver.sharedkernel.infra.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import yuhan.pro.chatserver.core.MemberClient;
import yuhan.pro.chatserver.domain.dto.MemberProfileUrlResponse;
import yuhan.pro.chatserver.sharedkernel.jwt.ChatMemberDetails;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceListener {

    private static final String STOMP_TOPIC_PREFIX = "/topic/";
    private static final String PRESENCE_DOT_PREFIX = STOMP_TOPIC_PREFIX + "presence.";
    private static final String ROOMS_DOT_PREFIX = STOMP_TOPIC_PREFIX + "rooms.";
    private static final String PRESENCE_SLASH_PREFIX = STOMP_TOPIC_PREFIX + "presence/";
    private static final String ROOMS_SLASH_PREFIX = STOMP_TOPIC_PREFIX + "rooms/";
    private static final String PRESENCE_SEND_PREFIX = STOMP_TOPIC_PREFIX + "presence.";
    private static final String PRESENCE_KEY_PREFIX = "chatroom:presence:";

    private final Map<String, String> sessionRooms = new ConcurrentHashMap<>();
    private final RedisTemplate<String, String> redisTemplate;
    private final MemberClient memberClient;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        String destination = sha.getDestination();
        String roomId = extractRoomId(destination);
        if (roomId == null) {
            return;
        }

        sessionRooms.put(sessionId, roomId);
        ChatMemberDetails user = resolveUser(sha, sessionId);
        if (user == null) {
            return;
        }

        updateRedis(roomId, user, true);
        publishPresence(roomId);
        log.info("사용자 {}님이 방 {}에 입장했습니다.", user.getNickName(), roomId);
    }

    @EventListener
    public void onUnsubscribe(SessionUnsubscribeEvent event) {
        onLeave(StompHeaderAccessor.wrap(event.getMessage()));
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        onLeave(StompHeaderAccessor.wrap(event.getMessage()));
    }

    private void onLeave(StompHeaderAccessor sha) {
        String sessionId = sha.getSessionId();
        String roomId = sessionRooms.remove(sessionId);
        if (roomId == null) {
            return;
        }

        ChatMemberDetails user = resolveUser(sha, sessionId);
        if (user == null) {
            return;
        }

        updateRedis(roomId, user, false);
        publishPresence(roomId);
        log.info("사용자 {}님이 방 {}에서 나갔습니다.", user.getNickName(), roomId);
    }

    private ChatMemberDetails resolveUser(StompHeaderAccessor sha, String sessionId) {
        Authentication auth = (Authentication) sha.getUser();
        if (auth == null || auth.getPrincipal() == null) {
            log.warn("세션 {}에 인증 정보가 없습니다.", sessionId);
            return null;
        }
        Object principal = auth.getPrincipal();
        if (!(principal instanceof ChatMemberDetails)) {
            log.warn("세션 {}의 Principal 타입이 다릅니다: {}", sessionId, principal.getClass());
            return null;
        }
        return (ChatMemberDetails) principal;
    }

    private void updateRedis(String roomId, ChatMemberDetails user, boolean join) {
        String key = PRESENCE_KEY_PREFIX + roomId;
        String field = user.getMemberId().toString();
        if (join) {
            MemberProfileUrlResponse profile = memberClient.getMemberProfileUrl(user.getMemberId());
            String value = user.getNickName() + "|" + profile.avatarUrl();
            redisTemplate.opsForHash().put(key, field, value);
        } else {
            redisTemplate.opsForHash().delete(key, field);
        }
    }

    private void publishPresence(String roomId) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash()
                    .entries(PRESENCE_KEY_PREFIX + roomId);
            String payload = objectMapper.writeValueAsString(entries);

            messagingTemplate.convertAndSend(PRESENCE_SEND_PREFIX + roomId, payload);

            log.debug("방 {}의 접속자 정보를 전파했습니다: {}", roomId, payload);
        } catch (JsonProcessingException e) {
            log.error("Presence JSON 변환 실패 for room {}", roomId, e);
        }
    }

    private String extractRoomId(String destination) {
        if (destination == null) {
            return null;
        }
        if (destination.startsWith(PRESENCE_DOT_PREFIX)) {
            return destination.substring(PRESENCE_DOT_PREFIX.length());
        }
        if (destination.startsWith(ROOMS_DOT_PREFIX)) {
            return destination.substring(ROOMS_DOT_PREFIX.length());
        }
        if (destination.startsWith(PRESENCE_SLASH_PREFIX)) {
            return destination.substring(PRESENCE_SLASH_PREFIX.length());
        }
        if (destination.startsWith(ROOMS_SLASH_PREFIX)) {
            return destination.substring(ROOMS_SLASH_PREFIX.length());
        }
        return null;
    }
}
