import {chatApi, chatRoomApi} from '../lib/axios.ts';

function getErrorMessage(error, defaultMsg) {
  const serverMsg = error?.response?.data?.message;
  return serverMsg || defaultMsg;
}

export async function createChatRoom(payload) {
  try {
    const res = await chatRoomApi.post('/create', payload); // 앞의 /api/chatroom 생략
    return res.data;
  } catch (error) {
    console.error('Failed to create chat room:', error);
    throw new Error(getErrorMessage(error, '채팅방 생성 중 알 수 없는 오류가 발생했습니다.'));
  }
}

export async function getChatRooms(params) {
  try {
    // signal을 params에서 아예 제거
    const { signal, ...requestParams } = params;
    
    const { data } = await chatRoomApi.get(
      `/list`,
      { 
        params: requestParams
      }
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


export async function searchChatRooms({
  keyword,
  type,
  page,
  size,
  sort
}, options = {}) {
  try {
    const params = {};
    if (keyword) {
      params.keyword = keyword;
    }
    if (type) {
      params.type = type;
    }
    if (page != null) {
      params.page = page;
    }
    if (size != null) {
      params.size = size;
    }
    if (sort) {
      params.sort = sort;
    }

    console.log('🔍 searchChatRooms params:', params);

    const { data } = await chatRoomApi.get(
      `/search`,
      { 
        params,
        signal: options.signal // signal을 axios 옵션으로 전달
      }
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
    const {data} = await chatRoomApi.get(`/${roomId}`);  // 앞의 /api/chatroom 생략
    return data;
  } catch (error) {
    console.error(`Failed to fetch chat room info (roomId: ${roomId}):`, error);
    throw new Error(getErrorMessage(error, '채팅방 정보를 가져오는 중 오류가 발생했습니다.'));
  }
}

export async function joinChatRoom({roomId, password}) {
  try {
    await chatRoomApi.post(`/${roomId}/join`, {roomId, password});  // 앞의 /api/chatroom 생략
  } catch (error) {
    console.error(`Failed to join chat room (roomId: ${roomId}):`, error);
    throw new Error(getErrorMessage(error, '채팅방 참가 중 오류가 발생했습니다.'));
  }
}

export async function getChatsByCursor(roomId, cursor = null, size) {
  try {
    const params = {};
    if (cursor) {
      params.cursor = cursor;
    }
    if (size != null) {
      params.size = size;
    }
    const {data} = await chatApi.get(`/${roomId}`, {params});  // 앞의 /api/chatroom 생략
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
    await chatRoomApi.post(`/${roomId}/delete`);  // 앞의 /api/chatroom 생략
    return {message: '채팅방이 성공적으로 삭제되었습니다.'};
  } catch (error) {
    console.error(`Failed to delete chat room (roomId: ${roomId}):`, error);
    const defaultMsg = error.response?.status === 403 ? '채팅방 방장이 아닙니다.'
        : '채팅방 삭제 중 오류가 발생했습니다.';
    throw new Error(getErrorMessage(error, defaultMsg));
  }
}

export async function updateChatRoom({roomId, ...payload}) {
  try {
    await chatRoomApi.patch(`/${roomId}/update`, payload);  // 앞의 /api/chatroom 생략
    return {message: '채팅방이 성공적으로 수정되었습니다.'};
  } catch (error) {
    console.error(`Failed to update chat room (roomId: ${roomId}):`, error);
    throw new Error(getErrorMessage(error, '채팅방 수정 중 오류가 발생했습니다.'));
  }
}

export async function kickMember(roomId, kickedMemberId) {
  try {
    await chatRoomApi.post(  // 앞의 /api/chatroom 생략
        `/${roomId}/kickMember`,
        null,
        {params: {kickedMemberId}}
    );
    return {message: '멤버가 성공적으로 강퇴되었습니다.'};
  } catch (error) {
    console.error(
        `Failed to kick member (roomId: ${roomId}, memberId: ${kickedMemberId}):`,
        error);
    throw new Error(getErrorMessage(error, '멤버 강퇴 중 오류가 발생했습니다.'));
  }
}
