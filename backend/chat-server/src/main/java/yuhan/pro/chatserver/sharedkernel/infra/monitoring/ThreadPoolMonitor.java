package yuhan.pro.chatserver.sharedkernel.infra.monitoring;

import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ThreadPoolMonitor {

    private final ThreadPoolTaskExecutor redisPubSubExecutor;
    private final ThreadPoolTaskExecutor chatSaveExecutor;

    public ThreadPoolMonitor(
            @Qualifier("redisPubSubExecutor") ThreadPoolTaskExecutor redisPubSubExecutor,
            @Qualifier("chatSaveExecutor") ThreadPoolTaskExecutor chatSaveExecutor) {
        this.redisPubSubExecutor = redisPubSubExecutor;
        this.chatSaveExecutor = chatSaveExecutor;
    }

    @Scheduled(fixedRate = 30000)
    public void logThreadPoolStatus() {
        logExecutorStatus("RedisPubSub", redisPubSubExecutor);
        logExecutorStatus("ChatSave", chatSaveExecutor);
    }

    private void logExecutorStatus(String name, ThreadPoolTaskExecutor executor) {
        ThreadPoolExecutor threadPool = executor.getThreadPoolExecutor();

        int activeCount = threadPool.getActiveCount();
        int poolSize = threadPool.getPoolSize();
        int maxPoolSize = threadPool.getMaximumPoolSize();
        long completedTaskCount = threadPool.getCompletedTaskCount();
        int queueSize = threadPool.getQueue().size();
        int queueRemainingCapacity = threadPool.getQueue().remainingCapacity();

        log.info("[{}] active={}/{}/{}, queue={}/{}, completed={}",
                name,
                activeCount,
                poolSize,
                maxPoolSize,
                queueSize,
                queueSize + queueRemainingCapacity,
                completedTaskCount);

        // 경고: 큐가 80% 이상 찬 경우
        if (queueRemainingCapacity < (queueSize + queueRemainingCapacity) * 0.2) {
            log.warn("[{}] 큐가 거의 가득 찼습니다! queue={}/{}",
                    name, queueSize, queueSize + queueRemainingCapacity);
        }

        if (poolSize >= maxPoolSize * 0.9) {
            log.warn("[{}] 스레드 풀이 거의 최대치입니다! poolSize={}/{}",
                    name, poolSize, maxPoolSize);
        }
    }
}

