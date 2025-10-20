package yuhan.pro.mainserver.domain.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
    @NotBlank(message = "질문을 입력해주세요")
    String question
) {
}


