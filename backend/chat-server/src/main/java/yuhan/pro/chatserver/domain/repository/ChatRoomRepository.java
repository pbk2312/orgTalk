package yuhan.pro.chatserver.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yuhan.pro.chatserver.domain.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

}
