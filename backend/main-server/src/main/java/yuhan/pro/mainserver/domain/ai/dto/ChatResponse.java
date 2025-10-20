package yuhan.pro.mainserver.domain.ai.dto;

public record ChatResponse(
    String answer,
    long tokensUsed
) {
    public ChatResponse(String answer) {
        this(answer, 0);
    }
}


