package yuhan.pro.mainserver.sharedkernel.jwt.dto;

import lombok.Builder;

@Builder
public record TokenDto(
    String accessToken,
    String refreshToken,
    int accessTokenExpiresIn,
    int refreshTokenExpiresIn
) {

}
