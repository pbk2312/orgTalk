package yuhan.pro.chatserver;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ChatServerApplicationTests {


    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() {
    }

}
