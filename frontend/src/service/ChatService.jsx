// src/lib/ChatService.jsx
import { chatApi } from '../lib/axios.ts'; 

/**
 * ChatRoom 생성 요청용 타입 (JSDoc 용)
 * @typedef {Object} ChatRoomCreateRequest
 * @property {number} organizationId     - 백엔드가 요구하는 조직 ID
 * @property {string} name               - 채팅방 이름 (필수)
 * @property {string} [description]      - 채팅방 설명 (선택)
 * @property {"PUBLIC" | "PRIVATE"} type  - RoomType enum 값
 */

/**
 * 채팅방 생성 API를 호출합니다.
 * @param {ChatRoomCreateRequest} payload
 * @returns {Promise<any>} 생성을 성공하면 백엔드가 리턴하는 데이터(oauth 없이 Void 생략 → 필요 시 변경)
 */
export async function createChatRoom(payload) {
  try {
    const res = await chatApi.post('/api/chatroom', payload);
    return res.data;
  } catch (error) {
    console.error('Failed to create chat room:', error);
    throw error;
  }
}
