package yuhan.pro.mainserver.domain.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import yuhan.pro.mainserver.domain.ai.dto.ChatRequest;
import yuhan.pro.mainserver.domain.ai.dto.ChatResponse;
import yuhan.pro.mainserver.domain.ai.dto.OpenAIRequest;
import yuhan.pro.mainserver.domain.ai.dto.OpenAIResponse;

@Slf4j
@Service
public class AIChatService {

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public AIChatService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(OPENAI_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ChatResponse getAnswer(ChatRequest request) {
        try {
            log.info("OpenAI API 호출 시작 - 질문: {}", request.question());

            OpenAIRequest openAIRequest = OpenAIRequest.createDeveloperAssistant(
                    request.question());

            OpenAIResponse response = webClient.post()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(openAIRequest)
                    .retrieve()
                    .bodyToMono(OpenAIResponse.class)
                    .block();

            if (response == null) {
                log.error("OpenAI API 응답이 null입니다");
                return new ChatResponse("죄송합니다. 응답을 받지 못했습니다. 다시 시도해주세요.");
            }

            String answer = response.getContent();
            int tokensUsed = response.getTotalTokens();

            log.info("OpenAI API 호출 성공 - 사용 토큰: {}", tokensUsed);

            return new ChatResponse(answer, tokensUsed);

        } catch (WebClientResponseException e) {
            log.error("OpenAI API 호출 실패 - 상태 코드: {}, 응답: {}", e.getStatusCode(),
                    e.getResponseBodyAsString());
            return new ChatResponse("죄송합니다. AI 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 예외 발생", e);
            return new ChatResponse("죄송합니다. 예상치 못한 오류가 발생했습니다. 관리자에게 문의해주세요.");
        }
    }
}
