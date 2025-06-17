package yuhan.pro.chatserver.core.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import yuhan.pro.chatserver.domain.dto.ChatRequest;

@Component
@RequiredArgsConstructor
public class KafkaProducer {

  private final KafkaTemplate<String, ChatRequest> chatRequestKafkaTemplate;
  private final KafkaTemplate<String, String> stringKafkaTemplate;

  public void send(String topic, String roomId, ChatRequest message) {
    chatRequestKafkaTemplate.send(topic, roomId, message);
  }

  public void send(String topic, String roomId, String message) {
    stringKafkaTemplate.send(topic, roomId, message);
  }
}
