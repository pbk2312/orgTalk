package yuhan.pro.mainserver.domain.organization.dto;

import lombok.Builder;

@Builder
public record CreateOrganizationResponse(
        Long id,
        String login,
        String avatarUrl,
        String accessToken,
        String refreshToken,
        Integer accessTokenExpiresIn,
        Integer refreshTokenExpiresIn
) {

}


