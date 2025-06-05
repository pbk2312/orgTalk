package yuhan.pro.mainserver.domain.member.controller;


import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import yuhan.pro.mainserver.domain.member.dto.ChatMemberResponse;
import yuhan.pro.mainserver.domain.member.dto.ChatMembersRequest;
import yuhan.pro.mainserver.domain.member.service.MemberService;
import yuhan.pro.mainserver.domain.organization.dto.OrganizationsResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

  private final MemberService memberService;

  @GetMapping("/organizations")
  @ResponseStatus(HttpStatus.OK)
  public Set<OrganizationsResponse> getOrganizations() {
    return memberService.getOrganizations();
  }

  @PostMapping("/chatMembers")
  @ResponseStatus(HttpStatus.OK)
  public Set<ChatMemberResponse> getChatMembers(@RequestBody ChatMembersRequest request) {
    log.info("request: {}", request);
    return memberService.getChatMembers(request);
  }
}
