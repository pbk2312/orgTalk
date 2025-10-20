package yuhan.pro.mainserver.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAIResponse(
    List<Choice> choices,
    Usage usage
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(
        Message message,
        int index
    ) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(
        String role,
        String content
    ) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(
        int prompt_tokens,
        int completion_tokens,
        int total_tokens
    ) {}
    
    public String getContent() {
        if (choices != null && !choices.isEmpty()) {
            return choices.get(0).message().content();
        }
        return "";
    }
    
    public int getTotalTokens() {
        return usage != null ? usage.total_tokens() : 0;
    }
}


