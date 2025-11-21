package yuhan.pro.chatserver.domain.repository.mongoDB;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import yuhan.pro.chatserver.domain.entity.Chat;

public interface ChatRepository extends MongoRepository<Chat, String> {

  List<Chat> findByRoomIdOrderByCreatedAtAsc(Long roomId);

  List<Chat> findByRoomIdAndCreatedAtBeforeOrderByCreatedAtDesc(
      Long roomId, LocalDateTime cursor, Pageable pageable);

  List<Chat> findByRoomIdOrderByCreatedAtDesc(
      Long roomId, Pageable pageable);

  long countByRoomIdAndCreatedAtAfter(Long roomId, LocalDateTime after);

  void deleteByRoomId(Long roomId);

  // 특정 채팅방의 최신 메시지 1개 조회
  Chat findFirstByRoomIdOrderByCreatedAtDesc(Long roomId);
}
