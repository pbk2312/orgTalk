package yuhan.pro.chatserver.core.kafka;

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
      log.info("Kafka 수신 - roomId: {}, partition: {}, message: {}", roomId, partition, message);
      messagingTemplate.convertAndSend("/topic/rooms/" + roomId, message);
      chatService.saveChat(message, Long.valueOf(roomId));
    } catch (Exception e) {
      log.error("Kafka 메시지 처리 오류", e);
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
