package yuhan.pro.chatserver.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yuhan.pro.chatserver.domain.entity.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long> {
  
}
