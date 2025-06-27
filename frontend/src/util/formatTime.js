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


export const formatKoreaTime = (timestamp) => {
  try {
    const date = new Date(timestamp);
    

    if (isNaN(date.getTime())) {
      return '시간 오류';
    }

    const hours = date.getHours();
    const minutes = date.getMinutes();
    

    const period = hours < 12 ? '오전' : '오후';
    

    const displayHours = hours === 0 ? 12 : hours > 12 ? hours - 12 : hours;
    

    const displayMinutes = minutes.toString().padStart(2, '0');
    
    return `${period} ${displayHours}:${displayMinutes}`;
  } catch (error) {
    console.error('시간 포맷 오류:', error);
    return '시간 오류';
  }
};


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