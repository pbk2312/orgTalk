package yuhan.pro.chatserver.domain.service;


import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.CHAT_ROOM_MEMBER_NOT_FOUND;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.CHAT_ROOM_NAME_DUPLICATE;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.MEMBER_NOT_ACCEPTED;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.MEMBER_NOT_FOUND;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.ORGANIZATION_NOT_FOUND;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.OWNER_CANNOT_BE_KICKED;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.PRIVATE_ROOM_PASSWORD_IS_EMPTY;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.PRIVATE_ROOM_PASSWORD_NOT_MATCH;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.ROOM_OWNER_MISMATCH;

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

    @Transactional
    public ChatRoomCreateResponse saveChatRoom(ChatRoomCreateRequest request) {

        Authentication authentication = getAuthentication();

        Long memberId = getMemberId(authentication);

        valiadteChatRoomNameDuplicated(request);

        validateMemberInOrg(request.organizationId(), authentication);

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
    }


    @Transactional(readOnly = true)
    public PageResponse<ChatRoomResponse> getChatRooms(Long organizationId, RoomType type,
            Pageable pageable) {

        Authentication authentication = getAuthentication();

        Long memberId = getMemberId(authentication);

        validateMemberInOrg(organizationId, authentication);

        Page<ChatRoomSummary> summaryPage = fetchSummaries(organizationId, type, pageable);
        if (summaryPage.isEmpty()) {
            return PageResponse.fromPage(Page.empty(pageable));
        }

        List<Long> chatRoomIds = extractRoomIds(summaryPage);
        Map<Long, Long> memberCounts = fetchMemberCounts(chatRoomIds);
        Set<Long> joinedRoomIds = fetchJoinedRoomIds(chatRoomIds, memberId);

        Page<ChatRoomResponse> responsePage = mapToResponsePage(summaryPage, memberCounts,
                joinedRoomIds, pageable);
        return PageResponse.fromPage(responsePage);
    }


    @Transactional(readOnly = true)
    public PageResponse<ChatRoomResponse> searchChatRooms(
            Long organizationId,
            RoomType type,
            String keyword,
            Pageable pageable
    ) {
        Long memberId = getMemberId(getAuthentication());

        // ① 키워드 분기 로직 분리
        Page<ChatRoomSummary> summaryPage = fetchSummaryPage(organizationId, type, keyword,
                pageable);

        // ② 검색 결과가 없으면 빈 페이지 리턴
        if (summaryPage.isEmpty()) {
            return PageResponse.fromPage(Page.empty(pageable));
        }

        // ③ 추가 데이터 조회 및 매핑
        List<Long> roomIds = summaryPage.stream()
                .map(ChatRoomSummary::id)
                .toList();
        Map<Long, Long> memberCounts = fetchMemberCounts(roomIds);
        Set<Long> joinedRoomIds = fetchJoinedRoomIds(roomIds, memberId);

        Page<ChatRoomResponse> responsePage = mapToResponsePage(
                summaryPage,
                memberCounts,
                joinedRoomIds,
                pageable
        );
        return PageResponse.fromPage(responsePage);
    }


    @Transactional(readOnly = true)
    public ChatRoomInfoResponse getChatRoomInfo(Long roomId, String jwtToken) {
        ChatRoom chatRoom = findChatRoomOrThrow(roomId);

        // 메인 서버에서 참여중인 멤버 정보 가져오기
        Set<Long> memberIds = chatRoom.getMembers().stream()
                .map(ChatRoomMember::getMemberId)
                .collect(Collectors.toSet());

        Authentication auth = getAuthentication();
        Long memberId = getMemberId(auth);

        validateMemberRoomIn(chatRoom, memberId);

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
            Long organizationId,
            RoomType type,
            String keyword,
            Pageable pageable
    ) {
        if (keyword == null || keyword.isBlank()) {
            return chatRoomRepository.findSummaryByOrgAndType(organizationId, type, pageable);
        }

        if (keyword.length() <= 2) {
            return chatRoomRepository.findSummaryByOrgTypeAndNamePrefix(
                    organizationId, type, keyword, pageable
            );
        }

        // 3) 긴 키워드: full-text 검색
        Page<ChatRoomSummaryProjection> projPage =
                chatRoomRepository.findSummaryByOrgTypeAndFullText(
                        organizationId,
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

    private void validateMemberInOrg(Long orgId, Authentication authentication) {
        if (authentication != null
                && authentication.getPrincipal() instanceof ChatMemberDetails chatMemberDetails) {
            Set<Long> orgIds = chatMemberDetails.getOrganizationIds();
            if (orgIds == null || !orgIds.contains(orgId)) {
                throw new CustomException(ORGANIZATION_NOT_FOUND);
            }
        }
    }

    private Page<ChatRoomSummary> fetchSummaries(Long orgId, RoomType type, Pageable pageable) {
        return chatRoomRepository.findChatRoomsByOrgAndType(orgId, type, pageable);
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

    private Page<ChatRoomResponse> mapToResponsePage(
            Page<ChatRoomSummary> summaryPage,
            Map<Long, Long> memberCounts,
            Set<Long> joinedRoomIds,
            Pageable pageable
    ) {
        List<ChatRoomResponse> responses = summaryPage.getContent().stream()
                .map(summary -> ChatRoomMapper.toChatRoomResponse(
                        summary,
                        memberCounts.getOrDefault(summary.id(), 0L),
                        joinedRoomIds.contains(summary.id())
                ))
                .toList();

        return new PageImpl<>(
                responses,
                pageable,
                summaryPage.getTotalElements()
        );
    }

    private void valiadteChatRoomNameDuplicated(ChatRoomCreateRequest request) {
        if (chatRoomRepository.existsByOrganizationIdAndName(
                request.organizationId(), request.name())) {
            throw new CustomException(CHAT_ROOM_NAME_DUPLICATE);
        }
    }
}
