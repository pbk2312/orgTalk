package yuhan.pro.chatserver.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yuhan.pro.chatserver.domain.entity.ChatRoomMember;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

  boolean existsByChatRoom_IdAndMemberId(Long chatRoomId, Long memberId);

  @Query("""
      select m.chatRoom.id, count(m)
      from ChatRoomMember m
      where m.chatRoom.id in :chatRoomIds
      group by m.chatRoom.id
      """)
  List<Object[]> findMemberCountsByChatRoomIds(@Param("chatRoomIds") List<Long> chatRoomIds);

  @Query("""
      select distinct m.chatRoom.id
      from ChatRoomMember m
      where m.chatRoom.id in :chatRoomIds
      and m.memberId = :memberId
      """)
  Set<Long> findJoinedChatRoomIds(
      @Param("chatRoomIds") List<Long> chatRoomIds,
      @Param("memberId") Long memberId);

  Optional<ChatRoomMember> findByChatRoom_IdAndMemberId(Long chatRoomId, Long memberId);
}
