package yuhan.pro.chatserver.domain.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import yuhan.pro.chatserver.domain.dto.ChatRoomCreateRequest;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.entity.ChatRoomMember;
import yuhan.pro.chatserver.domain.entity.RoomType;
import yuhan.pro.chatserver.domain.repository.ChatRoomMemberRepository;
import yuhan.pro.chatserver.domain.repository.ChatRoomRepository;
import yuhan.pro.chatserver.sharedkernel.jwt.ChatMemberDetails;
import yuhan.pro.chatserver.sharedkernel.jwt.MemberRole;

class ChatRoomServiceTest {

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private ChatRoomMemberRepository chatRoomMemberRepository;

  @InjectMocks
  private ChatRoomService chatRoomService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    ChatMemberDetails testUser = ChatMemberDetails.builder()
        .memberId(42L)
        .username("testuser")
        .nickName("Test User")
        .password("N/A")
        .memberRole(MemberRole.USER)
        .build();

    Authentication authentication = new UsernamePasswordAuthenticationToken(
        testUser, null, testUser.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Nested
  @DisplayName("saveChatRoom()")
  class SaveChatRoomTests {

    @Test
    @DisplayName("정상적으로 ChatRoom과 ChatRoomMember 저장")
    void saveChatRoom() {
      // given
      ChatRoomCreateRequest request = ChatRoomCreateRequest.builder()
          .organizationId(1L)
          .name("테스트방")
          .description("설명입니다")
          .type(RoomType.PUBLIC)
          .build();

      // when
      chatRoomService.saveChatRoom(request);

      // then
      ArgumentCaptor<ChatRoom> roomCaptor = ArgumentCaptor.forClass(ChatRoom.class);
      ArgumentCaptor<ChatRoomMember> memberCaptor = ArgumentCaptor.forClass(ChatRoomMember.class);

      verify(chatRoomRepository).save(roomCaptor.capture());
      verify(chatRoomMemberRepository).save(memberCaptor.capture());

      ChatRoom savedRoom = roomCaptor.getValue();
      ChatRoomMember savedMember = memberCaptor.getValue();

      assert savedRoom.getOrganizationId().equals(1L);
      assert savedRoom.getName().equals("테스트방");
      assert savedRoom.getDescription().equals("설명입니다");
      assert savedRoom.getType() == RoomType.PUBLIC;
      assert savedRoom.getMessageCount() == 0;
      assert savedRoom.getLastMessage().equals("");

      assert savedMember.getMemberId().equals(42L);
      assert savedMember.getChatRoom() == savedRoom;
    }
  }
}
