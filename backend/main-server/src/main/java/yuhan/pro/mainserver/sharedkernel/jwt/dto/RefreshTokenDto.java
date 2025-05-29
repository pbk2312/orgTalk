package yuhan.pro.mainserver.sharedkernel.jwt.dto;

import lombok.Builder;

@Builder
public record RefreshTokenDto(
    String refreshToken,
    int refreshTokenExpiresIn
) {

}
