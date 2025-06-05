package yuhan.pro.mainserver.domain.member.repository;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import yuhan.pro.mainserver.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByEmail(String email);

  Set<Member> findAllByIdIn(Set<Long> ids);
}
