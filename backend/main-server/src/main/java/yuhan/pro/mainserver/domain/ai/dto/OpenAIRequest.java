package yuhan.pro.mainserver.domain.ai.dto;

import java.util.List;

public record OpenAIRequest(
    String model,
    List<Message> messages,
    double temperature,
    int max_tokens
) {
    public record Message(
        String role,
        String content
    ) {}
    
    public static OpenAIRequest createDeveloperAssistant(String userQuestion) {
        String systemPrompt = """
                당신은 개발자들을 위한 전문 AI 멘토입니다.
                개발 관련 질문에 명확하고 정확하게 답변해주세요.
                코드 예제가 필요한 경우 적절한 언어로 제공해주세요.
                복잡한 개념은 쉽게 설명하고, 실용적인 조언을 제공해주세요.
                한국어로 답변해주세요.
                """;
        
        return new OpenAIRequest(
                "gpt-3.5-turbo",
                List.of(
                        new Message("system", systemPrompt),
                        new Message("user", userQuestion)
                ),
                0.7,
                2000
        );
    }
}


