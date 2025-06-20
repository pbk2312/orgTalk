package yuhan.pro.chatserver.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yuhan.pro.chatserver.domain.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  Page<ChatRoom> findAllByOrganizationId(Long organizationId, Pageable pageable);


  @Query("SELECT c " +
      "FROM ChatRoom c " +
      "WHERE c.organizationId = :orgId " +
      "  AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :kw, '%')) " +
      "       OR LOWER(c.description) LIKE LOWER(CONCAT('%', :kw, '%')))")
  Page<ChatRoom> searchByOrgAndKeyword(
      @Param("orgId") Long organizationId,
      @Param("kw") String keyword,
      Pageable pageable);
}
