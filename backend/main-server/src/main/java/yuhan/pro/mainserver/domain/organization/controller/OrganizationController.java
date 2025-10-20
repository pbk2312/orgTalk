package yuhan.pro.mainserver.domain.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import yuhan.pro.mainserver.domain.organization.dto.CreateOrganizationRequest;
import yuhan.pro.mainserver.domain.organization.dto.CreateOrganizationResponse;
import yuhan.pro.mainserver.domain.organization.dto.OrganizationsInfoResponse;
import yuhan.pro.mainserver.domain.organization.service.OrganizationService;

@RestController
@RequestMapping("/api/organization")
@RequiredArgsConstructor
@Tag(name = "Organization", description = "조직 API")
public class OrganizationController {

  private final OrganizationService organizationService;

  @GetMapping("/{organizationId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "조직 정보 조회")
  @ApiResponse(responseCode = "200", description = "조직 정보 조회 성공")
  @ApiResponse(responseCode = "404", description = "조직을 찾을 수 없음")
  public OrganizationsInfoResponse getOrganizationInfo(
      @PathVariable("organizationId")
      Long organizationId) {
    return organizationService.getOrganizationInfo(organizationId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "조직 생성", description = "테스트용 조직을 생성합니다. 생성한 사용자가 자동으로 조직에 추가되고, 업데이트된 JWT 토큰을 반환합니다.")
  @ApiResponse(responseCode = "201", description = "조직 생성 성공")
  @ApiResponse(responseCode = "400", description = "잘못된 요청")
  @ApiResponse(responseCode = "401", description = "인증 실패")
  public CreateOrganizationResponse createOrganization(
      @Valid @RequestBody CreateOrganizationRequest request) {
    return organizationService.createOrganization(request);
  }
}
