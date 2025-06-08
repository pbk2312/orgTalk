package yuhan.pro.mainserver.domain.member.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import yuhan.pro.mainserver.domain.member.dto.ChatMemberResponse;
import yuhan.pro.mainserver.domain.member.dto.ChatMembersRequest;
import yuhan.pro.mainserver.domain.member.service.MemberService;
import yuhan.pro.mainserver.domain.organization.dto.OrganizationsResponse;
import yuhan.pro.mainserver.sharedkernel.dto.PageResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Member", description = "멤버 API")
@RequestMapping("/api/member")
public class MemberController {

  private final MemberService memberService;

  @Operation(summary = "조직 리스트 조회")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조직 조회 성공"),
      @ApiResponse(responseCode = "404", description = "회원 조회 실패"),
  })
  @GetMapping("/organizations")
  public PageResponse<OrganizationsResponse> getOrganizations(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size
  ) {
    return memberService.getOrganizations(page, size);
  }


  @Operation(summary = "채팅방에 참여하는 회원들 조회")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "회원들 정보 조회 성공"),
  })
  @PostMapping("/chatMembers")
  @ResponseStatus(HttpStatus.OK)
  public Set<ChatMemberResponse> getChatMembers(@RequestBody ChatMembersRequest request) {
    return memberService.getChatMembers(request);
  }
}
