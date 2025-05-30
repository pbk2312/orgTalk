package yuhan.pro.mainserver.domain.organization.service;


import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.ORGANIZATION_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yuhan.pro.mainserver.domain.organization.dto.OrganizationsInfoResponse;
import yuhan.pro.mainserver.domain.organization.repository.OrganizationRepository;
import yuhan.pro.mainserver.sharedkernel.exception.CustomException;

@Service
@RequiredArgsConstructor
public class OrganizationService {

  private final OrganizationRepository organizationRepository;

  public OrganizationsInfoResponse getOrganizationInfo(Long organizationId) {
    return findOrganizationWithMemberCountOrThrow(organizationId);
  }

  private OrganizationsInfoResponse findOrganizationWithMemberCountOrThrow(Long organizationId) {
    return organizationRepository
        .findInfoWithMemberCount(organizationId)
        .orElseThrow(() -> new CustomException(ORGANIZATION_NOT_FOUND));
  }
}
