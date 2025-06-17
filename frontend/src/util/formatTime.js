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
