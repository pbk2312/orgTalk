package yuhan.pro.chatserver.domain.repository.mongoDB;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import yuhan.pro.chatserver.domain.entity.Chat;

public interface ChatRepository extends MongoRepository<Chat, String> {

    List<Chat> findByRoomIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            Long roomId, LocalDateTime cursor, Pageable pageable);

    List<Chat> findByRoomIdOrderByCreatedAtDesc(
            Long roomId, Pageable pageable);


    long countByRoomIdAndCreatedAtAfterAndSenderIdNot(Long roomId, LocalDateTime after,
            Long senderId);

    void deleteByRoomId(Long roomId);

    Chat findFirstByRoomIdOrderByCreatedAtDesc(Long roomId);
}
