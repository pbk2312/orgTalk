package yuhan.pro.chatserver.sharedkernel.infra.redis;

import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.REDIS_PROCESSING_FAILED;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.REDIS_PUBLISH_FAILED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import yuhan.pro.chatserver.domain.dto.ChatRequest;
import yuhan.pro.chatserver.sharedkernel.exception.CustomException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPubSubService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        MessageListener chatListener = (message, pattern) -> {
            try {
                String channel = new String(message.getChannel());
                String messageBody = new String(message.getBody());

                String roomIdStr = channel.substring(RedisConstants.CHAT_CHANNEL_PREFIX.length());
                long roomId = Long.parseLong(roomIdStr);

                ChatRequest chatRequest = objectMapper.readValue(messageBody, ChatRequest.class);

                String destination = "/topic/chat." + roomId;
                messagingTemplate.convertAndSend(destination, chatRequest);

            } catch (NumberFormatException e) {
                warnInvalidRoomIdFormat();
            } catch (JsonProcessingException e) {
                logParsingFailure();
                throw new CustomException(REDIS_PROCESSING_FAILED, e.getMessage());
            } catch (Exception e) {
                logProcessingFailure(RedisConstants.CTX_CHAT, e);
                throw new CustomException(REDIS_PROCESSING_FAILED, e.getMessage());
            }
        };

        MessageListener presenceListener = (message, pattern) -> {
            try {
                String channel = new String(message.getChannel());
                String presenceData = new String(message.getBody());

                String roomIdStr = channel.substring(
                        RedisConstants.PRESENCE_CHANNEL_PREFIX.length());
                long roomId = Long.parseLong(roomIdStr);

                String destination = "/topic/presence." + roomId;
                messagingTemplate.convertAndSend(destination, presenceData);

            } catch (NumberFormatException e) {
                warnInvalidRoomIdFormat();
            } catch (Exception e) {
                logProcessingFailure(RedisConstants.CTX_PRESENCE, e);
                throw new CustomException(REDIS_PROCESSING_FAILED, e.getMessage());
            }
        };

        redisMessageListenerContainer.addMessageListener(chatListener,
                new PatternTopic(RedisConstants.CHAT_CHANNEL_PATTERN));
        redisMessageListenerContainer.addMessageListener(presenceListener,
                new PatternTopic(RedisConstants.PRESENCE_CHANNEL_PATTERN));

        log.info("Redis Pub/Sub 리스너 초기화 완료");
    }

    public void publishChatMessage(Long roomId, ChatRequest chatRequest) {
        try {
            String channel = RedisConstants.CHAT_CHANNEL_PREFIX + roomId;
            String message = objectMapper.writeValueAsString(chatRequest);
            stringRedisTemplate.convertAndSend(channel, message);
        } catch (Exception e) {
            logPublishFailure(RedisConstants.CTX_CHAT, e);
            throw new CustomException(REDIS_PUBLISH_FAILED, e.getMessage());
        }
    }

    public void publishPresenceUpdate(Long roomId, String presenceData) {
        try {
            String channel = RedisConstants.PRESENCE_CHANNEL_PREFIX + roomId;
            stringRedisTemplate.convertAndSend(channel, presenceData);
        } catch (Exception e) {
            logPublishFailure(RedisConstants.CTX_PRESENCE, e);
            throw new CustomException(REDIS_PUBLISH_FAILED, e.getMessage());
        }
    }

    private void warnInvalidRoomIdFormat() {
        log.warn("잘못된 roomId 형식 (채널)");
    }

    private void logParsingFailure() {
        log.error("메시지 파싱 실패");
    }

    private void logProcessingFailure(String context, Exception e) {
        log.error("{} 처리 중 오류", context, e);
    }

    private void logPublishFailure(String context, Exception e) {
        log.error("{} 발행 실패", context, e);
    }
}
