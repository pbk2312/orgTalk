package yuhan.pro.chatserver.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import yuhan.pro.chatserver.domain.dto.ChatRoomCreateRequest;
import yuhan.pro.chatserver.domain.dto.ChatRoomResponse;
import yuhan.pro.chatserver.domain.dto.ChatRoomUpdateRequest;
import yuhan.pro.chatserver.domain.dto.JoinChatRoomRequest;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.entity.ChatRoomMember;
import yuhan.pro.chatserver.domain.entity.RoomType;
import yuhan.pro.chatserver.domain.repository.ChatRoomMemberRepository;
import yuhan.pro.chatserver.domain.repository.ChatRoomRepository;
import yuhan.pro.chatserver.sharedkernel.dto.PageResponse;
import yuhan.pro.chatserver.sharedkernel.exception.CustomException;
import yuhan.pro.chatserver.sharedkernel.jwt.ChatMemberDetails;
import yuhan.pro.chatserver.sharedkernel.jwt.MemberRole;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private ChatRoomMemberRepository chatRoomMemberRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private ChatRoomService chatRoomService;

  ChatMemberDetails chatMemberDetails;
  Principal principal;

  @BeforeEach
  void setUp() {
    chatMemberDetails = ChatMemberDetails
        .builder()
        .memberId(1L)
        .memberRole(MemberRole.USER)
        .username("pbk2312@inu.ac.kr")
        .nickName("pbk2312")
        .password("TEST")
        .organizationIds(Set.of(1L, 2L, 3L))
        .build();

    Authentication authentication = new UsernamePasswordAuthenticationToken(
        chatMemberDetails, null, chatMemberDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    principal = authentication;
  }

  @AfterEach
  void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  @Nested
  @DisplayName("saveChatRoom()")
  class SaveChatRoomTests {

    @Test
    @DisplayName("채팅방 저장")
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
      verify(chatRoomRepository).save(roomCaptor.capture());
      ChatRoom savedRoom = roomCaptor.getValue();
      assertThat(savedRoom.getOrganizationId()).isEqualTo(1L);
      assertThat(savedRoom.getName()).isEqualTo("테스트방");
      assertThat(savedRoom.getDescription()).isEqualTo("설명입니다");
      assertThat(savedRoom.getType()).isEqualTo(RoomType.PUBLIC);
      assertThat(savedRoom.getOwnerId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("채팅방 저장 실패")
    void saveRoomFaliure() {
      // given
      ChatRoomCreateRequest request = ChatRoomCreateRequest.builder()
          .organizationId(100L)
          .name("테스트방")
          .description("설명입니다")
          .type(RoomType.PUBLIC)
          .build();

      assertThatThrownBy(() -> chatRoomService.saveChatRoom(request))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당되는 조직 ID가 존재하지 않습니다");
    }
  }

  @Nested
  @DisplayName("getChatRooms()")
  class GetChatRoomsTests {

    @Test
    @DisplayName("페이징 조회 테스트")
    void getChatRoomsSuccess() {
      // given
      Long organizationId = 1L;
      Pageable pageable = PageRequest.of(0, 2);

      ChatRoom room1 = ChatRoom.builder()
          .ownerId(1L)
          .organizationId(organizationId)
          .name("간단테스트방")
          .description("간단 설명")
          .type(RoomType.PUBLIC)
          .members(List.of())
          .build();

      ChatRoom room2 = ChatRoom.builder()
          .ownerId(1L)
          .organizationId(organizationId)
          .name("간단테스트방2")
          .description("간단 설명2")
          .type(RoomType.PUBLIC)
          .members(List.of())
          .build();

      List<ChatRoom> roomList = List.of(room1, room2);

      long totalElements = 2L;
      Page<ChatRoom> page = new PageImpl<>(roomList, pageable, totalElements);

      when(chatRoomRepository.findAllByOrganizationId(organizationId, pageable))
          .thenReturn(page);

      // when
      PageResponse<ChatRoomResponse> response =
          chatRoomService.getChatRooms(organizationId, pageable);

      // then
      assertThat(response).isNotNull();
      assertThat(response.getTotalElements()).isEqualTo(totalElements);
      assertThat(response.getTotalPages()).isEqualTo(1);
      assertThat(response.getContent()).hasSize(2);
      assertThat(response.getContent().get(0).description()).isEqualTo("간단 설명");
      assertThat(response.getContent().get(1).description()).isEqualTo("간단 설명2");
    }

    @Test
    @DisplayName("채팅방 조회 실패")
    void saveRoomFaliure() {
      // given
      Long organizationId = 100L;
      Pageable pageable = PageRequest.of(0, 2);

      assertThatThrownBy(() -> chatRoomService.getChatRooms(organizationId, pageable))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당되는 조직 ID가 존재하지 않습니다");
    }
  }

  @Nested
  @DisplayName("joinChatRoom()")
  class JoinChatRoomTests {

    @Test
    @DisplayName("공개방 참가 성공")
    void joinPublicRoomSuccess() {
      // given
      JoinChatRoomRequest req = new JoinChatRoomRequest(null);

      ChatRoom publicRoom = ChatRoom.builder()
          .id(1L)
          .ownerId(10L)
          .organizationId(1L)
          .type(RoomType.PUBLIC)
          .build();

      when(chatRoomRepository.findById(1L))
          .thenReturn(Optional.of(publicRoom));

      // when
      chatRoomService.joinChatRoom(1L, req);

      // then
      ArgumentCaptor<ChatRoomMember> cap = ArgumentCaptor.forClass(ChatRoomMember.class);
      verify(chatRoomMemberRepository).save(cap.capture());
      ChatRoomMember savedMember = cap.getValue();
      assertThat(savedMember.getChatRoom().getId()).isEqualTo(1L);
      assertThat(savedMember.getMemberId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("비밀방 비밀번호 불일치 시 실패")
    void joinPrivateRoomWrongPassword() {
      // given
      JoinChatRoomRequest req = new JoinChatRoomRequest("wrongPw");
      ChatRoom privateRoom = ChatRoom.builder()
          .id(1L)
          .ownerId(1L)
          .organizationId(1L)
          .type(RoomType.PRIVATE)
          .password("123")
          .build();
      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(privateRoom));

      assertThatThrownBy(() -> chatRoomService.joinChatRoom(1L, req))
          .isInstanceOf(CustomException.class)
          .hasMessage("비밀번호가 일치하지 않습니다.");

      verify(chatRoomMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("비밀방 올바른 비밀번호로 참가 성공")
    void joinPrivateRoomSuccess() {
      // given
      JoinChatRoomRequest req = new JoinChatRoomRequest("123");
      ChatRoom privateRoom = ChatRoom.builder()
          .id(1L)
          .ownerId(1L)
          .organizationId(1L)
          .type(RoomType.PRIVATE)
          .password("123")
          .build();

      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(privateRoom));
      when(passwordEncoder.matches("123", "123"))
          .thenReturn(true);

      // when
      chatRoomService.joinChatRoom(1L, req);

      // then
      ArgumentCaptor<ChatRoomMember> captor = ArgumentCaptor.forClass(ChatRoomMember.class);
      verify(chatRoomMemberRepository).save(captor.capture());

      ChatRoomMember saved = captor.getValue();
      assertThat(saved.getChatRoom().getId()).isEqualTo(1L);
      assertThat(saved.getMemberId()).isEqualTo(1L);
    }
  }

  @Nested
  @DisplayName("채팅방 삭제")
  class deleteChatroom {


    @Test
    @DisplayName("채팅방 성공적으로 삭제")
    void deleteRoomSuccess() {

      // given
      ChatRoom privateRoom = ChatRoom.builder()
          .id(1L)
          .ownerId(1L)
          .organizationId(1L)
          .type(RoomType.PRIVATE)
          .password("123")
          .build();
      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(privateRoom));

      // when
      chatRoomService.deleteChatRoom(1L);

      // then
      verify(chatRoomRepository).delete(privateRoom);
    }

    @Test
    @DisplayName("채팅방 삭제 실패 - 방장이 아님")
    void deleteRoomFail_NotOwner() {
      // given
      ChatRoom privateRoom = ChatRoom.builder()
          .id(2L)
          .ownerId(99L)
          .organizationId(1L)
          .type(RoomType.PRIVATE)
          .password("pwd")
          .build();

      when(chatRoomRepository.findById(2L)).thenReturn(Optional.of(privateRoom));

      // when & then
      assertThatThrownBy(() -> chatRoomService.deleteChatRoom(2L))
          .isInstanceOf(CustomException.class)
          .hasMessage("채팅방 방장이 아닙니다.");

      verify(chatRoomRepository, never()).delete(any(ChatRoom.class));
    }
  }

  @Nested
  @DisplayName("updateChatRoom()")
  class UpdateChatRoomTests {

    @Test
    @DisplayName("비공개 방 전환 실패 - 비밀번호 미입력")
    void updateFail_NoPassword() {
      // given
      ChatRoom room = ChatRoom.builder()
          .id(1L)
          .ownerId(1L)
          .name("room")
          .description("desc")
          .organizationId(1L)
          .type(RoomType.PUBLIC)
          .build();
      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));

      ChatRoomUpdateRequest req = new ChatRoomUpdateRequest(
          "room2", "desc2", RoomType.PRIVATE, null);

      // when & then
      assertThatThrownBy(() -> chatRoomService.updateChatRoom(1L, req))
          .isInstanceOf(CustomException.class)
          .hasMessage("비밀번호가 비어있습니다.");
    }

    @Test
    @DisplayName("공개->비공개 전환 성공")
    void updateSuccess_PublicToPrivate() {
      // given
      ChatRoom room = ChatRoom.builder()
          .id(1L)
          .ownerId(1L)
          .name("room")
          .description("desc")
          .organizationId(1L)
          .type(RoomType.PUBLIC)
          .build();
      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));
      when(passwordEncoder.encode("pw")).thenReturn("encoded");

      ChatRoomUpdateRequest req = new ChatRoomUpdateRequest(
          "room2", "desc2", RoomType.PRIVATE, "pw");

      // when
      chatRoomService.updateChatRoom(1L, req);

      // then
      assertThat(room.getType()).isEqualTo(RoomType.PRIVATE);
      assertThat(room.getPassword()).isEqualTo("encoded");
      assertThat(room.getName()).isEqualTo("room2");
      assertThat(room.getDescription()).isEqualTo("desc2");
    }

    @Test
    @DisplayName("비공개->공개 전환 성공")
    void updateSuccess_PrivateToPublic() {
      // given
      ChatRoom room = ChatRoom.builder()
          .id(1L)
          .ownerId(1L)
          .name("room")
          .description("desc")
          .organizationId(1L)
          .type(RoomType.PRIVATE)
          .password("encoded")
          .build();
      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));

      ChatRoomUpdateRequest req = new ChatRoomUpdateRequest(
          "room2", "desc2", RoomType.PUBLIC, null);

      // when
      chatRoomService.updateChatRoom(1L, req);

      // then
      assertThat(room.getType()).isEqualTo(RoomType.PUBLIC);
      assertThat(room.getPassword()).isNull();
    }

    @Test
    @DisplayName("비공개 방 비밀번호만 변경 성공")
    void updateSuccess_PrivateChangePassword() {
      // given
      ChatRoom room = ChatRoom.builder()
          .id(1L)
          .ownerId(1L)
          .name("room")
          .description("desc")
          .organizationId(1L)
          .type(RoomType.PRIVATE)
          .password("old")
          .build();
      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));
      when(passwordEncoder.encode("newpw")).thenReturn("newEncoded");

      ChatRoomUpdateRequest req = new ChatRoomUpdateRequest(
          "room", "desc", RoomType.PRIVATE, "newpw");

      // when
      chatRoomService.updateChatRoom(1L, req);

      // then
      assertThat(room.getPassword()).isEqualTo("newEncoded");
    }

    @Test
    @DisplayName("수정 실패 - 방장이 아님")
    void updateFail_NotOwner() {
      // given
      ChatRoom room = ChatRoom.builder()
          .id(1L)
          .ownerId(99L)
          .name("room")
          .description("desc")
          .organizationId(1L)
          .type(RoomType.PUBLIC)
          .build();
      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));

      ChatRoomUpdateRequest req = new ChatRoomUpdateRequest(
          "room2", "desc2", RoomType.PUBLIC, null);

      // when & then
      assertThatThrownBy(() -> chatRoomService.updateChatRoom(1L, req))
          .isInstanceOf(CustomException.class)
          .hasMessage("채팅방 방장이 아닙니다.");
    }
  }
}
