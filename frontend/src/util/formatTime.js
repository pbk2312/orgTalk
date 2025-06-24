// src/util/formatTime.js

export const formatTime = (iso) => {
  try {
    const date = new Date(iso);
    if (isNaN(date.getTime())) {
      return iso;
    }
    const diffMinutes = Math.floor((Date.now() - date.getTime()) / 60000);
    if (diffMinutes < 1) return '방금 전';
    if (diffMinutes < 60) return `${diffMinutes}분 전`;
    if (diffMinutes < 1440) return `${Math.floor(diffMinutes / 60)}시간 전`;
    return date.toLocaleDateString();
  } catch (err) {
    console.error('formatTime error:', err);
    return iso;
  }
};


/**
 * 한국어 형식으로 시간을 포맷합니다
 * @param {string|Date|number} timestamp - 타임스탬프
 * @returns {string} 포맷된 시간 문자열 (예: "오전 11:30", "오후 2:45")
 */
export const formatKoreaTime = (timestamp) => {
  try {
    const date = new Date(timestamp);
    
    // 유효한 날짜인지 확인
    if (isNaN(date.getTime())) {
      return '시간 오류';
    }

    const hours = date.getHours();
    const minutes = date.getMinutes();
    
    // 오전/오후 구분
    const period = hours < 12 ? '오전' : '오후';
    
    // 12시간 형식으로 변환 (0시 -> 12시, 13시 -> 1시)
    const displayHours = hours === 0 ? 12 : hours > 12 ? hours - 12 : hours;
    
    // 분이 한 자리수일 때 0 패딩
    const displayMinutes = minutes.toString().padStart(2, '0');
    
    return `${period} ${displayHours}:${displayMinutes}`;
  } catch (error) {
    console.error('시간 포맷 오류:', error);
    return '시간 오류';
  }
};

/**
 * 메시지 날짜를 표시용으로 포맷합니다 (날짜가 다를 때 사용)
 * @param {string|Date|number} timestamp - 타임스탬프
 * @returns {string} 포맷된 날짜 문자열 (예: "2024년 1월 15일")
 */
export const formatDate = (timestamp) => {
  try {
    const date = new Date(timestamp);
    
    if (isNaN(date.getTime())) {
      return '날짜 오류';
    }

    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();
    
    return `${year}년 ${month}월 ${day}일`;
  } catch (error) {
    console.error('날짜 포맷 오류:', error);
    return '날짜 오류';
  }
};

/**
 * 오늘인지 확인합니다
 * @param {string|Date|number} timestamp - 타임스탬프
 * @returns {boolean} 오늘이면 true
 */
export const isToday = (timestamp) => {
  try {
    const date = new Date(timestamp);
    const today = new Date();
    
    return date.getDate() === today.getDate() &&
           date.getMonth() === today.getMonth() &&
           date.getFullYear() === today.getFullYear();
  } catch (error) {
    return false;
  }
};

/**
 * 어제인지 확인합니다
 * @param {string|Date|number} timestamp - 타임스탬프
 * @returns {boolean} 어제면 true
 */
export const isYesterday = (timestamp) => {
  try {
    const date = new Date(timestamp);
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    
    return date.getDate() === yesterday.getDate() &&
           date.getMonth() === yesterday.getMonth() &&
           date.getFullYear() === yesterday.getFullYear();
  } catch (error) {
    return false;
  }
};

/**
 * 상대적 시간을 표시합니다 (예: "오늘", "어제", "1월 15일")
 * @param {string|Date|number} timestamp - 타임스탬프
 * @returns {string} 상대적 날짜 문자열
 */
export const formatRelativeDate = (timestamp) => {
  if (isToday(timestamp)) {
    return '오늘';
  } else if (isYesterday(timestamp)) {
    return '어제';
  } else {
    const date = new Date(timestamp);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    return `${month}월 ${day}일`;
  }
};