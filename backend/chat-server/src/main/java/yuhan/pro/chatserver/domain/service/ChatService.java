package yuhan.pro.chatserver.domain.service;

import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.AUTHENTICATION_NOT_FOUND;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.ROOM_ID_NOT_FOUND;

import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yuhan.pro.chatserver.domain.dto.ChatRequest;
import yuhan.pro.chatserver.domain.dto.ChatResponse;
import yuhan.pro.chatserver.domain.entity.Chat;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.mapper.ChatMapper;
import yuhan.pro.chatserver.domain.repository.ChatRepository;
import yuhan.pro.chatserver.domain.repository.ChatRoomRepository;
import yuhan.pro.chatserver.sharedkernel.exception.CustomException;
import yuhan.pro.chatserver.sharedkernel.jwt.ChatMemberDetails;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatRepository chatRepository;
  private final ChatRoomRepository chatRoomRepository;


  @Transactional
  public ChatResponse saveChat(
      ChatRequest chatRequest,
      Long roomId,
      Principal principal
  ) {

    validatePrincipal(principal);

    UsernamePasswordAuthenticationToken token =
        (UsernamePasswordAuthenticationToken) principal;

    ChatMemberDetails userDetails = (ChatMemberDetails) token.getPrincipal();
    Long memberId = userDetails.getMemberId();
    String userName = userDetails.getNickName();

    ChatRoom chatRoom = findChatRoomOrThrow(roomId);

    Chat chat = ChatMapper.fromRequest(chatRequest, chatRoom, userName, memberId);
    Chat savedChat = chatRepository.save(chat);
    return ChatMapper.toChatResponse(chatRoom, savedChat);
  }

  @Transactional(readOnly = true)
  public List<ChatResponse> getAllChats(Long roomId) {
    ChatRoom room = findChatRoomOrThrow(roomId);

    List<Chat> chats = chatRepository.findAllByRoom_IdOrderByCreatedAtAsc(
        roomId);

    return chats.stream()
        .map(chat -> ChatMapper.toChatResponse(room, chat))
        .toList();
  }

  private ChatRoom findChatRoomOrThrow(Long roomId) {
    return chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new CustomException(ROOM_ID_NOT_FOUND));
  }

  private static void validatePrincipal(Principal principal) {
    if (principal == null) {
      throw new CustomException(AUTHENTICATION_NOT_FOUND);
    }
  }
}
