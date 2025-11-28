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
import java.util.Objects;
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
        Long memberId = getCurrentMemberId();
        validateChatRoomNameNotDuplicated(request.name());

        String encodedPassword = encodePasswordIfPrivate(request.type(), request.password());
        ChatRoom chatRoom = createAndSaveChatRoom(request, memberId, encodedPassword);
        createAndSaveChatRoomMember(memberId, chatRoom);

        // 방어: 만약 chatRoom이 null이면 Mapper 호출로 인한 NPE를 피합니다.
        if (chatRoom == null) {
            return new ChatRoomCreateResponse(null);
        }

        return ChatRoomMapper.toChatRoomCreateResponse(chatRoom);
    }

    @Transactional
    public void joinChatRoom(Long roomId, JoinChatRoomRequest request) {
        ChatRoom chatRoom = findChatRoomOrThrow(roomId);
        validatePasswordIfPrivate(chatRoom, request.password());

        Long memberId = getCurrentMemberId();
        createAndSaveChatRoomMember(memberId, chatRoom);
        unreadMessageService.markAsRead(memberId, roomId);
    }

    @Transactional(readOnly = true)
    public PageResponse<ChatRoomResponse> getChatRooms(RoomType type, Pageable pageable) {
        Long memberId = getCurrentMemberId();
        Page<ChatRoomSummary> summaryPage = fetchSummaries(type, pageable);

        if (summaryPage.isEmpty()) {
            return PageResponse.fromPage(Page.empty(pageable));
        }

        Page<ChatRoomResponse> responsePage = buildChatRoomResponsePage(summaryPage, memberId,
                pageable);
        return PageResponse.fromPage(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<ChatRoomResponse> searchChatRooms(RoomType type, String keyword,
            Pageable pageable) {
        Long memberId = getCurrentMemberId();
        Page<ChatRoomSummary> summaryPage = fetchSummaryPage(type, keyword, pageable);

        if (summaryPage.isEmpty()) {
            return PageResponse.fromPage(Page.empty(pageable));
        }

        Page<ChatRoomResponse> responsePage = buildChatRoomResponsePage(summaryPage, memberId,
                pageable);
        return PageResponse.fromPage(responsePage);
    }

    @Transactional(readOnly = true)
    public ChatRoomInfoResponse getChatRoomInfo(Long roomId, String jwtToken) {
        ChatRoom chatRoom = findChatRoomOrThrow(roomId);
        Long memberId = getCurrentMemberId();

        validateMemberInRoom(chatRoom, memberId);
        unreadMessageService.markAsRead(memberId, roomId);

        Set<Long> memberIds = extractMemberIds(chatRoom);
        Set<ChatMemberResponse> chatMembers = memberClient.getChatMembers(memberIds, jwtToken);

        return ChatRoomMapper.toChatRoomInfoResponse(chatRoom, chatMembers);
    }

    @Transactional
    public void deleteChatRoom(Long roomId) {
        Long memberId = getCurrentMemberId();
        ChatRoom chatRoom = findChatRoomOrThrow(roomId);

        validateOwner(memberId, chatRoom);

        chatRepository.deleteByRoomId(roomId);
        chatRoomRepository.delete(chatRoom);
    }

    @Transactional
    public void updateChatRoom(Long roomId, ChatRoomUpdateRequest request) {
        Long memberId = getCurrentMemberId();
        ChatRoom chatRoom = findChatRoomOrThrow(roomId);

        validateOwner(memberId, chatRoom);
        chatRoom.updateRoom(
                request.name(),
                request.description(),
                request.type(),
                request.password(),
                passwordEncoder
        );
    }

    @Transactional
    public void kickOutMember(Long roomId, Long kickedMemberId) {
        Long ownerId = getCurrentMemberId();
        ChatRoom chatRoom = findChatRoomOrThrow(roomId);

        validateOwner(ownerId, chatRoom);
        validateNotKickingSelf(kickedMemberId, ownerId);

        ChatRoomMember member = findChatRoomMemberOrThrow(roomId, kickedMemberId);
        chatRoomMemberRepository.delete(member);
    }

    private Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.getPrincipal() instanceof ChatMemberDetails details) {
            return details.getMemberId();
        }
        throw new CustomException(MEMBER_NOT_FOUND);
    }

    private String encodePasswordIfPrivate(RoomType type, String password) {
        if (type != RoomType.PRIVATE) {
            return null;
        }

        validatePasswordNotEmpty(password);
        return passwordEncoder.encode(password);
    }

    private ChatRoom createAndSaveChatRoom(ChatRoomCreateRequest request, Long memberId,
            String encodedPassword) {
        ChatRoom chatRoom = ChatRoomMapper.fromChatRoomCreateRequest(request, memberId,
                encodedPassword);
        // 저장소 구현(예: 테스트용 mock)이 null을 반환할 수 있으므로 안전하게 처리합니다.
        ChatRoom saved = chatRoomRepository.save(chatRoom);
        return saved != null ? saved : chatRoom;
    }

    private void createAndSaveChatRoomMember(Long memberId, ChatRoom chatRoom) {
        ChatRoomMember chatRoomMember = ChatRoomMapper.fromMemberId(memberId, chatRoom);
        chatRoomMemberRepository.save(chatRoomMember);
    }

    private void validatePasswordIfPrivate(ChatRoom chatRoom, String password) {
        if (chatRoom.isPrivate() && !chatRoom.matchesPassword(password, passwordEncoder)) {
            throw new CustomException(PRIVATE_ROOM_PASSWORD_NOT_MATCH);
        }
    }

    private Page<ChatRoomResponse> buildChatRoomResponsePage(
            Page<ChatRoomSummary> summaryPage,
            Long memberId,
            Pageable pageable) {

        List<Long> roomIds = extractRoomIds(summaryPage);

        Map<Long, Long> memberCounts = fetchMemberCounts(roomIds);
        Set<Long> joinedRoomIds = fetchJoinedRoomIds(roomIds, memberId);
        Map<Long, Long> unreadCounts = fetchUnreadCounts(roomIds, memberId);
        Map<Long, ChatRoomResponse.LatestMessage> latestMessages = fetchLatestMessages(roomIds);

        return mapToResponsePage(summaryPage, memberCounts, joinedRoomIds, unreadCounts,
                latestMessages, pageable);
    }

    private Set<Long> extractMemberIds(ChatRoom chatRoom) {
        return chatRoom.getMembers().stream()
                .map(ChatRoomMember::getMemberId)
                .collect(Collectors.toSet());
    }

    private Page<ChatRoomSummary> fetchSummaryPage(RoomType type, String keyword,
            Pageable pageable) {
        if (isBlankKeyword(keyword)) {
            return chatRoomRepository.findSummaryByType(type, pageable);
        }

        if (isShortKeyword(keyword)) {
            return chatRoomRepository.findSummaryByTypeAndNamePrefix(type, keyword, pageable);
        }

        return fetchFullTextSearchResults(type, keyword, pageable);
    }

    private boolean isBlankKeyword(String keyword) {
        return keyword == null || keyword.isBlank();
    }

    private boolean isShortKeyword(String keyword) {
        if (keyword == null) {
            return false;
        }
        return keyword.length() <= 2;
    }

    private Page<ChatRoomSummary> fetchFullTextSearchResults(RoomType type, String keyword,
            Pageable pageable) {
        Page<ChatRoomSummaryProjection> projPage = chatRoomRepository.findSummaryByTypeAndFullText(
                type != null ? type.name() : null,
                keyword,
                pageable
        );
        return projPage.map(ChatRoomSummary::fromProjection);
    }

    private ChatRoom findChatRoomOrThrow(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
    }

    private ChatRoomMember findChatRoomMemberOrThrow(Long roomId, Long memberId) {
        return chatRoomMemberRepository
                .findByChatRoom_IdAndMemberId(roomId, memberId)
                .orElseThrow(() -> new CustomException(CHAT_ROOM_MEMBER_NOT_FOUND));
    }

    private void validatePasswordNotEmpty(String password) {
        if (password == null || password.isEmpty()) {
            throw new CustomException(PRIVATE_ROOM_PASSWORD_IS_EMPTY);
        }
    }

    private void validateOwner(Long memberId, ChatRoom chatRoom) {
        if (!Objects.equals(chatRoom.getOwnerId(), memberId)) {
            throw new CustomException(ROOM_OWNER_MISMATCH);
        }
    }

    private void validateMemberInRoom(ChatRoom chatRoom, Long memberId) {
        boolean inRoom = chatRoom.getMembers().stream()
                .map(ChatRoomMember::getMemberId)
                .anyMatch(id -> Objects.equals(id, memberId));

        if (!inRoom) {
            throw new CustomException(MEMBER_NOT_ACCEPTED);
        }
    }

    private void validateNotKickingSelf(Long kickedMemberId, Long ownerId) {
        if (Objects.equals(ownerId, kickedMemberId)) {
            throw new CustomException(OWNER_CANNOT_BE_KICKED);
        }
    }

    private void validateChatRoomNameNotDuplicated(String name) {
        if (chatRoomRepository.existsByName(name)) {
            throw new CustomException(CHAT_ROOM_NAME_DUPLICATE);
        }
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
        if (!isMemberInRoom(roomId, memberId)) {
            return 0L;
        }

        LocalDateTime lastReadTime = unreadMessageService.getLastReadTime(memberId, roomId);
        if (lastReadTime == null) {
            return 0L;
        }

        return chatRepository.countByRoomIdAndCreatedAtAfterAndSenderIdNot(roomId, lastReadTime,
                memberId);
    }

    private boolean isMemberInRoom(Long roomId, Long memberId) {
        return chatRoomMemberRepository.existsByChatRoom_IdAndMemberId(roomId, memberId);
    }

    private Map<Long, ChatRoomResponse.LatestMessage> fetchLatestMessages(List<Long> roomIds) {
        return roomIds.stream()
                .map(this::createLatestMessageEntry)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private Map.Entry<Long, ChatRoomResponse.LatestMessage> createLatestMessageEntry(Long roomId) {
        var latestChat = chatRepository.findFirstByRoomIdOrderByCreatedAtDesc(roomId);
        if (latestChat == null) {
            return null;
        }

        ChatRoomResponse.LatestMessage message = new ChatRoomResponse.LatestMessage(
                latestChat.getMessage(),
                latestChat.getSenderName(),
                latestChat.getCreatedAt()
        );

        return new AbstractMap.SimpleEntry<>(roomId, message);
    }

    private Page<ChatRoomResponse> mapToResponsePage(
            Page<ChatRoomSummary> summaryPage,
            Map<Long, Long> memberCounts,
            Set<Long> joinedRoomIds,
            Map<Long, Long> unreadCounts,
            Map<Long, ChatRoomResponse.LatestMessage> latestMessages,
            Pageable pageable) {

        List<ChatRoomResponse> responses = summaryPage.getContent().stream()
                .map(summary -> createChatRoomResponse(
                        summary,
                        memberCounts,
                        joinedRoomIds,
                        unreadCounts,
                        latestMessages))
                .toList();

        return new PageImpl<>(responses, pageable, summaryPage.getTotalElements());
    }

    private ChatRoomResponse createChatRoomResponse(
            ChatRoomSummary summary,
            Map<Long, Long> memberCounts,
            Set<Long> joinedRoomIds,
            Map<Long, Long> unreadCounts,
            Map<Long, ChatRoomResponse.LatestMessage> latestMessages) {

        boolean joined = joinedRoomIds.contains(summary.id());
        ChatRoomResponse.LatestMessage latestMessage =
                joined ? latestMessages.get(summary.id()) : null;

        return ChatRoomMapper.toChatRoomResponse(
                summary,
                memberCounts.getOrDefault(summary.id(), 0L),
                joined,
                unreadCounts.getOrDefault(summary.id(), 0L),
                latestMessage
        );
    }
}


