package yuhan.pro.chatserver.domain.dto;

import yuhan.pro.chatserver.domain.entity.Language;
import yuhan.pro.chatserver.domain.entity.MessageType;

public record ChatRequest(
    String message,
    MessageType messageType,
    String codeContent,
    Language language,
    Long senderId,
    String senderName
) {

}
