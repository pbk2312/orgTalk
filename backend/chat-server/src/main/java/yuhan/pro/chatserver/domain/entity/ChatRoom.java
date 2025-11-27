package yuhan.pro.chatserver.domain.entity;


import static yuhan.pro.chatserver.sharedkernel.exception.ExceptionCode.PRIVATE_ROOM_PASSWORD_IS_EMPTY;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import yuhan.pro.chatserver.sharedkernel.entity.BaseEntity;
import yuhan.pro.chatserver.sharedkernel.exception.CustomException;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        indexes = {
                @Index(name = "idx_chatroom_type", columnList = "type"),
                @Index(
                        name = "idx_chatroom_type_created_updated",
                        columnList = "type, createdAt, updatedAt"
                )
        }
)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private RoomType type;

    private Long ownerId;

    @Column(nullable = true)
    private String password;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
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

    public void updateRoom(String newName,
            String newDescription,
            RoomType newType,
            String rawPassword,
            PasswordEncoder encoder) {
        this.name = newName;
        this.description = newDescription;

        if (this.type != newType) {
            this.type = newType;
            if (newType == RoomType.PRIVATE) {
                if (rawPassword == null || rawPassword.isEmpty()) {
                    throw new CustomException(PRIVATE_ROOM_PASSWORD_IS_EMPTY);
                }
                this.password = encoder.encode(rawPassword);
            } else {
                this.password = null;
            }
        } else if (newType == RoomType.PRIVATE && rawPassword != null && !rawPassword.isEmpty()) {
            this.password = encoder.encode(rawPassword);
        }
    }
}
