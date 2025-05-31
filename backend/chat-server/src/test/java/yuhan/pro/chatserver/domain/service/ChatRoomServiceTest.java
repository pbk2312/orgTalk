package yuhan.pro.chatserver.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import yuhan.pro.chatserver.domain.dto.ChatRoomCreateRequest;
import yuhan.pro.chatserver.domain.dto.ChatRoomResponse;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.entity.ChatRoomMember;
import yuhan.pro.chatserver.domain.entity.RoomType;
import yuhan.pro.chatserver.domain.repository.ChatRoomMemberRepository;
import yuhan.pro.chatserver.domain.repository.ChatRoomRepository;
import yuhan.pro.chatserver.sharedkernel.dto.PageResponse;
import yuhan.pro.chatserver.sharedkernel.jwt.ChatMemberDetails;
import yuhan.pro.chatserver.sharedkernel.jwt.MemberRole;

@DisplayName("ChatRoomService 단위 테스트")
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

    // SecurityContext에 가짜 사용자(Authentication) 세팅
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

      assertEquals(1L, savedRoom.getOrganizationId());
      assertEquals("테스트방", savedRoom.getName());
      assertEquals("설명입니다", savedRoom.getDescription());
      assertEquals(RoomType.PUBLIC, savedRoom.getType());
      assertEquals(0, savedRoom.getMessageCount());
      assertEquals("", savedRoom.getLastMessage());

      assertEquals(42L, savedMember.getMemberId());
      assertEquals(savedRoom, savedMember.getChatRoom());
    }
  }

  @Nested
  @DisplayName("getChatRooms()")
  class GetChatRoomsTests {

    @Test
    @DisplayName("간단한 페이징 조회 테스트")
    void getChatRoomsSimplePaging() {
      // given
      Long organizationId = 99L;
      Pageable pageable = PageRequest.of(0, 2);

      ChatRoom room = ChatRoom.builder()
          .ownerId(10L)
          .organizationId(organizationId)
          .name("간단테스트방")
          .description("간단 설명")
          .type(RoomType.PUBLIC)
          .build();

      List<ChatRoom> roomList = List.of(room);
      long totalElements = 1L;
      Page<ChatRoom> stubPage = new PageImpl<>(roomList, pageable, totalElements);

      when(chatRoomRepository.findAllByOrganizationId(organizationId, pageable))
          .thenReturn(stubPage);

      // when
      PageResponse<ChatRoomResponse> response =
          chatRoomService.getChatRooms(organizationId, pageable);

      // then
      assertEquals(0, response.getPage());
      assertEquals(2, response.getSize());
      assertEquals(1L, response.getTotalElements());
      assertEquals(1, response.getTotalPages());

      assertEquals(1, response.getContent().size());

      ChatRoomResponse dto = response.getContent().get(0);
      assertEquals("간단테스트방", dto.name());

      verify(chatRoomRepository).findAllByOrganizationId(organizationId, pageable);
    }
  }
}
