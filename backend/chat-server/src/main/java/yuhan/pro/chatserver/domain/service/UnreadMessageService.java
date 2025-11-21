package yuhan.pro.chatserver.domain.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 읽지 않은 메시지 관리를 담당하는 서비스
 * Redis를 사용하여 사용자별 채팅방별 마지막 읽은 시간을 저장하고 조회합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnreadMessageService {

    private static final String LAST_READ_KEY_PREFIX = "user:lastRead:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 사용자가 특정 채팅방에서 마지막으로 읽은 시간을 저장합니다.
     *
     * @param memberId 사용자 ID
     * @param roomId 채팅방 ID
     * @param lastReadTime 마지막 읽은 시간
     */
    public void updateLastReadTime(Long memberId, Long roomId, LocalDateTime lastReadTime) {
        String key = buildKey(memberId, roomId);
        String value = lastReadTime.format(FORMATTER);
        
        redisTemplate.opsForValue().set(key, value);
        log.debug("마지막 읽은 시간 업데이트: memberId={}, roomId={}, time={}", memberId, roomId, lastReadTime);
    }

    /**
     * 사용자가 특정 채팅방에서 마지막으로 읽은 시간을 조회합니다.
     *
     * @param memberId 사용자 ID
     * @param roomId 채팅방 ID
     * @return 마지막 읽은 시간, 없으면 null
     */
    public LocalDateTime getLastReadTime(Long memberId, Long roomId) {
        String key = buildKey(memberId, roomId);
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            log.debug("마지막 읽은 시간 없음: memberId={}, roomId={}", memberId, roomId);
            return null;
        }

        try {
            String timeString = value.toString();
            LocalDateTime lastReadTime = LocalDateTime.parse(timeString, FORMATTER);
            log.debug("마지막 읽은 시간 조회: memberId={}, roomId={}, time={}", memberId, roomId, lastReadTime);
            return lastReadTime;
        } catch (Exception e) {
            log.error("마지막 읽은 시간 파싱 실패: memberId={}, roomId={}, value={}", 
                memberId, roomId, value, e);
            return null;
        }
    }

    /**
     * 사용자가 채팅방에 처음 입장할 때 현재 시간을 마지막 읽은 시간으로 설정합니다.
     *
     * @param memberId 사용자 ID
     * @param roomId 채팅방 ID
     */
    public void markAsRead(Long memberId, Long roomId) {
        updateLastReadTime(memberId, roomId, LocalDateTime.now());
        log.info("채팅방 읽음 처리: memberId={}, roomId={}", memberId, roomId);
    }

    private String buildKey(Long memberId, Long roomId) {
        return LAST_READ_KEY_PREFIX + memberId + ":room:" + roomId;
    }
}

