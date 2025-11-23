package yuhan.pro.chatserver.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.time.LocalDateTime;
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
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import yuhan.pro.chatserver.domain.dto.ChatPageResponse;
import yuhan.pro.chatserver.domain.dto.ChatRequest;
import yuhan.pro.chatserver.domain.dto.ChatResponse;
import yuhan.pro.chatserver.domain.entity.Chat;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.entity.MessageType;
import yuhan.pro.chatserver.domain.entity.RoomType;
import yuhan.pro.chatserver.domain.mapper.ChatMapper;
import yuhan.pro.chatserver.domain.repository.ChatRoomMemberRepository;
import yuhan.pro.chatserver.domain.repository.ChatRoomRepository;
import yuhan.pro.chatserver.domain.repository.mongoDB.ChatRepository;
import yuhan.pro.chatserver.domain.service.UnreadMessageService;
import yuhan.pro.chatserver.sharedkernel.exception.CustomException;
import yuhan.pro.chatserver.sharedkernel.jwt.ChatMemberDetails;
import yuhan.pro.chatserver.sharedkernel.jwt.MemberRole;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @Mock
  private ChatRepository chatRepository;

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private ChatRoomMemberRepository chatRoomMemberRepository;

  @Mock
  private UnreadMessageService unreadMessageService;

  @InjectMocks
  private ChatService chatService;

  ChatMemberDetails chatMemberDetails;
  Principal principal;
  ChatRoom chatRoom;


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

    chatRoom = ChatRoom.builder()
        .id(1L)
        .name("Test Room")
        .description("테스트용 방")
        .type(RoomType.PUBLIC)
        .ownerId(chatMemberDetails.getMemberId())
        .build();
  }

  @AfterEach
  void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  @Nested
  @DisplayName("채팅 저장")
  class SaveChatTests {

    @Test
    @DisplayName("몽고 DB에 Chat 저장")
    void saveChatSuccess() {
      // given
      ChatRequest chatRequest = new ChatRequest(
          "테스트 해볼게요!",
          MessageType.TEXT,
          null,
          null,
          chatMemberDetails.getMemberId(),
          chatMemberDetails.getNickName(),
          LocalDateTime.now()
      );

      Chat savedChat = ChatMapper.fromRequest(chatRequest, 1L);

      when(chatRoomRepository.findById(1L))
          .thenReturn(Optional.of(chatRoom));

      when(chatRepository.save(any(Chat.class))).thenReturn(savedChat);
      when(chatRoomMemberRepository.existsByChatRoom_IdAndMemberId(
          chatRoom.getId(), chatMemberDetails.getMemberId()))
          .thenReturn(true);

      // when
      chatService.saveChat(chatRequest, 1L);

      //then
      verify(chatRoomRepository).findById(1L);
      ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
      verify(chatRepository).save(captor.capture());

      Chat actualSaved = captor.getValue();
      assertThat(actualSaved.getMessage()).isEqualTo("테스트 해볼게요!");
      assertThat(actualSaved.getType()).isEqualTo(MessageType.TEXT);
      assertThat(actualSaved.getRoomId()).isEqualTo(1L);
      assertThat(actualSaved.getSenderId()).isEqualTo(1L);
      assertThat(actualSaved.getSenderName()).isEqualTo("pbk2312");
    }

    @Test
    @DisplayName("해당하는 채팅방이 없을 시, 예외 처리")
    void saveChatFailure() {
      // given
      ChatRequest chatRequest = new ChatRequest(
          "테스트 해볼게요!",
          MessageType.TEXT,
          null,
          null,
          chatMemberDetails.getMemberId(),
          chatMemberDetails.getNickName(),
          LocalDateTime.now()
      );

      when(chatRoomRepository.findById(1L))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> chatService.saveChat(chatRequest, 1L))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당하는 채팅방이 없습니다.");

      verify(chatRepository, never()).save(any(Chat.class));
    }

    @Test
    @DisplayName("채팅방에 있는 멤버가 아닐시, 예외처리")
    void saveChatFailure2() {

      // given
      ChatRequest chatRequest = new ChatRequest(
          "테스트 해볼게요!",
          MessageType.TEXT,
          null,
          null,
          chatMemberDetails.getMemberId(),
          chatMemberDetails.getNickName(),
          LocalDateTime.now()
      );

      // 채팅방은 존재
      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
      // 그러나 멤버가 아님
      when(chatRoomMemberRepository.existsByChatRoom_IdAndMemberId(1L,
          chatMemberDetails.getMemberId()))
          .thenReturn(false);

      // when & then
      assertThatThrownBy(() -> chatService.saveChat(chatRequest, 1L))
          .isInstanceOf(CustomException.class)
          .hasMessage("채팅방 멤버가 아닙니다.");

      verify(chatRepository, never()).save(any(Chat.class));
    }

  }

  @Nested
  @DisplayName("커서 기반 채팅 히스토리 가져오기")
  class getChatsCursorTests {

    @Test
    @DisplayName("커서가 null일 때, 처음 페이지 반환")
    void getChatsNoCursor() {
      // given
      LocalDateTime t1 = LocalDateTime.now().minusMinutes(10);
      LocalDateTime t2 = LocalDateTime.now().minusMinutes(5);

      // 오래된 채팅
      Chat chat1 = Chat.builder()
          .id("1")
          .roomId(chatRoom.getId())
          .senderName("userA")
          .senderId(100L)
          .message("첫 번째 메시지")
          .type(MessageType.TEXT)
          .codeContent(null)
          .language(null)
          .build();
      ReflectionTestUtils.setField(chat1, "createdAt", t1);

      // 최신 채팅
      Chat chat2 = Chat.builder()
          .id("2")
          .roomId(chatRoom.getId())
          .senderName("userB")
          .senderId(200L)
          .message("두 번째 메시지")
          .type(MessageType.TEXT)
          .codeContent(null)
          .language(null)
          .build();
      ReflectionTestUtils.setField(chat2, "createdAt", t2);

      when(chatRoomRepository.findById(chatRoom.getId()))
          .thenReturn(Optional.of(chatRoom));

      when(chatRepository.findByRoomIdOrderByCreatedAtDesc(eq(chatRoom.getId()),
          any(Pageable.class)))
          .thenReturn(List.of(chat2, chat1));

      // when
      int pageSize = 2;
      ChatPageResponse response = chatService.getChatsByCursor(chatRoom.getId(), null, pageSize, chatMemberDetails.getMemberId());

      // then
      List<ChatResponse> data = response.chats();
      assertThat(data).hasSize(2);
      assertThat(data.getFirst().message()).isEqualTo("첫 번째 메시지");
      assertThat(data.getFirst().senderId()).isEqualTo(100L);
      assertThat(response.nextCursor()).isEqualTo(t1);

      verify(chatRoomRepository).findById(chatRoom.getId());
      verify(chatRepository).findByRoomIdOrderByCreatedAtDesc(eq(chatRoom.getId()),
          any(Pageable.class));
    }

    @Test
    @DisplayName("커서가 있을 때 이전 메시지 반환")
    void getChatsWithCursor() {
      // given
      LocalDateTime t1 = LocalDateTime.now().minusMinutes(20);
      LocalDateTime t2 = LocalDateTime.now().minusMinutes(10);
      LocalDateTime t3 = LocalDateTime.now().minusMinutes(5);

      Chat chat1 = Chat.builder()
          .id("1")
          .roomId(chatRoom.getId())
          .senderName("userA")
          .senderId(100L)
          .message("메시지 1")
          .type(MessageType.TEXT)
          .codeContent(null)
          .language(null)
          .build();
      ReflectionTestUtils.setField(chat1, "createdAt", t1);

      Chat chat2 = Chat.builder()
          .id("2")
          .roomId(chatRoom.getId())
          .senderName("userB")
          .senderId(200L)
          .message("메시지 2")
          .type(MessageType.TEXT)
          .codeContent(null)
          .language(null)
          .build();
      ReflectionTestUtils.setField(chat2, "createdAt", t2);

      Chat chat3 = Chat.builder()
          .id("3")
          .roomId(chatRoom.getId())
          .senderName("userC")
          .senderId(300L)
          .message("메시지 3")
          .type(MessageType.TEXT)
          .codeContent(null)
          .language(null)
          .build();
      ReflectionTestUtils.setField(chat3, "createdAt", t3);

      when(chatRoomRepository.findById(chatRoom.getId()))
          .thenReturn(Optional.of(chatRoom));

      LocalDateTime cursor = t3;
      int pageSize = 1;

      when(chatRepository.findByRoomIdAndCreatedAtBeforeOrderByCreatedAtDesc(
          eq(chatRoom.getId()), eq(cursor), any(Pageable.class)))
          .thenReturn(List.of(chat2));

      // when
      ChatPageResponse response = chatService.getChatsByCursor(chatRoom.getId(), cursor, pageSize, chatMemberDetails.getMemberId());

      // then
      List<ChatResponse> data = response.chats();
      assertThat(data).hasSize(1);
      assertThat(data.getFirst().message()).isEqualTo("메시지 2");
      assertThat(data.getFirst().senderId()).isEqualTo(200L);

      assertThat(response.nextCursor()).isEqualTo(t2);

      verify(chatRoomRepository).findById(chatRoom.getId());
      verify(chatRepository).findByRoomIdAndCreatedAtBeforeOrderByCreatedAtDesc(
          eq(chatRoom.getId()), eq(cursor), any(Pageable.class));
    }

    @Test
    @DisplayName("커서가 있을 때 이전 메시지 없으면 빈 결과 반환")
    void getChatsEmptyBeforeCursor() {
      // given
      LocalDateTime tCursor = LocalDateTime.now().minusMinutes(30);

      when(chatRoomRepository.findById(chatRoom.getId()))
          .thenReturn(Optional.of(chatRoom));

      when(chatRepository.findByRoomIdAndCreatedAtBeforeOrderByCreatedAtDesc(
          eq(chatRoom.getId()), eq(tCursor), any(Pageable.class)))
          .thenReturn(List.of());

      // when
      ChatPageResponse response = chatService.getChatsByCursor(chatRoom.getId(), tCursor, 10, chatMemberDetails.getMemberId());

      // then
      List<ChatResponse> data = response.chats();
      assertThat(data).isEmpty();
      assertThat(response.nextCursor()).isNull();

      verify(chatRoomRepository).findById(chatRoom.getId());
      verify(chatRepository).findByRoomIdAndCreatedAtBeforeOrderByCreatedAtDesc(
          eq(chatRoom.getId()), eq(tCursor), any(Pageable.class));
    }

    @Test
    @DisplayName("해당하는 채팅방이 없을 시, 예외 처리")
    void getAllChatsFailure() {
      // given
      when(chatRoomRepository.findById(1L)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> chatService.getChatsByCursor(1L, null, 10, chatMemberDetails.getMemberId()))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당하는 채팅방이 없습니다.");

      verify(chatRepository, never()).findByRoomIdOrderByCreatedAtDesc(any(), any(Pageable.class));
      verify(chatRepository, never()).findByRoomIdAndCreatedAtBeforeOrderByCreatedAtDesc(any(),
          any(), any(Pageable.class));
    }
  }
}
