package yuhan.pro.chatserver.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yuhan.pro.chatserver.sharedkernel.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoom extends BaseEntity {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long organizationId;

  private String name;
  private String description; // 채팅방 설명

  @Enumerated(EnumType.STRING)
  private RoomType type;

  private Long ownerId; // 방장 ID

  private Integer messageCount;
  private LocalDateTime lastMessageAt;
  private String lastMessage;

  @Builder
  public ChatRoom(Long organizationId, String name, String description, RoomType type,
      Long ownerId) {
    this.organizationId = organizationId;
    this.name = name;
    this.description = description;
    this.type = type;
    this.ownerId = ownerId;
  }
}
