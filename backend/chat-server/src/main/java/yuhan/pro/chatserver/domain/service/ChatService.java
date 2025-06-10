package yuhan.pro.chatserver.domain.service;

import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.ROOM_ID_NOT_FOUND;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yuhan.pro.chatserver.domain.dto.ChatRequest;
import yuhan.pro.chatserver.domain.dto.ChatResponse;
import yuhan.pro.chatserver.domain.entity.Chat;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.mapper.ChatMapper;
import yuhan.pro.chatserver.domain.repository.ChatRoomRepository;
import yuhan.pro.chatserver.domain.repository.mongoDB.ChatRepository;
import yuhan.pro.chatserver.sharedkernel.exception.CustomException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatRepository chatRepository;
  private final ChatRoomRepository chatRoomRepository;


  public void saveChat(
      ChatRequest chatRequest,
      Long roomId
  ) {

    ChatRoom chatRoom = findChatRoomOrThrow(roomId);

    Chat chat = ChatMapper.fromRequest(chatRequest, chatRoom.getId());
    chatRepository.save(chat);
  }

  @Transactional(readOnly = true)
  public List<ChatResponse> getAllChats(Long roomId) {
    ChatRoom room = findChatRoomOrThrow(roomId);

    List<Chat> chats = chatRepository.findByRoomIdOrderByCreatedAtAsc(roomId);

    return chats.stream()
        .map(chat -> ChatMapper.toChatResponse(room, chat))
        .toList();
  }

  private ChatRoom findChatRoomOrThrow(Long roomId) {
    return chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new CustomException(ROOM_ID_NOT_FOUND));
  }
}
