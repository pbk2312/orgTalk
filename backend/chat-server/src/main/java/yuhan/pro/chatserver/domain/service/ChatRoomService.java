package yuhan.pro.chatserver.domain.service;


import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.MEMBER_NOT_FOUND;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.PRIVATE_ROOM_PASSWORD_IS_EMPTY;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.PRIVATE_ROOM_PASSWORD_NOT_MATCH;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
import yuhan.pro.chatserver.domain.dto.JoinChatRoomRequest;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.entity.ChatRoomMember;
import yuhan.pro.chatserver.domain.entity.RoomType;
import yuhan.pro.chatserver.domain.mapper.ChatRoomMapper;
import yuhan.pro.chatserver.domain.repository.ChatRoomMemberRepository;
import yuhan.pro.chatserver.domain.repository.ChatRoomRepository;
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

  // Todo: 조직에 포함되지 않은 사람은 접근 불가하게 로직 추가
  // Todo: roomId를 응답에 넘기기
  @Transactional
  public ChatRoomCreateResponse saveChatRoom(ChatRoomCreateRequest request) {
    Long memberId = getMemberId();

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

    Long memberId = getMemberId();
    ChatRoomMember chatRoomMember = ChatRoomMapper.fromMemberId(memberId, chatRoom);
    chatRoomMemberRepository.save(chatRoomMember);
  }


  @Transactional(readOnly = true)
  public PageResponse<ChatRoomResponse> getChatRooms(Long organizationId, Pageable pageable) {

    Long memberId = getMemberId();

    Page<ChatRoom> chatRoomPage = chatRoomRepository.findAllByOrganizationId(organizationId,
        pageable);

    Page<ChatRoomResponse> dtoPage = chatRoomPage.map(chatRoom ->
        ChatRoomMapper.toChatRoomResponse(chatRoom, memberId)
    );

    return PageResponse.fromPage(dtoPage);
  }

  @Transactional(readOnly = true)
  public ChatRoomInfoResponse getChatRoomInfo(Long roomId, String jwtToken) {
    ChatRoom chatRoom = findChatRoomOrThrow(roomId);

    // 메인 서버에서 참여중인 멤버 정보 가져오기
    Set<Long> memberIds = chatRoom.getMembers().stream()
        .map(ChatRoomMember::getMemberId)
        .collect(Collectors.toSet());

    Set<ChatMemberResponse> chatMembers = memberClient.getChatMembers(memberIds, jwtToken);

    return ChatRoomMapper.toChatRoomInfoResponse(chatRoom, chatMembers);
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

  private Long getMemberId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof ChatMemberDetails chatMemberDetails) {
      return chatMemberDetails.getMemberId();
    }
    return null;
  }
}
