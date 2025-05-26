package yuhan.pro.mainserver.domain.organization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yuhan.pro.mainserver.domain.organization.entity.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

}
