// src/hooks/useChatStomp.js
import { useEffect, useRef } from "react";
import SockJS from "sockjs-client";
import Stomp from "stompjs";
import { getAccessToken } from "../lib/axios.ts"; 

export function useChatStomp({ roomId, onMessageReceived }) {
  const stompClientRef = useRef(null);

  useEffect(() => {
    // 1) roomId가 없으면 연결하지 않음
    if (!roomId) return;

    // 2) 메모리에서 꺼내온 accessToken
    const accessToken = getAccessToken();
    if (!accessToken) {
      console.warn("접속 토큰이 없습니다. 로그인 상태를 확인하세요.");
      return;
    }

    // 3) SockJS -> STOMP 연결 준비
    const socket = new SockJS("/ws-stomp");
    const stompClient = Stomp.over(socket);

    // 4) CONNECT 시 헤더에 JWT를 실어 보낸다
    stompClient.connect(
      { Authorization: `Bearer ${accessToken}` },
      () => {
        console.log(`>>> STOMP 연결 성공 (roomId=${roomId})`);
        stompClient.subscribe(`/room/${roomId}`, (message) => {
          const payload = JSON.parse(message.body);
          onMessageReceived(payload);
        });
      },
      (error) => {
        console.error(">>> STOMP 연결 에러:", error);
      }
    );

    stompClientRef.current = stompClient;

    // 언마운트 시 연결 해제
    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.disconnect(() => {
          console.log(">>> STOMP 연결 종료");
        });
      }
    };
  }, [roomId, onMessageReceived]);

  // 메시지 전송 함수 (DTO가 { message } 하나만 받는다고 가정)
  const sendMessage = (message) => {
    if (stompClientRef.current && stompClientRef.current.connected) {
      const chatRequest = { message };
      stompClientRef.current.send(
        `/send/chat/${roomId}`, 
        {},
        JSON.stringify(chatRequest)
      );
    } else {
      console.warn(">>> STOMP 클라이언트가 연결되지 않았습니다.");
    }
  };

  return { sendMessage };
}
