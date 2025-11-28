package yuhan.pro.chatserver.sharedkernel.infra.socket;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AsyncMessageSender {

    private final SimpMessagingTemplate messagingTemplate;
    private final Executor redisPubSubExecutor;

    public AsyncMessageSender(SimpMessagingTemplate messagingTemplate,
            @Qualifier("redisPubSubExecutor") Executor redisPubSubExecutor) {
        this.messagingTemplate = messagingTemplate;
        this.redisPubSubExecutor = redisPubSubExecutor;
    }

    public void sendAsync(String destination, Object payload) {
        CompletableFuture.runAsync(() -> {
            try {
                messagingTemplate.convertAndSend(destination, payload);
            } catch (Exception e) {
                log.error("비동기 메시지 전송 실패: destination={}, error={}",
                        destination, e.getMessage(), e);
            }
        }, redisPubSubExecutor);
    }
}

