import { chatApi } from '../lib/axios.ts';


function getErrorMessage(error, defaultMsg) {
  const serverMsg = error?.response?.data?.message;
  return serverMsg || defaultMsg;
}

export async function createChatRoom(payload) {
  try {
    const res = await chatApi.post('/api/chatroom', payload);
    return res.data;
  } catch (error) {
    console.error('Failed to create chat room:', error);
    throw new Error(getErrorMessage(error, '채팅방 생성 중 알 수 없는 오류가 발생했습니다.'));
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
    throw new Error(getErrorMessage(error, '채팅방 목록을 가져오는 중 오류가 발생했습니다.'));
  }
}

export async function searchChatRooms({ organizationId, keyword, page, size, sort }) {
  try {
    const params = {};
    if (keyword) params.keyword = keyword;
    if (page != null) params.page = page;
    if (size != null) params.size = size;
    if (sort) params.sort = sort;

    const { data } = await chatApi.get(
      `/api/chatroom/search/${organizationId}`,
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
    console.error('Failed to search chat rooms:', error);
    throw new Error(getErrorMessage(error, '채팅방 검색 중 오류가 발생했습니다.'));
  }
}

export async function getChatRoomInfo(roomId) {
  try {
    const { data } = await chatApi.get(`/api/chatroom/${roomId}`);
    return data;
  } catch (error) {
    console.error(`Failed to fetch chat room info (roomId: ${roomId}):`, error);
    throw new Error(getErrorMessage(error, '채팅방 정보를 가져오는 중 오류가 발생했습니다.'));
  }
}

export async function joinChatRoom({ roomId, password }) {
  try {
    await chatApi.post(`/api/chatroom/${roomId}/join`, { password });
  } catch (error) {
    console.error(`Failed to join chat room (roomId: ${roomId}):`, error);
    throw new Error(getErrorMessage(error, '채팅방 참가 중 오류가 발생했습니다.'));
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
      nextCursor: data.nextCursor,
    };
  } catch (error) {
    console.error('Failed to fetch chats by cursor:', error);
    throw new Error(getErrorMessage(error, '채팅 조회 중 오류가 발생했습니다.'));
  }
}

export async function deleteChatRoom(roomId) {
  try {
    await chatApi.post(`/api/chatroom/${roomId}/delete`);
    return { message: '채팅방이 성공적으로 삭제되었습니다.' };
  } catch (error) {
    console.error(`Failed to delete chat room (roomId: ${roomId}):`, error);
    const defaultMsg = error.response?.status === 403 ? '채팅방 방장이 아닙니다.' : '채팅방 삭제 중 오류가 발생했습니다.';
    throw new Error(getErrorMessage(error, defaultMsg));
  }
}

export async function updateChatRoom({ roomId, ...payload }) {
  try {
    await chatApi.patch(`/api/chatroom/${roomId}/update`, payload);
    return { message: '채팅방이 성공적으로 수정되었습니다.' };
  } catch (error) {
    console.error(`Failed to update chat room (roomId: ${roomId}):`, error);
    throw new Error(getErrorMessage(error, '채팅방 수정 중 오류가 발생했습니다.'));
  }
}