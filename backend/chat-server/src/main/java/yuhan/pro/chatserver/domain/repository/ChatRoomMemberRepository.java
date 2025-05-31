package yuhan.pro.chatserver.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yuhan.pro.chatserver.domain.entity.ChatRoomMember;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

}
