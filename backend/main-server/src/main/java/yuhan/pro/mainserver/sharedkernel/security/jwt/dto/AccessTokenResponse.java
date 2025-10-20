package yuhan.pro.mainserver.sharedkernel.security.jwt.dto;

import lombok.Builder;

@Builder
public record AccessTokenResponse(
    String accessToken,
    int accessTokenExpiresIn
) {

}
