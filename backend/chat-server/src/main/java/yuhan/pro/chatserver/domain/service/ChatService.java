package yuhan.pro.chatserver.domain.service;


import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.ROOM_ID_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatRepository chatRepository;
  private final ChatRoomRepository chatRoomRepository;

  @Transactional
  public ChatResponse saveChat(ChatRequest chatRequest, Long roomId) {
    ChatRoom chatRoom = findChatRoomOrThrow(roomId);
    Authentication authentication = getAuthentication();

    String userName = getCurrentUserName(authentication);
    Long memberId = getCurrentUserId(authentication);

    Chat chat = ChatMapper.fromRequest(chatRequest, chatRoom, userName, memberId);
    Chat savedChat = chatRepository.save(chat);
    return ChatMapper.toChatResponse(chatRoom, savedChat);
  }

  private ChatRoom findChatRoomOrThrow(Long roomId) {
    return chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new CustomException(
            ROOM_ID_NOT_FOUND));
  }

  private Long getCurrentUserId(Authentication authentication) {
    if (authentication != null
        && authentication.getPrincipal() instanceof ChatMemberDetails chatMemberDetails) {
      return chatMemberDetails.getMemberId();
    }
    return null;
  }

  private String getCurrentUserName(Authentication authentication) {
    if (authentication != null
        && authentication.getPrincipal() instanceof ChatMemberDetails chatMemberDetails) {
      return chatMemberDetails.getNickName();
    }
    return null;
  }

  private Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }
}
