package yuhan.pro.chatserver.sharedkernel.infra.redis;


import lombok.experimental.UtilityClass;

@UtilityClass
public final class RedisConstants {

    public static final String CHAT_CHANNEL_PREFIX = "chat:room:";
    public static final String PRESENCE_CHANNEL_PREFIX = "presence:room:";
    public static final String CHAT_CHANNEL_PATTERN = "chat:room:*";
    public static final String PRESENCE_CHANNEL_PATTERN = "presence:room:*";

    public static final String CTX_CHAT = "채팅 메시지";
    public static final String CTX_PRESENCE = "상태 업데이트";
}

