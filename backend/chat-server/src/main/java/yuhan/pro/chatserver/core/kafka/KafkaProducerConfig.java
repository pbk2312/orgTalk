package yuhan.pro.chatserver.core.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import yuhan.pro.chatserver.domain.dto.ChatRequest;

@Configuration
public class KafkaProducerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  public Map<String, Object> producerConfig() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return props;
  }

  @Bean
  public ProducerFactory<String, ChatRequest> chatRequestProducerFactory() {
    return new DefaultKafkaProducerFactory<>(
        producerConfig(),
        new StringSerializer(),
        new JsonSerializer<ChatRequest>()
    );
  }

  @Bean
  public KafkaTemplate<String, ChatRequest> chatRequestKafkaTemplate(
      ProducerFactory<String, ChatRequest> chatRequestProducerFactory) {
    return new KafkaTemplate<>(chatRequestProducerFactory);
  }
}
