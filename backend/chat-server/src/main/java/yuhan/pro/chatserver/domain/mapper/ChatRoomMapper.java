package yuhan.pro.chatserver.domain.mapper;

import java.util.Set;
import yuhan.pro.chatserver.domain.dto.ChatMemberResponse;
import yuhan.pro.chatserver.domain.dto.ChatRoomCreateRequest;
import yuhan.pro.chatserver.domain.dto.ChatRoomCreateResponse;
import yuhan.pro.chatserver.domain.dto.ChatRoomInfoResponse;
import yuhan.pro.chatserver.domain.dto.ChatRoomResponse;
import yuhan.pro.chatserver.domain.dto.ChatRoomSummary;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.entity.ChatRoomMember;

public class ChatRoomMapper {

    private ChatRoomMapper() {
        // Utility class
    }

    public static ChatRoom fromChatRoomCreateRequest(
            ChatRoomCreateRequest request,
            Long ownerId,
            String encodedPassword
    ) {
        return ChatRoom.builder()
                .ownerId(ownerId)
                .name(request.name())
                .password(encodedPassword)
                .description(request.description())
                .type(request.type())
                .build();
    }

    public static ChatRoomMember fromMemberId(Long memberId, ChatRoom chatRoom) {
        return ChatRoomMember.builder()
                .chatRoom(chatRoom)
                .memberId(memberId)
                .build();
    }

    public static ChatRoomCreateResponse toChatRoomCreateResponse(ChatRoom chatRoom) {
        return new ChatRoomCreateResponse(chatRoom == null ? null : chatRoom.getId());
    }

    public static ChatRoomResponse toChatRoomResponse(
            ChatRoomSummary summary,
            Long memberCount,
            boolean joined,
            Long unreadCount,
            ChatRoomResponse.LatestMessage latestMessage
    ) {
        return new ChatRoomResponse(
                summary.id(),
                summary.name(),
                summary.description(),
                summary.type(),
                memberCount,
                joined,
                unreadCount != null ? unreadCount : 0L,
                summary.createdAt(),
                latestMessage
        );
    }

    public static ChatRoomInfoResponse toChatRoomInfoResponse(
            ChatRoom chatRoom,
            Set<ChatMemberResponse> members
    ) {
        return new ChatRoomInfoResponse(
                chatRoom.getName(),
                chatRoom.getDescription(),
                chatRoom.getType(),
                chatRoom.getOwnerId(),
                members
        );
    }
}
