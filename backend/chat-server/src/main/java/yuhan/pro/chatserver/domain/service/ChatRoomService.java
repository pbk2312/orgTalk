package yuhan.pro.chatserver.domain.service;


import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.CHAT_ROOM_MEMBER_NOT_FOUND;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.CHAT_ROOM_NAME_DUPLICATE;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.MEMBER_NOT_ACCEPTED;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.MEMBER_NOT_FOUND;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.OWNER_CANNOT_BE_KICKED;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.PRIVATE_ROOM_PASSWORD_IS_EMPTY;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.PRIVATE_ROOM_PASSWORD_NOT_MATCH;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.ROOM_OWNER_MISMATCH;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yuhan.pro.chatserver.core.MemberClient;
import yuhan.pro.chatserver.domain.dto.ChatMemberResponse;
import yuhan.pro.chatserver.domain.dto.ChatRoomCreateRequest;
import yuhan.pro.chatserver.domain.dto.ChatRoomCreateResponse;
import yuhan.pro.chatserver.domain.dto.ChatRoomInfoResponse;
import yuhan.pro.chatserver.domain.dto.ChatRoomResponse;
import yuhan.pro.chatserver.domain.dto.ChatRoomSummary;
import yuhan.pro.chatserver.domain.dto.ChatRoomSummaryProjection;
import yuhan.pro.chatserver.domain.dto.ChatRoomUpdateRequest;
import yuhan.pro.chatserver.domain.dto.JoinChatRoomRequest;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.entity.ChatRoomMember;
import yuhan.pro.chatserver.domain.entity.RoomType;
import yuhan.pro.chatserver.domain.mapper.ChatRoomMapper;
import yuhan.pro.chatserver.domain.repository.ChatRoomMemberRepository;
import yuhan.pro.chatserver.domain.repository.ChatRoomRepository;
import yuhan.pro.chatserver.domain.repository.mongoDB.ChatRepository;
import yuhan.pro.chatserver.sharedkernel.dto.PageResponse;
import yuhan.pro.chatserver.sharedkernel.exception.CustomException;
import yuhan.pro.chatserver.sharedkernel.jwt.ChatMemberDetails;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberClient memberClient;
    private final ChatRepository chatRepository;
    private final UnreadMessageService unreadMessageService;

    @Transactional
    public ChatRoomCreateResponse saveChatRoom(ChatRoomCreateRequest request) {

        Authentication authentication = getAuthentication();

        Long memberId = getMemberId(authentication);

        valiadteChatRoomNameDuplicated(request);

        RoomType type = request.type();

        String encodedPassword = null;
        if (requestRoomTypeIsPrivate(type)) {
            String rawPassword = request.password();

            rawPasswordIsEmpty(rawPassword);
            encodedPassword = passwordEncoder.encode(rawPassword);
        }

        ChatRoom chatRoom = ChatRoomMapper.fromChatRoomCreateRequest(request, memberId,
                encodedPassword);

        chatRoomRepository.save(chatRoom);

        ChatRoomMember chatRoomMember = ChatRoomMapper.fromMemberId(memberId, chatRoom);
        chatRoomMemberRepository.save(chatRoomMember);
        return ChatRoomMapper.toChatRoomCreateResponse(chatRoom);
    }

    @Transactional
    public void joinChatRoom(Long roomId, JoinChatRoomRequest request) {
        String password = request.password();
        ChatRoom chatRoom = findChatRoomOrThrow(roomId);

        if (chatRoom.isPrivate() && !chatRoom.matchesPassword(password, passwordEncoder)) {
            throw new CustomException(PRIVATE_ROOM_PASSWORD_NOT_MATCH);
        }

        Authentication authentication = getAuthentication();

        Long memberId = getMemberId(authentication);
        ChatRoomMember chatRoomMember = ChatRoomMapper.fromMemberId(memberId, chatRoom);
        chatRoomMemberRepository.save(chatRoomMember);

        // 채팅방 입장 시 읽은 시간 업데이트
        unreadMessageService.markAsRead(memberId, roomId);
    }


    @Transactional(readOnly = true)
    public PageResponse<ChatRoomResponse> getChatRooms(RoomType type,
            Pageable pageable) {

        Authentication authentication = getAuthentication();

        Long memberId = getMemberId(authentication);

        Page<ChatRoomSummary> summaryPage = fetchSummaries(type, pageable);
        if (summaryPage.isEmpty()) {
            return PageResponse.fromPage(Page.empty(pageable));
        }

        List<Long> chatRoomIds = extractRoomIds(summaryPage);
        Map<Long, Long> memberCounts = fetchMemberCounts(chatRoomIds);
        Set<Long> joinedRoomIds = fetchJoinedRoomIds(chatRoomIds, memberId);
        Map<Long, Long> unreadCounts = fetchUnreadCounts(chatRoomIds, memberId);
        Map<Long, ChatRoomResponse.LatestMessage> latestMessages = fetchLatestMessages(chatRoomIds);

        Page<ChatRoomResponse> responsePage = mapToResponsePage(summaryPage, memberCounts,
                joinedRoomIds, unreadCounts, latestMessages, pageable);
        return PageResponse.fromPage(responsePage);
    }


    @Transactional(readOnly = true)
    public PageResponse<ChatRoomResponse> searchChatRooms(
            RoomType type,
            String keyword,
            Pageable pageable
    ) {
        Long memberId = getMemberId(getAuthentication());

        Page<ChatRoomSummary> summaryPage = fetchSummaryPage(type, keyword,
                pageable);

        if (summaryPage.isEmpty()) {
            return PageResponse.fromPage(Page.empty(pageable));
        }

        List<Long> roomIds = summaryPage.stream()
                .map(ChatRoomSummary::id)
                .toList();
        Map<Long, Long> memberCounts = fetchMemberCounts(roomIds);
        Set<Long> joinedRoomIds = fetchJoinedRoomIds(roomIds, memberId);
        Map<Long, Long> unreadCounts = fetchUnreadCounts(roomIds, memberId);
        Map<Long, ChatRoomResponse.LatestMessage> latestMessages = fetchLatestMessages(roomIds);

        Page<ChatRoomResponse> responsePage = mapToResponsePage(
                summaryPage,
                memberCounts,
                joinedRoomIds,
                unreadCounts,
                latestMessages,
                pageable
        );
        return PageResponse.fromPage(responsePage);
    }


    @Transactional(readOnly = true)
    public ChatRoomInfoResponse getChatRoomInfo(Long roomId, String jwtToken) {
        ChatRoom chatRoom = findChatRoomOrThrow(roomId);

        Set<Long> memberIds = chatRoom.getMembers().stream()
                .map(ChatRoomMember::getMemberId)
                .collect(Collectors.toSet());

        Authentication auth = getAuthentication();
        Long memberId = getMemberId(auth);

        validateMemberRoomIn(chatRoom, memberId);

        // 채팅방 정보 조회 시 읽은 시간 업데이트
        unreadMessageService.markAsRead(memberId, roomId);

        Set<ChatMemberResponse> chatMembers = memberClient.getChatMembers(memberIds, jwtToken);

        return ChatRoomMapper.toChatRoomInfoResponse(chatRoom, chatMembers);
    }

    @Transactional
    public void deleteChatRoom(Long roomId) {
        Authentication authentication = getAuthentication();

        Long memberId = getMemberId(authentication);

        ChatRoom chatRoom = findChatRoomOrThrow(roomId);
        validateOwner(memberId, chatRoom);
        chatRepository.deleteByRoomId(roomId);
        chatRoomRepository.delete(chatRoom);
    }

    @Transactional
    public void updateChatRoom(Long roomId, ChatRoomUpdateRequest req) {
        Authentication auth = getAuthentication();
        Long memberId = getMemberId(auth);

        ChatRoom room = findChatRoomOrThrow(roomId);
        validateOwner(memberId, room);

        room.updateRoom(
                req.name(),
                req.description(),
                req.type(),
                req.password(),
                passwordEncoder
        );
    }


    @Transactional
    public void kickOutMember(Long roomId, Long kickedMemberId) {
        Authentication auth = getAuthentication();
        Long memberId = getMemberId(auth);
        ChatRoom room = findChatRoomOrThrow(roomId);
        validateOwner(memberId, room);

        validateKickOwnerId(kickedMemberId, memberId);

        ChatRoomMember member = findChatMemberAndThrows(roomId,
                kickedMemberId);

        chatRoomMemberRepository.delete(member);
    }

    private Page<ChatRoomSummary> fetchSummaryPage(
            RoomType type,
            String keyword,
            Pageable pageable
    ) {
        if (keyword == null || keyword.isBlank()) {
            return chatRoomRepository.findSummaryByType(type, pageable);
        }

        if (keyword.length() <= 2) {
            return chatRoomRepository.findSummaryByTypeAndNamePrefix(
                    type, keyword, pageable
            );
        }

        Page<ChatRoomSummaryProjection> projPage =
                chatRoomRepository.findSummaryByTypeAndFullText(
                        type != null ? type.name() : null,
                        keyword,
                        pageable
                );

        return projPage.map(ChatRoomSummary::fromProjection);
    }

    private ChatRoomMember findChatMemberAndThrows(Long roomId, Long kickedMemberId) {
        return chatRoomMemberRepository
                .findByChatRoom_IdAndMemberId(roomId, kickedMemberId)
                .orElseThrow(() -> new CustomException(CHAT_ROOM_MEMBER_NOT_FOUND));
    }

    private static void validateKickOwnerId(Long kickedMemberId, Long memberId) {
        if (memberId.equals(kickedMemberId)) {
            throw new CustomException(OWNER_CANNOT_BE_KICKED);
        }
    }


    private void validateOwner(Long memberId, ChatRoom chatRoom) {
        if (!chatRoom.getOwnerId().equals(memberId)) {
            throw new CustomException(ROOM_OWNER_MISMATCH);
        }
    }

    private void validateMemberRoomIn(ChatRoom chatRoom, Long memberId) {
        boolean inRoom = chatRoom.getMembers().stream()
                .map(ChatRoomMember::getMemberId)
                .anyMatch(id -> id.equals(memberId));
        if (!inRoom) {
            throw new CustomException(MEMBER_NOT_ACCEPTED);
        }
    }

    private ChatRoom findChatRoomOrThrow(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
    }

    private void rawPasswordIsEmpty(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new CustomException(PRIVATE_ROOM_PASSWORD_IS_EMPTY);
        }
    }

    private boolean requestRoomTypeIsPrivate(RoomType type) {
        return type == RoomType.PRIVATE;
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private Long getMemberId(Authentication authentication) {
        if (authentication != null
                && authentication.getPrincipal() instanceof ChatMemberDetails chatMemberDetails) {
            return chatMemberDetails.getMemberId();
        }
        return null;
    }

    private Page<ChatRoomSummary> fetchSummaries(RoomType type, Pageable pageable) {
        return chatRoomRepository.findChatRoomsByType(type, pageable);
    }

    private List<Long> extractRoomIds(Page<ChatRoomSummary> summaryPage) {
        return summaryPage.getContent().stream()
                .map(ChatRoomSummary::id)
                .toList();
    }

    private Map<Long, Long> fetchMemberCounts(List<Long> roomIds) {
        return chatRoomMemberRepository.findMemberCountsByChatRoomIds(roomIds).stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1],
                        (existing, replacement) -> existing
                ));
    }

    private Set<Long> fetchJoinedRoomIds(List<Long> roomIds, Long memberId) {
        return chatRoomMemberRepository.findJoinedChatRoomIds(roomIds, memberId);
    }

    private Map<Long, Long> fetchUnreadCounts(List<Long> roomIds, Long memberId) {
        return roomIds.stream()
                .collect(Collectors.toMap(
                        roomId -> roomId,
                        roomId -> calculateUnreadCount(roomId, memberId),
                        (existing, replacement) -> existing
                ));
    }

    private Long calculateUnreadCount(Long roomId, Long memberId) {
        // 참여하지 않은 채팅방은 읽지 않은 메시지 수를 0으로 반환
        if (!chatRoomMemberRepository.existsByChatRoom_IdAndMemberId(roomId, memberId)) {
            return 0L;
        }

        LocalDateTime lastReadTime = unreadMessageService.getLastReadTime(memberId, roomId);
        
        // 마지막 읽은 시간이 없으면 모든 메시지를 읽지 않은 것으로 간주하지 않음 (0 반환)
        // 또는 채팅방 생성 시간 이후의 메시지만 카운트할 수도 있음
        if (lastReadTime == null) {
            return 0L;
        }

        long unreadCount = chatRepository.countByRoomIdAndCreatedAtAfter(roomId, lastReadTime);
        return unreadCount;
    }

    private Map<Long, ChatRoomResponse.LatestMessage> fetchLatestMessages(List<Long> roomIds) {
        return roomIds.stream()
                .map(roomId -> {
                    var latestChat = chatRepository.findFirstByRoomIdOrderByCreatedAtDesc(roomId);
                    if (latestChat == null) {
                        return null;
                    }
                    return new AbstractMap.SimpleEntry<>(
                            roomId,
                            new ChatRoomResponse.LatestMessage(
                                    latestChat.getMessage(),
                                    latestChat.getSenderName(),
                                    latestChat.getCreatedAt()
                            )
                    );
                })
                .filter(entry -> entry != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private Page<ChatRoomResponse> mapToResponsePage(
            Page<ChatRoomSummary> summaryPage,
            Map<Long, Long> memberCounts,
            Set<Long> joinedRoomIds,
            Map<Long, Long> unreadCounts,
            Map<Long, ChatRoomResponse.LatestMessage> latestMessages,
            Pageable pageable
    ) {
        List<ChatRoomResponse> responses = summaryPage.getContent().stream()
                .map(summary -> {
                    boolean joined = joinedRoomIds.contains(summary.id());
                    // 참여하지 않은 채팅방에서는 최신 메시지를 보여주지 않음
                    ChatRoomResponse.LatestMessage latestMessage = joined 
                            ? latestMessages.get(summary.id()) 
                            : null;
                    return ChatRoomMapper.toChatRoomResponse(
                            summary,
                            memberCounts.getOrDefault(summary.id(), 0L),
                            joined,
                            unreadCounts.getOrDefault(summary.id(), 0L),
                            latestMessage
                    );
                })
                .toList();

        return new PageImpl<>(
                responses,
                pageable,
                summaryPage.getTotalElements()
        );
    }

    private void valiadteChatRoomNameDuplicated(ChatRoomCreateRequest request) {
        if (chatRoomRepository.existsByName(request.name())) {
            throw new CustomException(CHAT_ROOM_NAME_DUPLICATE);
        }
    }
}
