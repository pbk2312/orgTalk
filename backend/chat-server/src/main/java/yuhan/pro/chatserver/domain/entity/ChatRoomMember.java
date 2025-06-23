package yuhan.pro.chatserver.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yuhan.pro.chatserver.sharedkernel.entity.BaseEntity;

@Entity
@Table(
    name = "chat_room_member",
    indexes = {
        @Index(name = "idx_chat_room_member_chat_room_id", columnList = "chat_room_id"),
        @Index(name = "idx_chat_room_member_member_id", columnList = "member_id"),
        @Index(name = "idx_crm_memberid_chatroomid", columnList = "member_id, chat_room_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoomMember extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_room_id", nullable = false)
  private ChatRoom chatRoom;

  private Long memberId;
}
