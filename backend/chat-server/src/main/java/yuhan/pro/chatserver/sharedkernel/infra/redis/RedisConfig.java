package yuhan.pro.chatserver.sharedkernel.infra.redis;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, Object> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);
        tpl.setKeySerializer(new StringRedisSerializer());
        tpl.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return tpl;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    @Bean
    public ThreadPoolTaskExecutor redisPubSubExecutor() {
        ThreadPoolTaskExecutor tp = new ThreadPoolTaskExecutor();
        tp.setCorePoolSize(100);      // 50 -> 100
        tp.setMaxPoolSize(500);        // 200 -> 500
        tp.setQueueCapacity(10000);    // 2000 -> 10000
        tp.setThreadNamePrefix("redis-pubsub-");
        tp.setRejectedExecutionHandler(
                new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        tp.initialize();
        return tp;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory, ThreadPoolTaskExecutor redisPubSubExecutor) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // Redis 수신 및 메시지 처리에 전용 executor 사용
        container.setTaskExecutor(redisPubSubExecutor);
        // 구독 관리에도 동일 executor 사용(필요 시 분리 가능)
        container.setSubscriptionExecutor(redisPubSubExecutor);

        // 성능 최적화 설정
        container.setMaxSubscriptionRegistrationWaitingTime(5000L); // 구독 대기 시간
        container.setRecoveryInterval(10000L); // 재연결 간격

        return container;
    }
}
