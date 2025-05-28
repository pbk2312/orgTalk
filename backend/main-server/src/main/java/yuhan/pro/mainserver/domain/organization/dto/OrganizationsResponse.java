package yuhan.pro.mainserver.domain.organization.dto;

import lombok.Builder;

@Builder
public record OrganizationsResponse(
    Long id,
    String login,
    String avatarUrl
) {

}
