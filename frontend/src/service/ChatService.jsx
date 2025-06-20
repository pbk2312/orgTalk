// src/lib/ChatService.jsx

import { chatApi } from '../lib/axios.ts'; 


export async function createChatRoom(payload) {
  try {
    const res = await chatApi.post('/api/chatroom', payload);
    alert('채팅방이 성공적으로 생성되었습니다.');
    return res.data;
  } catch (error) {
    console.error('Failed to create chat room:', error);
    throw error;
  }
}

export async function getChatRooms(params) {
  try {
    const { data } = await chatApi.get(
      `/api/chatroom/list/${params.organizationId}`,
      { params }
    );
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

export async function getChatRoomInfo(roomId) {
  try {
    const { data } = await chatApi.get(`/api/chatroom/${roomId}`);
    return data;
  } catch (error) {
    console.error(
      `Failed to fetch chat room info (roomId: ${roomId}):`,
      error
    );
    throw error;
  }
}

export async function joinChatRoom({ roomId, password }) {
  try {
    await chatApi.post(`/api/chatroom/${roomId}/join`, { password });
  } catch (error) {
    console.error(
      `Failed to join chat room (roomId: ${roomId}):`,
      error
    );
    throw error;
  }
}

export async function getChatsByCursor(roomId, cursor = null, size) {
  try {
    const params = {};
    if (cursor) params.cursor = cursor;
    if (size != null) {
      params.size = size;
    }
    const { data } = await chatApi.get(`/api/chat/${roomId}`, { params });
    return {
      chats: data.chats,
      nextCursor: data.nextCursor
    };
  } catch (error) {
    console.error('커서 기반 채팅 조회 실패:', error);
    throw error;
  }
}


export async function deleteChatRoom(roomId) {
  try {
    await chatApi.post(`/api/chatroom/${roomId}/delete`);
    alert('채팅방이 성공적으로 삭제되었습니다.');
  } catch (error) {
    if (error.response && error.response.status === 403) {
      alert('채팅방 방장이 아닙니다.');
    } else {
      console.error(`Failed to delete chat room (roomId: ${roomId}):`, error);
    }
    throw error;
  }
}


export async function updateChatRoom({ roomId, ...payload }) {
  try {
    await chatApi.patch(`/api/chatroom/${roomId}/update`, payload);
    alert('채팅방이 성공적으로 수정되었습니다.');
  } catch (error) {
    if (error.response?.data?.message) {
      alert(error.response.data.message);
    } else {
      console.error(`Failed to update chat room (roomId: ${roomId}):`, error);
      alert('알 수 없는 에러가 발생했습니다.');
    }
    throw error;
  }
}