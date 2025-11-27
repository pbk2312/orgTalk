package yuhan.pro.chatserver.sharedkernel.infra.socket;

import static yuhan.pro.chatserver.sharedkernel.infra.socket.ChatConstants.PRESENCE_DOT_PREFIX;
import static yuhan.pro.chatserver.sharedkernel.infra.socket.ChatConstants.PRESENCE_KEY_PREFIX;
import static yuhan.pro.chatserver.sharedkernel.infra.socket.ChatConstants.PRESENCE_SLASH_PREFIX;
import static yuhan.pro.chatserver.sharedkernel.infra.socket.ChatConstants.ROOMS_DOT_PREFIX;
import static yuhan.pro.chatserver.sharedkernel.infra.socket.ChatConstants.ROOMS_SLASH_PREFIX;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import yuhan.pro.chatserver.core.MemberClient;
import yuhan.pro.chatserver.domain.dto.MemberProfileUrlResponse;
import yuhan.pro.chatserver.sharedkernel.infra.redis.RedisPubSubService;
import yuhan.pro.chatserver.sharedkernel.jwt.ChatMemberDetails;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceListener {

    private final Map<String, String> sessionRooms = new ConcurrentHashMap<>();
    private final RedisTemplate<String, String> redisTemplate;
    private final MemberClient memberClient;
    private final ObjectMapper objectMapper;
    private final RedisPubSubService redisPubSubService;

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

        CompletableFuture.runAsync(() -> {
            try {
                updateRedis(roomId, user, true);
                publishPresence(roomId);
                logUserJoined(user.getNickName(), roomId);
            } catch (Exception e) {
                logUpdatePresenceFailed(user.getNickName(), roomId, e);
            }
        });
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

        CompletableFuture.runAsync(() -> {
            try {
                updateRedis(roomId, user, false);
                publishPresence(roomId);
                logUserLeft(user.getNickName(), roomId);
            } catch (Exception e) {
                logUpdatePresenceFailed(user.getNickName(), roomId, e);
            }
        });
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
            try {
                MemberProfileUrlResponse profile = memberClient.getMemberProfileUrl(
                        user.getMemberId());
                String avatarUrl = profile != null && profile.avatarUrl() != null
                        ? profile.avatarUrl()
                        : "";
                String value = user.getNickName() + "|" + avatarUrl;
                redisTemplate.opsForHash().put(key, field, value);
            } catch (Exception e) {
                logMemberProfileFailed(user.getMemberId(), e);
                String value = user.getNickName() + "|";
                redisTemplate.opsForHash().put(key, field, value);
            }
        } else {
            redisTemplate.opsForHash().delete(key, field);
        }
    }

    private void publishPresence(String roomId) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash()
                    .entries(PRESENCE_KEY_PREFIX + roomId);
            String payload = objectMapper.writeValueAsString(entries);

            redisPubSubService.publishPresenceUpdate(Long.parseLong(roomId), payload);

            log.debug("방 {}의 접속자 정보를 전파했습니다: {}", roomId, payload);
        } catch (JsonProcessingException e) {
            logPresenceJsonFail(roomId, e);
        } catch (Exception e) {
            logPresencePublishFail(roomId, e);
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

    // --- 로그 헬퍼 메서드: 중복되는 로그 메시지를 한 곳에 모아 IDE의 "Similar log messages" 경고를 줄입니다.
    private void logUserJoined(String nickName, String roomId) {
        log.info("사용자 {}님이 방 {}에 입장했습니다.", nickName, roomId);
    }

    private void logUserLeft(String nickName, String roomId) {
        log.info("사용자 {}님이 방 {}에서 나갔습니다.", nickName, roomId);
    }

    private void logUpdatePresenceFailed(String nickName, String roomId, Exception e) {
        // 스택트레이스는 과다 로그 방지를 위해 기본적으로 남기지 않고 메시지로만 기록합니다.
        log.error("사용자 {} 방 {}의 접속 상태 업데이트 실패: {}", nickName, roomId, e.getMessage());
    }

    private void logMemberProfileFailed(Long memberId, Exception e) {
        log.error("멤버 {}의 프로필 URL 조회 실패, 기본값 사용: {}", memberId, e.getMessage());
    }

    private void logPresenceJsonFail(String roomId, Exception e) {
        log.error("방 {}의 접속자 정보 JSON 변환 실패: {}", roomId, e.getMessage());
    }

    private void logPresencePublishFail(String roomId, Exception e) {
        log.error("방 {}의 접속자 정보 전파 실패: {}", roomId, e.getMessage());
    }
}
