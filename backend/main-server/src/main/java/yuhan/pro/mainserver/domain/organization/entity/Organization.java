package yuhan.pro.mainserver.domain.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import yuhan.pro.mainserver.domain.member.entity.Member;
import yuhan.pro.mainserver.sharedkernel.entity.BaseEntity;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Organization extends BaseEntity {

  @Id
  private Long id;

  @Column(nullable = false, unique = true)
  private String login;

  @Column(nullable = true)
  private String avatarUrl;

  @ManyToMany(mappedBy = "organizations", fetch = FetchType.LAZY)
  private Set<Member> members = new HashSet<>();
}
