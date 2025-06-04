// src/hooks/useChatStomp.js

import { useEffect, useRef, useState, useCallback } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getAccessToken } from "../lib/axios.ts";

/**
 * useChatStomp
 *
 * JWT 기반 STOMP/WebSocket 연결을 관리하고, 지정된 roomId로 메시지를 주고받을 수 있는 훅입니다.
 *
 * @param {string|number} roomId                     채팅방 ID
 * @param {(message: any) => void} onMessageCallback 서버로부터 새 메시지를 받을 때 호출되는 콜백
 *
 * @returns {{
 *   sendChat: (chatRequest: object) => void,
 *   disconnect: () => void,
 *   connected: boolean
 * }}
 */
export function useChatStomp(roomId, onMessageCallback) {
  const normalizedRoomId = String(roomId); // roomId를 문자열로 강제 변환
  const clientRef = useRef(null);
  const subscriptionRef = useRef(null);
  const [connected, setConnected] = useState(false);

  const callbackRef = useRef(onMessageCallback);
  useEffect(() => {
    callbackRef.current = onMessageCallback;
  }, [onMessageCallback]);

  const sendChat = useCallback(
    (chatRequest) => {
      if (!clientRef.current?.active || !connected) {
        console.warn("WebSocket이 연결되지 않았거나 활성화되지 않았습니다.");
        return;
      }
      try {
        clientRef.current.publish({
          destination: `/send/chat/${normalizedRoomId}`,
          body: JSON.stringify(chatRequest),
        });
      } catch (err) {
        console.error("메시지 전송 중 에러:", err);
      }
    },
    [normalizedRoomId, connected]
  );

  const disconnect = useCallback(() => {
    if (subscriptionRef.current) {
      try {
        subscriptionRef.current.unsubscribe();
      } catch {}
      subscriptionRef.current = null;
    }

    if (clientRef.current) {
      try {
        clientRef.current.deactivate();
      } catch {}
      clientRef.current = null;
    }

    setConnected(false);
  }, []);

  useEffect(() => {
    if (roomId == null) return;

    let isUnmounting = false;
    disconnect();

    (async () => {
      try {
        const token = await getAccessToken();
        if (!token) {
          console.error("토큰을 가져올 수 없습니다.");
          return;
        }

        const client = new Client({
          webSocketFactory: () => new SockJS("http://localhost:8081/ws-stomp"),
          connectHeaders: {
            Authorization: `Bearer ${token}`,
          },
          reconnectDelay: 5000,
          heartbeatIncoming: 10000,
          heartbeatOutgoing: 10000,

          onConnect: () => {
            if (isUnmounting) return;
            console.log("STOMP 연결 성공:", normalizedRoomId);
            setConnected(true);

            try {
              const destination = `/topic/room/${normalizedRoomId}`;
              subscriptionRef.current = client.subscribe(destination, ({ body }) => {
                if (!body) return;
                try {
                  const parsed = JSON.parse(body);
                  callbackRef.current(parsed);
                } catch (e) {
                  console.error("수신 메시지 파싱 오류:", e);
                }
              });
            } catch (e) {
              console.error("구독(subscribe) 중 오류:", e);
            }
          },

          onStompError: (frame) => {
            console.error("STOMP 프로토콜 에러:", frame.headers["message"]);
          },

          onWebSocketClose: () => {
            console.warn("WebSocket 연결이 끊어졌습니다.");
            setConnected(false);
          },

          onWebSocketError: (evt) => {
            console.error("WebSocket 오류:", evt);
          },
        });

        client.activate();
        clientRef.current = client;
      } catch (err) {
        console.error("WebSocket 연결 준비 중 에러:", err);
      }
    })();

    return () => {
      isUnmounting = true;
      disconnect();
    };
  }, [normalizedRoomId, disconnect]);

  return {
    sendChat,
    disconnect,
    connected,
  };
}
