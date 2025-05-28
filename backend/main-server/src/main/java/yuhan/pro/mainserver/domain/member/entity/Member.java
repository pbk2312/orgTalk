package yuhan.pro.mainserver.domain.member.entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yuhan.pro.mainserver.domain.organization.entity.Organization;
import yuhan.pro.mainserver.sharedkernel.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;


  private String name;

  @Column(unique = true)
  private Long githubId;

  @Column(unique = true)
  private String login;

  @Column(unique = true)
  private String email;

  private String avatarUrl;

  @Enumerated(EnumType.STRING)
  private MemberRole memberRole;

  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private Set<Organization> organizations = new HashSet<>();

  @Builder
  public Member(String login, String name, String email, String avatarUrl, MemberRole memberRole,
      Long githubId) {
    this.login = login;
    this.name = name;
    this.email = email;
    this.avatarUrl = avatarUrl;
    this.memberRole = memberRole;
    this.githubId = githubId;
  }

  public void setOrganizations(Set<Organization> organizations) {
    this.organizations.clear();
    this.organizations.addAll(organizations);
  }

  public void clearOrganizations() {
    this.organizations.clear();
  }

  public void addOrganization(Organization organization) {
    this.organizations.add(organization);
  }
}
