package yuhan.pro.chatserver.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yuhan.pro.chatserver.domain.dto.ChatRoomSummary;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.entity.RoomType;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {


  @Query("""
      select new yuhan.pro.chatserver.domain.dto.ChatRoomSummary(
        c.id,
        c.name,
        c.description,
        c.type,
        c.createdAt
      )
      from ChatRoom c
      where c.organizationId = :orgId
        and ( :type is null or c.type = :type )
      order by c.createdAt desc
      """)
  Page<ChatRoomSummary> findChatRoomsByOrgAndType(
      @Param("orgId") Long organizationId,
      @Param("type") RoomType type,
      Pageable pageable
  );

  @Query("""
      SELECT c
      FROM ChatRoom c
      WHERE c.organizationId = :orgId
        AND (:type IS NULL OR c.type = :type)
        AND (
          LOWER(c.name) LIKE LOWER(CONCAT('%', :kw, '%'))
          OR LOWER(c.description) LIKE LOWER(CONCAT('%', :kw, '%'))
        )
      """
  )
  Page<ChatRoom> searchByOrgAndKeyword(
      @Param("orgId") Long organizationId,
      @Param("type") RoomType type,
      @Param("kw") String keyword,
      Pageable pageable
  );

  boolean existsByOrganizationIdAndName(Long organizationId, String name);
}
