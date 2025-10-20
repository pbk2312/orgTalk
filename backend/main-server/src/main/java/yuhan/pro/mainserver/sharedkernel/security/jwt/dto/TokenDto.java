package yuhan.pro.mainserver.sharedkernel.security.jwt.dto;

import lombok.Builder;

@Builder
public record TokenDto(
    String accessToken,
    String refreshToken,
    int accessTokenExpiresIn,
    int refreshTokenExpiresIn
) {

}
