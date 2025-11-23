package yuhan.pro.chatserver.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yuhan.pro.chatserver.domain.dto.ChatRoomSummary;
import yuhan.pro.chatserver.domain.dto.ChatRoomSummaryProjection;
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
      where ( :type is null or c.type = :type )
      order by c.createdAt desc, c.updatedAt desc
      """)
  Page<ChatRoomSummary> findChatRoomsByType(
      @Param("type") RoomType type,
      Pageable pageable
  );

  // 1) 키워드 없음 (type 필터만)
  @Query("""
        select new yuhan.pro.chatserver.domain.dto.ChatRoomSummary(
          c.id, c.name, c.description, c.type, c.createdAt
        )
        from ChatRoom c
        where (:type is null or c.type = :type)
        order by c.createdAt desc, c.updatedAt desc
      """)
  Page<ChatRoomSummary> findSummaryByType(
      @Param("type") RoomType type,
      Pageable pageable
  );

  @Query("""
        select new yuhan.pro.chatserver.domain.dto.ChatRoomSummary(
          c.id, c.name, c.description, c.type, c.createdAt
        )
        from ChatRoom c
        where (:type is null or c.type = :type)
          and c.name like concat(:kw, '%')
        order by c.createdAt desc, c.updatedAt desc
      """)
  Page<ChatRoomSummary> findSummaryByTypeAndNamePrefix(
      @Param("type") RoomType type,
      @Param("kw") String keyword,
      Pageable pageable
  );


  // 3) 긴 키워드 (fulltext-search, nativeQuery)
  @Query(
      value = """
            SELECT id, name, description, type, created_at AS createdAt
            FROM chat_room
            WHERE (:type IS NULL OR type = :type)
              AND MATCH(name, description) AGAINST(CONCAT('+', :kw, '*') IN BOOLEAN MODE)
            ORDER BY created_at DESC, updated_at DESC
          """,
      countQuery = """
            SELECT COUNT(*)
            FROM chat_room
            WHERE (:type IS NULL OR type = :type)
              AND MATCH(name, description) AGAINST(CONCAT('+', :kw, '*') IN BOOLEAN MODE)
          """,
      nativeQuery = true
  )
  Page<ChatRoomSummaryProjection> findSummaryByTypeAndFullText(
      @Param("type") String type,
      @Param("kw") String keyword,
      Pageable pageable
  );


  boolean existsByName(String name);
}
