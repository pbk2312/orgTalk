package yuhan.pro.chatserver.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
import yuhan.pro.chatserver.domain.dto.ChatRequest;
import yuhan.pro.chatserver.domain.dto.ChatResponse;
import yuhan.pro.chatserver.domain.entity.Chat;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.entity.Language;
import yuhan.pro.chatserver.domain.entity.MessageType;
import yuhan.pro.chatserver.domain.entity.RoomType;
import yuhan.pro.chatserver.domain.mapper.ChatMapper;
import yuhan.pro.chatserver.domain.repository.ChatRepository;
import yuhan.pro.chatserver.domain.repository.ChatRoomRepository;
import yuhan.pro.chatserver.sharedkernel.exception.CustomException;
import yuhan.pro.chatserver.sharedkernel.jwt.ChatMemberDetails;
import yuhan.pro.chatserver.sharedkernel.jwt.MemberRole;

@DisplayName("ChatService 단위 테스트")
class ChatServiceTest {

  @Mock
  private ChatRepository chatRepository;

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @InjectMocks
  private ChatService chatService;

  private ChatMemberDetails testUser;
  private Principal principal;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    testUser = ChatMemberDetails.builder()
        .memberId(100L)
        .username("mockuser")
        .nickName("Mock User")
        .password("N/A")
        .memberRole(MemberRole.USER)
        .build();

    Authentication authentication = new UsernamePasswordAuthenticationToken(
        testUser, null, testUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    principal = authentication;
  }

  @Nested
  @DisplayName("saveChat()")
  class SaveChatTests {

    @Test
    @DisplayName("정상적으로 Chat 엔티티 저장 후 ChatResponse 반환")
    void saveChat_success() {
      // given
      Long roomId = 1L;
      ChatRequest chatRequest = new ChatRequest(
          "Hello, world!",
          MessageType.TEXT,
          null,
          null
      );

      ChatRoom mockRoom = ChatRoom.builder()
          .id(roomId)
          .organizationId(10L)
          .name("Test Room")
          .description("테스트용 방")
          .type(RoomType.PUBLIC)
          .ownerId(50L)
          .build();

      Chat savedEntity = ChatMapper.fromRequest(chatRequest, mockRoom, testUser.getNickName(),
          testUser.getMemberId());

      Long generatedId = 999L;
      LocalDateTime now = LocalDateTime.now();
      savedEntity = Chat.builder()
          .id(generatedId)
          .room(mockRoom)
          .senderName(testUser.getNickName())
          .senderId(testUser.getMemberId())
          .message(chatRequest.message())
          .type(chatRequest.messageType())
          .codeContent(chatRequest.codeContent())
          .language(chatRequest.language())
          .build();

      when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));
      when(chatRepository.save(any(Chat.class))).thenReturn(savedEntity);

      // when
      ChatResponse response = chatService.saveChat(chatRequest, roomId, principal);

      // then
      ArgumentCaptor<Chat> chatCaptor = ArgumentCaptor.forClass(Chat.class);
      verify(chatRepository).save(chatCaptor.capture());
      Chat passedEntity = chatCaptor.getValue();

      assertEquals(mockRoom, passedEntity.getRoom());
      assertEquals(testUser.getMemberId(), passedEntity.getSenderId());
      assertEquals(testUser.getNickName(), passedEntity.getSenderName());
      assertEquals(chatRequest.message(), passedEntity.getMessage());
      assertEquals(chatRequest.messageType(), passedEntity.getType());
      assertNull(passedEntity.getCodeContent());
      assertNull(passedEntity.getLanguage());

