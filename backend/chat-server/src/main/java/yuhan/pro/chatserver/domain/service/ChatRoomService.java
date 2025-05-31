package yuhan.pro.chatserver.domain.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yuhan.pro.chatserver.domain.dto.ChatRoomCreateRequest;
import yuhan.pro.chatserver.domain.dto.ChatRoomResponse;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.entity.ChatRoomMember;
import yuhan.pro.chatserver.domain.mapper.ChatRoomMapper;
import yuhan.pro.chatserver.domain.repository.ChatRoomMemberRepository;
import yuhan.pro.chatserver.domain.repository.ChatRoomRepository;
import yuhan.pro.chatserver.sharedkernel.dto.PageResponse;
import yuhan.pro.chatserver.sharedkernel.jwt.ChatMemberDetails;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomMemberRepository chatRoomMemberRepository;

  // Todo: 조직에 포함되지 않은 사람은 접근 불가하게 로직 추가

  @Transactional
  public void saveChatRoom(ChatRoomCreateRequest request) {
    Long memberId = getMemberId();
    ChatRoom chatRoom = ChatRoomMapper.fromChatRoomCreateRequest(request, memberId);

    // 방 저장
    chatRoomRepository.save(chatRoom);

    // 멤버 저장
    ChatRoomMember chatRoomMember = ChatRoomMapper.fromMemberId(memberId, chatRoom);
    chatRoomMemberRepository.save(chatRoomMember);
  }

  @Transactional(readOnly = true)
  public PageResponse<ChatRoomResponse> getChatRooms(Long organizationId, Pageable pageable) {

    Page<ChatRoom> chatRoomPage = chatRoomRepository.findAllByOrganizationId(organizationId,
        pageable);

    Page<ChatRoomResponse> dtoPage = chatRoomPage.map(ChatRoomMapper::toChatRoomResponse);

    return PageResponse.fromPage(dtoPage);
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
