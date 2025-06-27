package yuhan.pro.chatserver.sharedkernel.infra.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import yuhan.pro.chatserver.domain.dto.ChatRequest;
import yuhan.pro.chatserver.domain.service.ChatService;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaListeners {

  private final SimpMessageSendingOperations messagingTemplate;
  private final ChatService chatService;

  @KafkaListener(
      topics = "chat-messages",
      groupId = "${spring.application.name}-${random.uuid}",
      containerFactory = "chatMessageFactory"
  )
  public void handleChatMessage(
      ChatRequest message,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.RECEIVED_KEY) String roomId
  ) {
    try {

      chatService.saveChat(message, Long.valueOf(roomId));
      log.info("DB 저장 완료 - roomId: {}", roomId);

      messagingTemplate.convertAndSend("/topic/rooms/" + roomId, message);
      log.info("WebSocket 전송 완료 - roomId: {}", roomId);

    } catch (Exception e) {
      log.error("메시지 처리 실패 - roomId: {}", roomId, e);
    }
  }

  @KafkaListener(
      topics = "chat-presence",
      groupId = "${spring.kafka.consumer.group-id}-${random.uuid}",
      containerFactory = "presenceFactory"
  )
  public void handlePresenceEvent(
      String payload,
      @Header(KafkaHeaders.RECEIVED_KEY) String roomId,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
  ) {
    log.info("Kafka Presence 수신 - roomId: {}, partition: {}, payload: {}",
        roomId, partition, payload);

    messagingTemplate.convertAndSend("/topic/presence/" + roomId, payload);
  }
}
