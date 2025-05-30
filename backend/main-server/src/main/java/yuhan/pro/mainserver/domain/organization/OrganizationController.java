package yuhan.pro.mainserver.domain.organization;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import yuhan.pro.mainserver.domain.organization.dto.OrganizationsInfoResponse;
import yuhan.pro.mainserver.domain.organization.service.OrganizationService;

@RestController
@RequestMapping("/api/organization")
@RequiredArgsConstructor
public class OrganizationController {

  private final OrganizationService organizationService;

  @GetMapping("/{organizationId}")
  @ResponseStatus(HttpStatus.OK)
  public OrganizationsInfoResponse getOrganizationInfo(
      @PathVariable("organizationId")
      Long organizationId) {
    return organizationService.getOrganizationInfo(organizationId);
  }
}
