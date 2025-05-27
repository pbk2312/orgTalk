package yuhan.pro.mainserver.domain.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubOrgDto(
    Long id,
    String login,
    @JsonProperty("avatar_url")
    String avatarUrl
) {

}
