package yuhan.pro.chatserver.domain.entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import yuhan.pro.chatserver.sharedkernel.entity.BaseEntity;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoom extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long organizationId;

  @Column(unique = true)
  private String name;
  private String description; // 채팅방 설명

  @Enumerated(EnumType.STRING)
  private RoomType type;

  private Long ownerId; // 방장 ID

  @Builder.Default
  private Integer messageCount = 0;

  @Builder.Default
  private LocalDateTime lastMessageAt = LocalDateTime.now();

  @Builder.Default
  private String lastMessage = "";

  // PRIVATE ROOM ONLY
  @Column(nullable = true)
  private String password;

  @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ChatRoomMember> members = new ArrayList<>();

  public boolean isPrivate() {
    return this.type == RoomType.PRIVATE;
  }

  public boolean matchesPassword(String rawPassword, PasswordEncoder encoder) {
    if (this.password == null || rawPassword == null) {
      return false;
    }
    return encoder.matches(rawPassword, this.password);
  }
}
