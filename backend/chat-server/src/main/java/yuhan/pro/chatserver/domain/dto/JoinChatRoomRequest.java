package yuhan.pro.chatserver.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinChatRoomRequest(

    @NotBlank(message = "PRIVATE 방은 비밀번호가 필수 입니다.")
    String password
) {

}
