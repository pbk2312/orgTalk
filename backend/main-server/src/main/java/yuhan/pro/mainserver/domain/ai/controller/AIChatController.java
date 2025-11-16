package yuhan.pro.mainserver.domain.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import yuhan.pro.mainserver.domain.ai.dto.ChatRequest;
import yuhan.pro.mainserver.domain.ai.dto.ChatResponse;
import yuhan.pro.mainserver.domain.ai.service.AIChatService;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Chat", description = "AI 멘토 챗봇 API")
public class AIChatController {

    private final AIChatService aiChatService;

    @PostMapping("/chat")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "AI 멘토에게 질문하기", description = "개발 관련 질문을 하면 AI가 답변")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        log.info("AI 챗봇 질문 요청 - 질문: {}", request.question());
        return aiChatService.getAnswer(request);
    }

    @GetMapping("/health")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "AI 서비스 상태 확인")
    public String health() {
        return "AI Chat Service is running";
    }
}


