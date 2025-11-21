package yuhan.pro.mainserver.domain.ai.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Aiconstants {

    public static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    public static final String API_KEY_NOT_SET = "AI 서비스가 설정되지 않았습니다. 관리자에게 문의해주세요.";
    public static final String TEMPORARY_ERROR = "죄송합니다. AI 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
    public static final String UNEXPECTED_ERROR = "죄송합니다. 예상치 못한 오류가 발생했습니다. 관리자에게 문의해주세요.";
    public static final String NULL_RESPONSE = "죄송합니다. 응답을 받지 못했습니다. 다시 시도해주세요.";
}
