package yuhan.pro.chatserver.sharedkernel.infra.socket;


public final class ChatConstants {

    private ChatConstants() {
    }

    public static final String STOMP_TOPIC_PREFIX = "/topic/";
    public static final String PRESENCE_DOT_PREFIX = STOMP_TOPIC_PREFIX + "presence.";
    public static final String ROOMS_DOT_PREFIX = STOMP_TOPIC_PREFIX + "rooms.";
    public static final String PRESENCE_SLASH_PREFIX = STOMP_TOPIC_PREFIX + "presence/";
    public static final String ROOMS_SLASH_PREFIX = STOMP_TOPIC_PREFIX + "rooms/";
    public static final String PRESENCE_KEY_PREFIX = "chatroom:presence:";
}
