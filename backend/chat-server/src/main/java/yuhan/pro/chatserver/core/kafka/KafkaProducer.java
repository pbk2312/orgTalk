package yuhan.pro.chatserver.core.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import yuhan.pro.chatserver.domain.dto.ChatRequest;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class KafkaProducer {

  private final KafkaTemplate<String, ChatRequest> kafkaTemplate;

  public void send(String topic, String roomId, ChatRequest message) {
    kafkaTemplate.send(topic, roomId, message);
  }
}
