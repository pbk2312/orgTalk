package yuhan.pro.chatserver.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import yuhan.pro.chatserver.domain.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  Page<ChatRoom> findAllByOrganizationId(Long organizationId, Pageable pageable);

}
