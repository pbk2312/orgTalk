package yuhan.pro.chatserver.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import yuhan.pro.chatserver.core.MemberClient;
import yuhan.pro.chatserver.domain.dto.ChatRoomCreateRequest;
import yuhan.pro.chatserver.domain.dto.ChatRoomResponse;
import yuhan.pro.chatserver.domain.dto.ChatRoomSummary;
import yuhan.pro.chatserver.domain.dto.ChatRoomSummaryProjection;
import yuhan.pro.chatserver.domain.dto.ChatRoomUpdateRequest;
import yuhan.pro.chatserver.domain.dto.JoinChatRoomRequest;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.entity.ChatRoomMember;
import yuhan.pro.chatserver.domain.entity.RoomType;
import yuhan.pro.chatserver.domain.repository.ChatRoomMemberRepository;
import yuhan.pro.chatserver.domain.repository.ChatRoomRepository;
import yuhan.pro.chatserver.domain.repository.mongoDB.ChatRepository;
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

  @Mock
  private MemberClient memberClient;

  @Mock
  private ChatRepository chatRepository;

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

      when(chatRoomRepository.existsByOrganizationIdAndName(1L, "테스트방"))
          .thenReturn(false);

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

      // 멤버 저장도 확인
      ArgumentCaptor<ChatRoomMember> memberCaptor = ArgumentCaptor.forClass(ChatRoomMember.class);
      verify(chatRoomMemberRepository).save(memberCaptor.capture());
      ChatRoomMember savedMember = memberCaptor.getValue();
      assertThat(savedMember.getMemberId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("채팅방 저장 실패 - 조직에 속하지 않음")
    void saveRoomFailure_NotInOrganization() {
      // given
      ChatRoomCreateRequest request = ChatRoomCreateRequest.builder()
          .organizationId(100L)
          .name("테스트방")
          .description("설명입니다")
          .type(RoomType.PUBLIC)
          .build();

      // when & then
      assertThatThrownBy(() -> chatRoomService.saveChatRoom(request))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당되는 조직 ID가 존재하지 않습니다");
    }

    @Test
    @DisplayName("채팅방 저장 실패 - 중복된 이름")
    void saveRoomFailure_DuplicateName() {
      // given
      ChatRoomCreateRequest request = ChatRoomCreateRequest.builder()
          .organizationId(1L)
          .name("중복방")
          .description("설명입니다")
          .type(RoomType.PUBLIC)
          .build();

      when(chatRoomRepository.existsByOrganizationIdAndName(1L, "중복방"))
          .thenReturn(true);

      // when & then
      assertThatThrownBy(() -> chatRoomService.saveChatRoom(request))
          .isInstanceOf(CustomException.class)
          .hasMessage("이미 존재하는 채팅방 이름입니다.");
    }

    @Test
    @DisplayName("비밀방 저장 성공")
    void savePrivateRoomSuccess() {
      // given
      ChatRoomCreateRequest request = ChatRoomCreateRequest.builder()
          .organizationId(1L)
          .name("비밀방")
          .description("비밀 설명")
          .type(RoomType.PRIVATE)
          .password("password123")
          .build();

      when(chatRoomRepository.existsByOrganizationIdAndName(1L, "비밀방"))
          .thenReturn(false);
      when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

      // when
      chatRoomService.saveChatRoom(request);

      // then
      ArgumentCaptor<ChatRoom> roomCaptor = ArgumentCaptor.forClass(ChatRoom.class);
      verify(chatRoomRepository).save(roomCaptor.capture());
      ChatRoom savedRoom = roomCaptor.getValue();
      assertThat(savedRoom.getType()).isEqualTo(RoomType.PRIVATE);
      assertThat(savedRoom.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("비밀방 저장 실패 - 비밀번호 없음")
    void savePrivateRoomFailure_NoPassword() {
      // given
      ChatRoomCreateRequest request = ChatRoomCreateRequest.builder()
          .organizationId(1L)
          .name("비밀방")
          .description("비밀 설명")
          .type(RoomType.PRIVATE)
          .password(null)
          .build();

      when(chatRoomRepository.existsByOrganizationIdAndName(1L, "비밀방"))
          .thenReturn(false);

      // when & then
      assertThatThrownBy(() -> chatRoomService.saveChatRoom(request))
          .isInstanceOf(CustomException.class)
          .hasMessage("비밀번호가 비어있습니다.");
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

      ChatRoomSummary summary1 = new ChatRoomSummary(1L, "간단테스트방", "간단 설명", RoomType.PUBLIC,
          LocalDateTime.now());
      ChatRoomSummary summary2 = new ChatRoomSummary(2L, "간단테스트방2", "간단 설명2", RoomType.PUBLIC,
          LocalDateTime.now());
      List<ChatRoomSummary> summaries = List.of(summary1, summary2);

      Page<ChatRoomSummary> summaryPage = new PageImpl<>(summaries, pageable, 2L);

      when(chatRoomRepository.findChatRoomsByOrgAndType(organizationId, RoomType.PUBLIC, pageable))
          .thenReturn(summaryPage);

      // 멤버 수 조회 모킹
      when(chatRoomMemberRepository.findMemberCountsByChatRoomIds(List.of(1L, 2L)))
          .thenReturn(List.of(new Object[]{1L, 5L}, new Object[]{2L, 3L}));

      // 참여한 방 조회 모킹
      when(chatRoomMemberRepository.findJoinedChatRoomIds(List.of(1L, 2L), 1L))
          .thenReturn(Set.of(1L));

      // when
      PageResponse<ChatRoomResponse> response =
          chatRoomService.getChatRooms(organizationId, RoomType.PUBLIC, pageable);

      // then
      assertThat(response).isNotNull();
      assertThat(response.getTotalElements()).isEqualTo(2L);
      assertThat(response.getTotalPages()).isEqualTo(1);
      assertThat(response.getContent()).hasSize(2);
      assertThat(response.getContent().get(0).description()).isEqualTo("간단 설명");
      assertThat(response.getContent().get(1).description()).isEqualTo("간단 설명2");
    }

    @Test
    @DisplayName("빈 결과 조회")
    void getChatRoomsEmpty() {
      // given
      Long organizationId = 1L;
      Pageable pageable = PageRequest.of(0, 2);

      Page<ChatRoomSummary> emptyPage = Page.empty(pageable);
      when(chatRoomRepository.findChatRoomsByOrgAndType(organizationId, RoomType.PUBLIC, pageable))
          .thenReturn(emptyPage);

      // when
      PageResponse<ChatRoomResponse> response =
          chatRoomService.getChatRooms(organizationId, RoomType.PUBLIC, pageable);

      // then
      assertThat(response).isNotNull();
      assertThat(response.getTotalElements()).isEqualTo(0L);
      assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("채팅방 조회 실패 - 조직에 속하지 않음")
    void getChatRoomsFailure_NotInOrganization() {
      // given
      Long organizationId = 100L;
      Pageable pageable = PageRequest.of(0, 2);

      // when & then
      assertThatThrownBy(
          () -> chatRoomService.getChatRooms(organizationId, RoomType.PUBLIC, pageable))
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
          .password("encodedPassword")
          .build();
      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(privateRoom));
      when(passwordEncoder.matches("wrongPw", "encodedPassword")).thenReturn(false);

      // when & then
      assertThatThrownBy(() -> chatRoomService.joinChatRoom(1L, req))
          .isInstanceOf(CustomException.class)
          .hasMessage("비밀번호가 일치하지 않습니다.");

      verify(chatRoomMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("비밀방 올바른 비밀번호로 참가 성공")
    void joinPrivateRoomSuccess() {
      // given
      JoinChatRoomRequest req = new JoinChatRoomRequest("correctPw");
      ChatRoom privateRoom = ChatRoom.builder()
          .id(1L)
          .ownerId(1L)
          .organizationId(1L)
          .type(RoomType.PRIVATE)
          .password("encodedPassword")
          .build();

      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(privateRoom));
      when(passwordEncoder.matches("correctPw", "encodedPassword")).thenReturn(true);

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
  @DisplayName("deleteChatRoom()")
  class DeleteChatroomTests {

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
      verify(chatRepository).deleteByRoomId(1L);
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

      verify(chatRepository, never()).deleteByRoomId(any());
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

  @Nested
  @DisplayName("kickOutMember()")
  class KickOutMemberTests {

    @Test
    @DisplayName("멤버 추방 성공")
    void kickOutMemberSuccess() {
      // given
      ChatRoom room = ChatRoom.builder()
          .id(1L)
          .ownerId(1L)
          .organizationId(1L)
          .type(RoomType.PUBLIC)
          .build();

      ChatRoomMember member = ChatRoomMember.builder()
          .id(1L)
          .chatRoom(room)
          .memberId(2L)
          .build();

      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));
      when(chatRoomMemberRepository.findByChatRoom_IdAndMemberId(1L, 2L))
          .thenReturn(Optional.of(member));

      // when
      chatRoomService.kickOutMember(1L, 2L);

      // then
      verify(chatRoomMemberRepository).delete(member);
    }

    @Test
    @DisplayName("멤버 추방 실패 - 방장이 아님")
    void kickOutMemberFail_NotOwner() {
      // given
      ChatRoom room = ChatRoom.builder()
          .id(1L)
          .ownerId(99L)
          .organizationId(1L)
          .type(RoomType.PUBLIC)
          .build();

      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));

      // when & then
      assertThatThrownBy(() -> chatRoomService.kickOutMember(1L, 2L))
          .isInstanceOf(CustomException.class)
          .hasMessage("채팅방 방장이 아닙니다.");
    }

    @Test
    @DisplayName("멤버 추방 실패 - 자기 자신을 추방하려고 함")
    void kickOutMemberFail_KickSelf() {
      // given
      ChatRoom room = ChatRoom.builder()
          .id(1L)
          .ownerId(1L)
          .organizationId(1L)
          .type(RoomType.PUBLIC)
          .build();

      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));

      // when & then
      assertThatThrownBy(() -> chatRoomService.kickOutMember(1L, 1L))
          .isInstanceOf(CustomException.class)
          .hasMessage("방장은 강퇴할 수 없습니다.");
    }

    @Test
    @DisplayName("멤버 추방 실패 - 존재하지 않는 멤버")
    void kickOutMemberFail_MemberNotFound() {
      // given
      ChatRoom room = ChatRoom.builder()
          .id(1L)
          .ownerId(1L)
          .organizationId(1L)
          .type(RoomType.PUBLIC)
          .build();

      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));
      when(chatRoomMemberRepository.findByChatRoom_IdAndMemberId(1L, 2L))
          .thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> chatRoomService.kickOutMember(1L, 2L))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당 멤버가 방에 없습니다.");
    }
  }

  @Nested
  @DisplayName("searchChatRooms()")
  class SearchChatRoomsTests {

    @Test
    @DisplayName("키워드 없이 검색 - 전체 조회")
    void searchWithoutKeyword() {
      // given
      Long organizationId = 1L;
      Pageable pageable = PageRequest.of(0, 10);

      ChatRoomSummary summary1 = new ChatRoomSummary(1L, "테스트방1", "설명1", RoomType.PUBLIC,
          LocalDateTime.now());
      ChatRoomSummary summary2 = new ChatRoomSummary(2L, "테스트방2", "설명2", RoomType.PUBLIC,
          LocalDateTime.now());
      List<ChatRoomSummary> summaries = List.of(summary1, summary2);
      Page<ChatRoomSummary> summaryPage = new PageImpl<>(summaries, pageable, 2L);

      when(chatRoomRepository.findSummaryByOrgAndType(organizationId, RoomType.PUBLIC, pageable))
          .thenReturn(summaryPage);

      List<Object[]> counts = new ArrayList<>();
      counts.add(new Object[]{1L, 3L});
      counts.add(new Object[]{2L, 5L});
      when(chatRoomMemberRepository.findMemberCountsByChatRoomIds(List.of(1L, 2L)))
          .thenReturn(counts);

      when(chatRoomMemberRepository.findJoinedChatRoomIds(List.of(1L, 2L), 1L))
          .thenReturn(Set.of(1L));

      // when
      PageResponse<ChatRoomResponse> response =
          chatRoomService.searchChatRooms(organizationId, RoomType.PUBLIC, null, pageable);

      // then
      assertThat(response.getTotalElements()).isEqualTo(2L);
      assertThat(response.getContent()).hasSize(2);
      verify(chatRoomRepository).findSummaryByOrgAndType(organizationId, RoomType.PUBLIC, pageable);
    }

    @Test
    @DisplayName("짧은 키워드로 검색 (2글자 이하)")
    void searchWithShortKeyword() {
      // given
      Long organizationId = 1L;
      String keyword = "테스";
      Pageable pageable = PageRequest.of(0, 10);

      ChatRoomSummary summary = new ChatRoomSummary(1L, "테스트방", "설명", RoomType.PUBLIC,
          LocalDateTime.now());
      Page<ChatRoomSummary> summaryPage = new PageImpl<>(List.of(summary), pageable, 1L);

      when(chatRoomRepository.findSummaryByOrgTypeAndNamePrefix(
          organizationId, RoomType.PUBLIC, keyword, pageable))
          .thenReturn(summaryPage);

      List<Object[]> counts = new ArrayList<>();
      counts.add(new Object[]{1L, 5L});
      when(chatRoomMemberRepository.findMemberCountsByChatRoomIds(List.of(1L)))
          .thenReturn(counts);

      when(chatRoomMemberRepository.findJoinedChatRoomIds(List.of(1L), 1L))
          .thenReturn(Set.of());

      // when
      PageResponse<ChatRoomResponse> response =
          chatRoomService.searchChatRooms(organizationId, RoomType.PUBLIC, keyword, pageable);

      // then
      assertThat(response.getTotalElements()).isEqualTo(1L);
      assertThat(response.getContent()).hasSize(1);
      assertThat(response.getContent().getFirst().name()).isEqualTo("테스트방");
      verify(chatRoomRepository).findSummaryByOrgTypeAndNamePrefix(
          organizationId, RoomType.PUBLIC, keyword, pageable);
    }

    @Test
    @DisplayName("긴 키워드로 풀텍스트 검색 (3글자 이상)")
    void searchWithLongKeyword() {
      // given
      Long organizationId = 1L;
      String keyword = "개발팀회의";
      Pageable pageable = PageRequest.of(0, 10);

      ChatRoomSummaryProjection projection = getChatRoomSummaryProjection();
      Page<ChatRoomSummaryProjection> projectionPage =
          new PageImpl<>(List.of(projection), pageable, 1L);

      when(chatRoomRepository.findSummaryByOrgTypeAndFullText(
          organizationId, "PUBLIC", keyword, pageable))
          .thenReturn(projectionPage);

      List<Object[]> counts = new ArrayList<>();
      counts.add(new Object[]{1L, 8L});
      when(chatRoomMemberRepository.findMemberCountsByChatRoomIds(List.of(1L)))
          .thenReturn(counts);

      when(chatRoomMemberRepository.findJoinedChatRoomIds(List.of(1L), 1L))
          .thenReturn(Set.of(1L));

      // when
      PageResponse<ChatRoomResponse> response =
          chatRoomService.searchChatRooms(organizationId, RoomType.PUBLIC, keyword, pageable);

      // then
      assertThat(response.getTotalElements()).isEqualTo(1L);
      assertThat(response.getContent()).hasSize(1);
      assertThat(response.getContent().getFirst().name()).isEqualTo("개발팀 회의방");
      verify(chatRoomRepository).findSummaryByOrgTypeAndFullText(
          organizationId, "PUBLIC", keyword, pageable);
    }

    @Test
    @DisplayName("검색 결과 없음")
    void searchWithNoResults() {
      // given
      Long organizationId = 1L;
      String keyword = "존재하지않는방";
      Pageable pageable = PageRequest.of(0, 10);

      when(chatRoomRepository.findSummaryByOrgTypeAndFullText(
          organizationId, "PUBLIC", keyword, pageable))
          .thenReturn(Page.empty(pageable));

      // when
      PageResponse<ChatRoomResponse> response =
          chatRoomService.searchChatRooms(organizationId, RoomType.PUBLIC, keyword, pageable);

      // then
      assertThat(response.getTotalElements()).isEqualTo(0L);
      assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("빈 키워드로 검색")
    void searchWithEmptyKeyword() {
      // given
      Long organizationId = 1L;
      String keyword = "   ";
      Pageable pageable = PageRequest.of(0, 10);

      ChatRoomSummary summary = new ChatRoomSummary(1L, "방1", "설명1", RoomType.PUBLIC,
          LocalDateTime.now());
      Page<ChatRoomSummary> summaryPage = new PageImpl<>(List.of(summary), pageable, 1L);

      when(chatRoomRepository.findSummaryByOrgAndType(organizationId, RoomType.PUBLIC, pageable))
          .thenReturn(summaryPage);

      List<Object[]> counts = new ArrayList<>();
      counts.add(new Object[]{1L, 2L});
      when(chatRoomMemberRepository.findMemberCountsByChatRoomIds(List.of(1L)))
          .thenReturn(counts);

      when(chatRoomMemberRepository.findJoinedChatRoomIds(List.of(1L), 1L))
          .thenReturn(Set.of());

      // when
      PageResponse<ChatRoomResponse> response =
          chatRoomService.searchChatRooms(organizationId, RoomType.PUBLIC, keyword, pageable);

      // then
      assertThat(response.getTotalElements()).isEqualTo(1L);
      verify(chatRoomRepository).findSummaryByOrgAndType(organizationId, RoomType.PUBLIC, pageable);
    }

    @Test
    @DisplayName("비공개방 검색")
    void searchPrivateRooms() {
      // given
      Long organizationId = 1L;
      String keyword = "비밀";
      Pageable pageable = PageRequest.of(0, 10);

      ChatRoomSummary summary = new ChatRoomSummary(1L, "비밀회의방", "비밀 회의", RoomType.PRIVATE,
          LocalDateTime.now());
      Page<ChatRoomSummary> summaryPage = new PageImpl<>(List.of(summary), pageable, 1L);

      when(chatRoomRepository.findSummaryByOrgTypeAndNamePrefix(
          organizationId, RoomType.PRIVATE, keyword, pageable))
          .thenReturn(summaryPage);

      List<Object[]> counts = new ArrayList<>();
      counts.add(new Object[]{1L, 3L});
      when(chatRoomMemberRepository.findMemberCountsByChatRoomIds(List.of(1L)))
          .thenReturn(counts);

      when(chatRoomMemberRepository.findJoinedChatRoomIds(List.of(1L), 1L))
          .thenReturn(Set.of());

      // when
      PageResponse<ChatRoomResponse> response =
          chatRoomService.searchChatRooms(organizationId, RoomType.PRIVATE, keyword, pageable);

      // then
      assertThat(response.getTotalElements()).isEqualTo(1L);
      assertThat(response.getContent().getFirst().type()).isEqualTo(RoomType.PRIVATE);
      verify(chatRoomRepository).findSummaryByOrgTypeAndNamePrefix(
          organizationId, RoomType.PRIVATE, keyword, pageable);
    }
  }

  private static ChatRoomSummaryProjection getChatRoomSummaryProjection() {
    return new ChatRoomSummaryProjection() {
      @Override
      public Long getId() {
        return 1L;
      }

      @Override
      public String getName() {
        return "개발팀 회의방";
      }

      @Override
      public String getDescription() {
        return "개발팀 회의용";
      }

      @Override
      public String getType() {
        return "PUBLIC";
      }

      @Override
      public LocalDateTime getCreatedAt() {
        return LocalDateTime.now();
      }
    };
  }
}
