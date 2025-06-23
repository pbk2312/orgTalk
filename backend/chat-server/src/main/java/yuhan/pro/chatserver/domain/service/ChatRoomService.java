package yuhan.pro.chatserver.domain.service;


import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.MEMBER_NOT_ACCEPTED;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.MEMBER_NOT_FOUND;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.ORGANIZATION_NOT_FOUND;
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

    // 방 저장
    chatRoomRepository.save(chatRoom);

    // 멤버 저장
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
  public PageResponse<ChatRoomResponse> getChatRooms(Long organizationId, Pageable pageable) {

    Authentication authentication = getAuthentication();
    Long memberId = getMemberId(authentication);

    // todo: 주석 삭제
    // validateMemberInOrg(organizationId, authentication);

    // 1단계: 기본 ChatRoom 정보 조회 (인덱스 활용)
    Page<ChatRoomSummary> summaryPage = chatRoomRepository
        .findChatRoomsByOrg(organizationId, pageable);

    if (summaryPage.isEmpty()) {
      return PageResponse.fromPage(Page.empty(pageable));
    }

    // 2단계: ChatRoom ID 목록 추출
    List<Long> chatRoomIds = summaryPage.getContent()
        .stream()
        .map(ChatRoomSummary::id)
        .toList();

    // 3단계: 멤버 수 조회 (배치)
    Map<Long, Long> memberCounts = chatRoomMemberRepository
        .findMemberCountsByChatRoomIds(chatRoomIds)
        .stream()
        .collect(Collectors.toMap(
            arr -> (Long) arr[0],
            arr -> (Long) arr[1],
            (existing, replacement) -> existing // 중복 키 처리
        ));

    // 4단계: 참여 여부 조회 (배치)
    Set<Long> joinedRoomIds = chatRoomMemberRepository
        .findJoinedChatRoomIds(chatRoomIds, memberId);

    // 5단계: ChatRoomResponse 조합
    List<ChatRoomResponse> responses = summaryPage.getContent()
        .stream()
        .map(summary -> new ChatRoomResponse(
            summary.id(),
            summary.name(),
            summary.description(),
            summary.type(),
            memberCounts.getOrDefault(summary.id(), 0L), // 멤버 수
            joinedRoomIds.contains(summary.id()),        // 참여 여부
            summary.createdAt()
        ))
        .toList();

    Page<ChatRoomResponse> responsePage = new PageImpl<>(
        responses,
        pageable,
        summaryPage.getTotalElements()
    );

    return PageResponse.fromPage(responsePage);
  }

  @Transactional
  public PageResponse<ChatRoomResponse> searchChatRooms(Long organizationId, String keyword,
      Pageable pageable) {

    Authentication authentication = getAuthentication();

    Long memberId = getMemberId(authentication);

    Page<ChatRoom> chatRooms = chatRoomRepository.searchByOrgAndKeyword(organizationId, keyword,
        pageable);

    Page<ChatRoomResponse> dtoPage = chatRooms.map(
        chatRoom -> ChatRoomMapper.toChatRoomResponse(chatRoom, memberId));
    return PageResponse.fromPage(dtoPage);
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
}
