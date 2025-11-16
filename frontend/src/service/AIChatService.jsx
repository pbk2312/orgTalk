import { api } from '../lib/axios.ts';

class AIChatService {
  /**
   * AI 멘토에게 질문하기
   * @param {string} question - 사용자 질문
   * @returns {Promise<Object>} - AI 응답
   */
  async askQuestion(question) {
    try {
      const response = await api.post('/ai/chat', {
        question: question
      });
      return response.data;
    } catch (error) {
      console.error('AI 챗봇 질문 실패:', error);
      throw error;
    }
  }

  /**
   * AI 서비스 상태 확인
   * @returns {Promise<string>}
   */
  async checkHealth() {
    try {
      const response = await api.get('/ai/health');
      return response.data;
    } catch (error) {
      console.error('AI 서비스 상태 확인 실패:', error);
      throw error;
    }
  }
}

export default new AIChatService();

