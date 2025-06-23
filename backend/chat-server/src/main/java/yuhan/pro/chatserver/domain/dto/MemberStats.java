package yuhan.pro.chatserver.domain.dto;

public record MemberStats(
    Long memberId,
    boolean joined,
    int totalRooms
) {

}
