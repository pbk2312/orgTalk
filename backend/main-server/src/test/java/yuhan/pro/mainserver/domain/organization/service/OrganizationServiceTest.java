package yuhan.pro.mainserver.domain.organization.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.ORGANIZATION_NOT_FOUND;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yuhan.pro.mainserver.domain.organization.dto.OrganizationsInfoResponse;
import yuhan.pro.mainserver.domain.organization.repository.OrganizationRepository;
import yuhan.pro.mainserver.sharedkernel.exception.CustomException;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrganizationService: getOrganizationInfo 메서드")
class OrganizationServiceTest {

  @Mock
  private OrganizationRepository repository;

  @InjectMocks
  private OrganizationService service;

  private final Long ORG_ID = 1L;

  @Nested
  @DisplayName("조직 조회에 성공할 때")
  class GetOrganizationInfoTests {

    @Test
    @DisplayName("DTO가 반환")
    void returnsResponseDTO() {
      // given
      OrganizationsInfoResponse expected =
          new OrganizationsInfoResponse(ORG_ID, "login123", "http://avatar", 5L);
      when(repository.findInfoWithMemberCount(ORG_ID))
          .thenReturn(Optional.of(expected));

      // when
      OrganizationsInfoResponse actual = service.getOrganizationInfo(ORG_ID);

      // then
      assertThat(actual).isNotNull();
      assertThat(actual.id()).isEqualTo(expected.id());
      assertThat(actual.login()).isEqualTo(expected.login());
      assertThat(actual.avatarUrl()).isEqualTo(expected.avatarUrl());
      assertThat(actual.memberCount()).isEqualTo(expected.memberCount());
    }
  }

  @Test
  @DisplayName("CustomException이 발생한다")
  void throwsCustomException() {
    // given
    when(repository.findInfoWithMemberCount(ORG_ID))
        .thenReturn(Optional.empty());

    // when & then
    CustomException ex = assertThrows(
        CustomException.class,
        () -> service.getOrganizationInfo(ORG_ID)
    );
    assertThat(ex.getExceptionCode()).isEqualTo(ORGANIZATION_NOT_FOUND);
  }
}

