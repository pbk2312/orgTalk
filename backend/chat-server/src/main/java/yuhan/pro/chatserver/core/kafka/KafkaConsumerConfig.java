package yuhan.pro.chatserver.core.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import yuhan.pro.chatserver.domain.dto.ChatRequest;

@Configuration
public class KafkaConsumerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  public Map<String, Object> consumerConfig(String groupId) {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    return props;
  }

  // ------------ chat-messages 전용 ------------
  @Bean
  public ConsumerFactory<String, ChatRequest> chatRequestConsumerFactory() {
    JsonDeserializer<ChatRequest> deserializer = new JsonDeserializer<>(ChatRequest.class);
    deserializer.addTrustedPackages("yuhan.pro.chatserver.domain.dto");
    return new DefaultKafkaConsumerFactory<>(
        consumerConfig("${spring.application.name}-${random.uuid}"),
        new StringDeserializer(),
        deserializer
    );
  }

  @Bean(name = "factory")
  public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, ChatRequest>> factory(
      ConsumerFactory<String, ChatRequest> chatRequestConsumerFactory) {
    ConcurrentKafkaListenerContainerFactory<String, ChatRequest> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(chatRequestConsumerFactory);
    return factory;
  }

  // ------------ presence 전용 ------------
  @Bean
  public ConsumerFactory<String, String> presenceConsumerFactory(
      @Value("${spring.kafka.consumer.group-id}") String groupId) {
    // 그룹 아이디 분리 (옵션)
    String presenceGroup = groupId + "-presence";
    return new DefaultKafkaConsumerFactory<>(
        consumerConfig(presenceGroup),
        new StringDeserializer(),
        new StringDeserializer()
    );
  }

  @Bean(name = "presenceKafkaListenerContainerFactory")
  public ConcurrentKafkaListenerContainerFactory<String, String> presenceKafkaListenerContainerFactory(
      ConsumerFactory<String, String> presenceConsumerFactory
  ) {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(presenceConsumerFactory);
    return factory;
  }
}
