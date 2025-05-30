package yuhan.pro.mainserver.domain.organization.dto;

import lombok.Builder;

@Builder
public record OrganizationsInfoResponse(
    Long id,
    String login,
    String avatarUrl,
    long memberCount
) {

}
