package yuhan.pro.chatserver.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import yuhan.pro.chatserver.domain.entity.RoomType;

@Builder
public record ChatRoomCreateRequest(

    Long organizationId,


    @NotBlank(message = "채팅방 이름은 필수입니다.")
    String name,

    String description,

    @NotNull(message = "채팅방 타입은 필수입니다.")
    RoomType type
) {

}
