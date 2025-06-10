package yuhan.pro.chatserver.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import yuhan.pro.chatserver.domain.repository.ChatRoomRepository;
import yuhan.pro.chatserver.domain.repository.mongoDB.ChatRepository;
import yuhan.pro.chatserver.sharedkernel.exception.CustomException;
import yuhan.pro.chatserver.sharedkernel.jwt.ChatMemberDetails;
import yuhan.pro.chatserver.sharedkernel.jwt.MemberRole;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @Mock
  private ChatRepository chatRepository;

  @Mock
  private ChatRoomRepository chatRoomRepository;

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
        .organizationId(10L)
        .name("Test Room")
        .description("테스트용 방")
        .type(RoomType.PUBLIC)
        .ownerId(chatMemberDetails.getMemberId())
        .build();
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
          chatMemberDetails.getNickName()
      );

      Chat savedChat = ChatMapper.fromRequest(chatRequest, 1L);

      when(chatRoomRepository.findById(1L))
          .thenReturn(Optional.of(chatRoom));

      when(chatRepository.save(any(Chat.class))).thenReturn(savedChat);

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
          chatMemberDetails.getNickName()
      );

      when(chatRoomRepository.findById(1L))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> chatService.saveChat(chatRequest, 1L))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당하는 채팅방이 없습니다.");

      verify(chatRepository, never()).save(any(Chat.class));
    }
  }

  @Nested
  @DisplayName("채팅 히스토리 가져오기")
  class getAllChatsTests {

    @Test
    @DisplayName("채팅 히스토리 가져오기")
    void getAllChatsSuccess() {
      // given
      Chat chat1 = Chat.builder()
          .id("1L")
          .roomId(chatRoom.getId())
          .senderName(chatMemberDetails.getNickName())
          .senderId(chatMemberDetails.getMemberId())
          .message("첫 번째 메시지")
          .type(MessageType.TEXT)
          .codeContent(null)
          .language(null)
          .build();

      Chat chat2 = Chat.builder()
          .id("2L")
          .roomId(chatRoom.getId())
          .senderName(chatMemberDetails.getNickName())
          .senderId(chatMemberDetails.getMemberId())
          .message("두 번째 메시지")
          .type(MessageType.CODE)
          .codeContent("console.log('hi');")
          .language(Language.JAVASCRIPT)
          .build();

      List<Chat> chats = List.of(chat1, chat2);

      when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
      when(chatRepository.findByRoomIdOrderByCreatedAtAsc(chatRoom.getId())).thenReturn(chats);

      // when
      List<ChatResponse> responses = chatService.getAllChats(1L);

      // then
      assertThat(responses.size()).isEqualTo(2);
      assertThat(responses.getFirst().id()).isEqualTo("1L");
      assertThat(responses.getLast().id()).isEqualTo("2L");
    }

    @Test
    @DisplayName("해당하는 채팅방이 없을 시, 예외 처리")
    void getAllChatsFailure() {
      // given
      when(chatRoomRepository.findById(1L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> chatService.getAllChats(1L))
          .isInstanceOf(CustomException.class)
          .hasMessage("해당하는 채팅방이 없습니다.");

      verify(chatRepository, never()).save(any(Chat.class));
    }
  }
}
