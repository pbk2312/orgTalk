package yuhan.pro.mainserver.domain.member.dto;

import java.util.Set;

public record ChatMembersRequest(
    Set<Long> memberIds
) {

}
