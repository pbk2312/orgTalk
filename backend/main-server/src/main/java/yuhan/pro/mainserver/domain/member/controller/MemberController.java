package yuhan.pro.mainserver.domain.member.controller;


import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import yuhan.pro.mainserver.domain.member.service.MemberService;
import yuhan.pro.mainserver.domain.organization.dto.OrganizationsResponse;

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
}
