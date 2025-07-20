package yuhan.pro.chatserver.sharedkernel.infra.rabbitMQ;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@RequiredArgsConstructor
@Configuration
public class RabbitMQConfig {

    private static final String STOMP_EXCHANGE = "amq.topic";

    private static final String CHAT_ROUTING_KEY = "chat.#";
    private static final String CHAT_QUEUE_NAME = "chat.queue";

    private static final String PRESENCE_ROUTING_KEY = "presence.#";
    private static final String PRESENCE_QUEUE_NAME = "presence.queue";

    @Value("${spring.rabbitmq.host}")
    private String host;
    @Value("${spring.rabbitmq.port}")
    private int port;
    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost("/");
        return factory;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public Queue chatQueue() {
        return new Queue(CHAT_QUEUE_NAME, true);
    }

    @Bean
    public Queue presenceQueue() {
        return new Queue(PRESENCE_QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange stompExchange() {
        TopicExchange exchange = new TopicExchange(STOMP_EXCHANGE);
        exchange.setIgnoreDeclarationExceptions(true);
        return exchange;
    }

    @Bean
    public Binding chatBinding() {
        return BindingBuilder.bind(chatQueue())
                .to(stompExchange())
                .with(CHAT_ROUTING_KEY);
    }

    @Bean
    public Binding presenceBinding() {
        return BindingBuilder.bind(presenceQueue())
                .to(stompExchange())
                .with(PRESENCE_ROUTING_KEY);
    }

    @Bean
    public Binding specificChatBinding() {
        return BindingBuilder.bind(chatQueue())
                .to(stompExchange())
                .with("topic.chat.1");
    }
}
