package yuhan.pro.chatserver.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yuhan.pro.chatserver.sharedkernel.entity.BaseEntity;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Chat extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "chat_room_id", nullable = false)
  private ChatRoom room;

  private String senderName;

  private Long senderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private MessageType type;

  @Column(columnDefinition = "TEXT")
  private String message;

  @Lob // 코드 내용을 Lob 으로 저장
  @Column(name = "code_content")
  private String codeContent;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private Language language;

  @Builder
  public Chat(ChatRoom room, String senderName, Long senderId, String message) {
    this.room = room;
    this.senderName = senderName;
    this.senderId = senderId;
    this.message = message;
  }

  public static Chat createChat(ChatRoom room, String senderName, Long senderId, String message) {
    return Chat.builder()
        .room(room)
        .senderName(senderName)
        .senderId(senderId)
        .message(message)
        .build();
  }
}
