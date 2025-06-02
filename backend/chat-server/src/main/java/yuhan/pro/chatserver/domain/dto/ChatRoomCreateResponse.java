package yuhan.pro.chatserver.domain.dto;

import lombok.Builder;

@Builder
public record ChatRoomCreateResponse(
    Long id
) {

}
