package yuhan.pro.chatserver.domain.repository.mongoDB;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import yuhan.pro.chatserver.domain.entity.Chat;

public interface ChatRepository extends MongoRepository<Chat, String> {

  List<Chat> findByRoomIdOrderByCreatedAtAsc(Long roomId);
}
