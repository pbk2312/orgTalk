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

/**
 * 특정 채팅방의 상세 정보를 조회합니다.
 * @param {number} roomId - 조회할 채팅방 ID
 * @returns {Promise<ChatRoomInfoResponse>}
 */
export async function getChatRoomInfo(roomId) {
  try {
    const { data } = await chatApi.get(`/api/chatroom/${roomId}`);
    // data: { name, description, type, memberCount }
    return data;
  } catch (error) {
    console.error(`Failed to fetch chat room info (roomId: ${roomId}):`, error);
    throw error;
  }
}

/**
 * 채팅방 참여(join) API 호출
 * 백엔드 컨트롤러: @PostMapping("/api/chatroom/{roomId}/join")
 * 요청 바디로 { password }를 함께 보내야 합니다.
 *
 * @param {Object} payload
 * @param {number} payload.roomId    - 참여하려는 채팅방 ID
 * @param {string} payload.password  - 비공개 방일 경우 입력한 평문 비밀번호
 * @returns {Promise<void>} 성공 시 204 No Content 반환 (throw 시 에러)
 */
export async function joinChatRoom({ roomId, password }) {
  try {
    // ► 두 번째 인자로 바로 { password } 전달
    await chatApi.post(`/api/chatroom/${roomId}/join`, { password });
  } catch (error) {
    console.error(`Failed to join chat room (roomId: ${roomId}):`, error);
    throw error;
  }
}
