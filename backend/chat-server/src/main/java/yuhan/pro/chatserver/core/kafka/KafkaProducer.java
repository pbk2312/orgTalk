package yuhan.pro.chatserver.core.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import yuhan.pro.chatserver.domain.dto.ChatRequest;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {

  private final KafkaTemplate<String, ChatRequest> chatRequestKafkaTemplate;
  private final KafkaTemplate<String, String> stringKafkaTemplate;

  public void send(String topic, String roomId, ChatRequest message) {
    try {
      chatRequestKafkaTemplate.send(topic, roomId, message);
      log.info("카프카 전송 성공 - roomId: {}", roomId);
    } catch (Exception e) {
      log.error("카프카 전송 실패 - roomId: {} (DB는 이미 저장됨)", roomId, e);
    }
  }

  public void send(String topic, String roomId, String message) {
    stringKafkaTemplate.send(topic, roomId, message);
  }
}
