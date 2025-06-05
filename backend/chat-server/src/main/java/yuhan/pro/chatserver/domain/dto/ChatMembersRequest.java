package yuhan.pro.chatserver.domain.dto;

import java.util.Set;

public record ChatMembersRequest(
    Set<Long> memberIds
) {

}
