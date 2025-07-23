package yuhan.pro.chatserver.domain.service;

import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.ROOM_ID_NOT_FOUND;
import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.ROOM_MEMBER_NOT_FOUND;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yuhan.pro.chatserver.domain.dto.ChatPageResponse;
import yuhan.pro.chatserver.domain.dto.ChatRequest;
import yuhan.pro.chatserver.domain.dto.ChatResponse;
import yuhan.pro.chatserver.domain.entity.Chat;
import yuhan.pro.chatserver.domain.entity.ChatRoom;
import yuhan.pro.chatserver.domain.mapper.ChatMapper;
import yuhan.pro.chatserver.domain.repository.ChatRoomMemberRepository;
import yuhan.pro.chatserver.domain.repository.ChatRoomRepository;
import yuhan.pro.chatserver.domain.repository.mongoDB.ChatRepository;
import yuhan.pro.chatserver.sharedkernel.exception.CustomException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Transactional
    public void saveChat(
            ChatRequest chatRequest,
            Long roomId
    ) {

        ChatRoom chatRoom = findChatRoomOrThrow(roomId);

        validateMemberInRoom(chatRequest.senderId(), roomId);

        Chat chat = ChatMapper.fromRequest(chatRequest, chatRoom.getId());
        chatRepository.save(chat);
    }

    @Transactional(readOnly = true)
    public ChatPageResponse getChatsByCursor(
            Long roomId, LocalDateTime cursor, int size
    ) {
        ChatRoom room = findChatRoomOrThrow(roomId);

        Pageable pg = PageRequest.of(0, size, Sort.by("createdAt").descending());
        List<Chat> chats;
        if (cursor != null) {
            chats = chatRepository.findByRoomIdAndCreatedAtBeforeOrderByCreatedAtDesc(
                    roomId, cursor, pg);
        } else {
            chats = chatRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pg);
        }

        List<ChatResponse> data = chats.stream()
                .map(chat -> ChatMapper.toChatResponse(room, chat))
                .sorted(Comparator.comparing(ChatResponse::createdAt))
                .toList();

        LocalDateTime nextCursor = data.isEmpty()
                ? null
                : data.getFirst().createdAt();

        return new ChatPageResponse(data, nextCursor);
    }

    private ChatRoom findChatRoomOrThrow(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ROOM_ID_NOT_FOUND));
    }

    public void validateMemberInRoom(Long memberId, Long roomId) {
        if (!chatRoomMemberRepository.existsByChatRoom_IdAndMemberId(roomId, memberId)) {
            throw new CustomException(ROOM_MEMBER_NOT_FOUND);
        }
    }
}
