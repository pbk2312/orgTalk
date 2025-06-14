package yuhan.pro.chatserver.domain.entity;

public enum MessageType {
  TEXT,   // 일반 텍스트 메시지
  CODE,   // 코드 블록 메시지
  CHAT,   // 일반 채팅 메시지
  JOIN,   // 채팅방 입장 알림
  LEAVE   // 채팅방 퇴장 알림
}