      assertEquals(generatedId, response.id());
      assertEquals(roomId, response.roomId());
      assertEquals(testUser.getMemberId(), response.senderId());
      assertEquals(testUser.getNickName(), response.senderName());
      assertEquals(chatRequest.message(), response.message());
      assertNull(response.codeContent());
      assertNull(response.language());
      assertEquals(MessageType.TEXT, response.messageType());
    }

    @Test
    @DisplayName("존재하지 않는 방 ID로 호출 시 CustomException 발생")
    void saveChat_roomNotFound() {
      // given
      Long roomId = 2L;
      ChatRequest chatRequest = new ChatRequest(
          "This will fail",
          MessageType.TEXT,
          null,
          null
      );

      when(chatRoomRepository.findById(roomId)).thenReturn(Optional.empty());

      // when & then
      CustomException ex = assertThrows(CustomException.class, () ->
          chatService.saveChat(chatRequest, roomId, principal)
      );
      assertEquals("ROOM_ID_NOT_FOUND", ex.getExceptionCode().name());
      verify(chatRepository, never()).save(any());
    }

    @Test
    @DisplayName("Principal이 null일 경우 CustomException 발생")
    void saveChat_principalNull() {
      // given
      Long roomId = 1L;
      ChatRequest chatRequest = new ChatRequest(
          "No auth",
          MessageType.TEXT,
          null,
          null
      );

      // when & then
      CustomException ex = assertThrows(CustomException.class, () ->
          chatService.saveChat(chatRequest, roomId, null)
      );
      assertEquals("AUTHENTICATION_NOT_FOUND", ex.getExceptionCode().name());
      verify(chatRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("getAllChats()")
  class GetAllChatsTests {

    @Test
    @DisplayName("정상적으로 해당 방의 모든 Chat을 조회하여 DTO 리스트 반환")
    void getAllChats_success() {
      // given
      Long roomId = 5L;

      ChatRoom mockRoom = ChatRoom.builder()
          .id(roomId)
          .organizationId(20L)
          .name("History Room")
          .description("이전 대화 보기")
          .type(RoomType.PUBLIC)
          .ownerId(77L)
          .build();

      Chat chat1 = Chat.builder()
          .id(1L)
          .room(mockRoom)
          .senderName("Alice")
          .senderId(101L)
          .message("첫 번째 메시지")
          .type(MessageType.TEXT)
          .codeContent(null)
          .language(null)
          .build();

      Chat chat2 = Chat.builder()
          .id(2L)
          .room(mockRoom)
          .senderName("Bob")
          .senderId(102L)
          .message("두 번째 메시지")
          .type(MessageType.CODE)
          .codeContent("console.log('hi');")
          .language(Language.JAVASCRIPT)
          .build();

      List<Chat> mockChats = List.of(chat1, chat2);

      when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));
      when(chatRepository.findAllByRoom_IdOrderByCreatedAtAsc(roomId)).thenReturn(mockChats);

      // when
      List<ChatResponse> responses = chatService.getAllChats(roomId);

      // then
      verify(chatRoomRepository).findById(roomId);
      verify(chatRepository).findAllByRoom_IdOrderByCreatedAtAsc(roomId);

      assertEquals(2, responses.size());

      ChatResponse resp1 = responses.get(0);
      assertEquals(1L, resp1.id());
      assertEquals(roomId, resp1.roomId());
      assertEquals(101L, resp1.senderId());
      assertEquals("Alice", resp1.senderName());
      assertEquals("첫 번째 메시지", resp1.message());
      assertNull(resp1.codeContent());
      assertNull(resp1.language());
      assertEquals(MessageType.TEXT, resp1.messageType());

      ChatResponse resp2 = responses.get(1);
      assertEquals(2L, resp2.id());
      assertEquals(roomId, resp2.roomId());
      assertEquals(102L, resp2.senderId());
      assertEquals("Bob", resp2.senderName());
      assertEquals("두 번째 메시지", resp2.message());
      assertEquals("console.log('hi');", resp2.codeContent());
      assertEquals(Language.JAVASCRIPT, resp2.language());
      assertEquals(MessageType.CODE, resp2.messageType());
    }

    @Test
    @DisplayName("존재하지 않는 방 ID로 조회 시 CustomException 발생")
    void getAllChats_roomNotFound() {
      // given
      Long roomId = 999L;
      when(chatRoomRepository.findById(roomId)).thenReturn(Optional.empty());

      // when & then
      CustomException ex = assertThrows(CustomException.class, () ->
          chatService.getAllChats(roomId)
      );
      assertEquals("ROOM_ID_NOT_FOUND", ex.getExceptionCode().name());
      verify(chatRepository, never()).findAllByRoom_IdOrderByCreatedAtAsc(any());
    }
  }
}
