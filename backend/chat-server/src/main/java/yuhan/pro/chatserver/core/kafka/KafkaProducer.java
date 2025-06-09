package yuhan.pro.chatserver.core.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import yuhan.pro.chatserver.domain.dto.ChatRequest;

@Component
@RequiredArgsConstructor
public class KafkaProducer {

  private final KafkaTemplate<String, ChatRequest> kafkaTemplate;


  /**
   * Kafka 토픽에 메시지를 전송
   *
   * @param topic   메시지를 보낼 Kafka 토픽명
   * @param message 전송할 메시지 객체 (JSON으로 자동 직렬화됨)
   */
  public void send(String topic, ChatRequest message) {
    kafkaTemplate.send(topic, message);
  }
}
