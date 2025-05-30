package yuhan.pro.mainserver.domain.organization.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import yuhan.pro.mainserver.domain.organization.dto.OrganizationsInfoResponse;
import yuhan.pro.mainserver.domain.organization.entity.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {


  @Query("""
      SELECT new yuhan.pro.mainserver.domain.organization.dto.OrganizationsInfoResponse(
          o.id, o.login, o.avatarUrl, COUNT(m)
      )
      FROM Organization o
      LEFT JOIN o.members m
      WHERE o.id = :orgId
      GROUP BY o
      """)
  Optional<OrganizationsInfoResponse> findInfoWithMemberCount(Long orgId);
}
