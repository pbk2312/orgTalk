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



/**
 * 채팅방 목록 조회
 * @param {number} organizationId - 조회할 조직 ID
 * @param {number} page           - 0부터 시작하는 페이지 인덱스
 * @param {number} size           - 한 페이지당 가져올 개수
 * @param {string} sort           - 정렬 기준 (예: "lastMessageAt,DESC")
 */
export async function getChatRooms(params) {
  try {
    const { data } = await chatApi.get(`/api/chatroom/list/${params.organizationId}`, { params });


    console.log('📥 API 응답 전체 데이터:', data);
    console.log('📋 응답된 채팅방 목록 (content):', data.content); 
    return {
      chatRooms: data.content,
      page: data.page,
      size: data.size,
      totalPages: data.totalPages,
      totalElements: data.totalElements,
    };
  } catch (error) {
    console.error('Failed to fetch chat rooms:', error);
    throw error;
  }
}
