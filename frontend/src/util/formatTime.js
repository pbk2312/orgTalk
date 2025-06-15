// src/util/formatTime.js

/**
 * 주어진 ISO 타임스탬프를 받아서 상대적 시간(방금 전, 몇 분 전, 몇 시간 전) 또는 날짜 문자열로 반환합니다.
 * @param {string} iso - ISO 8601 형식의 시간 문자열 (예: "2025-06-15T12:34:56.789Z")
 * @returns {string} 상대적 시간 또는 날짜 문자열
 */
export const formatTime = (iso) => {
  try {
    const date = new Date(iso);
    if (isNaN(date.getTime())) {
      // 유효하지 않은 날짜인 경우 원본이나 빈 문자열 반환
      return iso;
    }
    const diffMinutes = Math.floor((Date.now() - date.getTime()) / 60000);
    if (diffMinutes < 1) return '방금 전';
    if (diffMinutes < 60) return `${diffMinutes}분 전`;
    if (diffMinutes < 1440) return `${Math.floor(diffMinutes / 60)}시간 전`;
    // 24시간(1440분) 이상이면 로컬 날짜 형식으로 표시
    return date.toLocaleDateString();
  } catch (err) {
    console.error('formatTime error:', err);
    return iso;
  }
};
