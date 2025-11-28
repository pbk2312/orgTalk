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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import yuhan.pro.chatserver.sharedkernel.jwt.ChatMemberDetails;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UnreadMessageService unreadMessageService;

    @Transactional
    public void saveChat(
            ChatRequest chatRequest,
            Long roomId
    ) {

        ChatRoom chatRoom = findChatRoomOrThrow(roomId);

        validateMemberInRoom(chatRequest.senderId(), roomId);

        Chat chat = ChatMapper.fromRequest(chatRequest, chatRoom.getId());
        chatRepository.save(chat);

        unreadMessageService.updateLastReadTime(
                chatRequest.senderId(),
                roomId,
                chatRequest.createdAt()
        );
        log.debug("메시지 전송 시 읽음 처리: senderId={}, roomId={}, createdAt={}",
                chatRequest.senderId(), roomId, chatRequest.createdAt());
    }

    @org.springframework.scheduling.annotation.Async("chatSaveExecutor")
    public void saveChatAsync(
            ChatRequest chatRequest,
            Long roomId
    ) {
        try {
            // 검증은 캐시를 사용하거나 최소화
            Chat chat = ChatMapper.fromRequest(chatRequest, roomId);
            chatRepository.save(chat);

            // 읽음 처리도 비동기로
            unreadMessageService.updateLastReadTime(
                    chatRequest.senderId(),
                    roomId,
                    chatRequest.createdAt()
            );
            log.debug("비동기 메시지 저장 완료: senderId={}, roomId={}",
                    chatRequest.senderId(), roomId);
        } catch (Exception e) {
            log.error("비동기 메시지 저장 실패: roomId={}, error={}", roomId, e.getMessage(), e);
            // 실패해도 클라이언트는 이미 메시지를 받았으므로 별도 처리
        }
    }

    @Transactional(readOnly = true)
    public ChatPageResponse getChatsByCursor(
            Long roomId, LocalDateTime cursor, int size
    ) {

        Long memberId = getMemberIdFromAuthentication();
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

        if (memberId != null && !data.isEmpty()) {
            LocalDateTime latestMessageTime = data.stream()
                    .map(ChatResponse::createdAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());
            unreadMessageService.updateLastReadTime(memberId, roomId, latestMessageTime);
            log.debug("채팅 조회 시 읽은 시간 업데이트: memberId={}, roomId={}, time={}",
                    memberId, roomId, latestMessageTime);
        }

        return new ChatPageResponse(data, nextCursor);
    }

    private Long getMemberIdFromAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof ChatMemberDetails userDetails) {
            return userDetails.getMemberId();
        }
        log.warn("Authentication not found or invalid principal");
        throw new IllegalStateException("인증 정보가 없습니다.");
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
