package yuhan.pro.chatserver.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ChatPageResponse(
    List<ChatResponse> chats,
    LocalDateTime nextCursor
) {

}
