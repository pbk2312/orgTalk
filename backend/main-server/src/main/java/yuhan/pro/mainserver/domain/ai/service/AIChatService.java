package yuhan.pro.mainserver.domain.ai.service;

import static yuhan.pro.mainserver.domain.ai.constants.Aiconstants.API_KEY_NOT_SET;
import static yuhan.pro.mainserver.domain.ai.constants.Aiconstants.NULL_RESPONSE;
import static yuhan.pro.mainserver.domain.ai.constants.Aiconstants.OPENAI_API_URL;
import static yuhan.pro.mainserver.domain.ai.constants.Aiconstants.TEMPORARY_ERROR;
import static yuhan.pro.mainserver.domain.ai.constants.Aiconstants.UNEXPECTED_ERROR;

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

    @Value("${openai.api.key:}")
    private String apiKey;

    public AIChatService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(OPENAI_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ChatResponse getAnswer(ChatRequest request) {
        if (isApiKeyInvalid()) {
            log.warn("OpenAI API 키가 설정되지 않았습니다.");
            return new ChatResponse(API_KEY_NOT_SET);
        }

        try {
            log.info("OpenAI API 호출 시작 - 질문: {}", request.question());
            OpenAIRequest openAIRequest = createOpenAIRequest(request.question());
            OpenAIResponse openAIResponse = callOpenAI(openAIRequest);
            return buildChatResponse(openAIResponse);
        } catch (WebClientResponseException e) {
            return handleWebClientError(e);
        } catch (Exception e) {
            return handleUnexpectedError(e);
        }
    }

    private boolean isApiKeyInvalid() {
        return apiKey == null || apiKey.trim().isEmpty();
    }

    private OpenAIRequest createOpenAIRequest(String question) {
        return OpenAIRequest.createDeveloperAssistant(question);
    }

    private OpenAIResponse callOpenAI(OpenAIRequest request) {
        return webClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAIResponse.class)
                .block();
    }

    private ChatResponse buildChatResponse(OpenAIResponse response) {
        if (response == null) {
            log.error("OpenAI API 응답이 null입니다.");
            return new ChatResponse(NULL_RESPONSE);
        }

        log.info("OpenAI API 호출 성공 - 사용 토큰: {}", response.getTotalTokens());
        return new ChatResponse(response.getContent(), response.getTotalTokens());
    }

    private ChatResponse handleWebClientError(WebClientResponseException e) {
        log.error("OpenAI API 호출 실패 - 상태 코드: {}, 응답: {}", e.getStatusCode(),
                e.getResponseBodyAsString());
        return new ChatResponse(TEMPORARY_ERROR);
    }

    private ChatResponse handleUnexpectedError(Exception e) {
        log.error("OpenAI API 호출 중 예외 발생", e);
        return new ChatResponse(UNEXPECTED_ERROR);
    }
}
