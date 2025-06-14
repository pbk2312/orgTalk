import { useEffect, useRef, useState, useCallback } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getAccessToken } from "../lib/axios.ts";

/**
 * useChatStomp
 *
 * JWT 기반 STOMP/WebSocket 연결을 관리하고,
 * 지정된 roomId로 메시지 주고받기와 Presence(입장/퇴장) 업데이트를 처리하는 훅
 *
 * @param {string|number} roomId                       채팅방 ID
 * @param {(message: any) => void} onMessageCallback   채팅 메시지 수신 콜백
 * @param {(presence: any) => void} onPresenceCallback Presence 이벤트 수신 콜백
 * @param {string} [brokerUrl]                         STOMP/WebSocket 브로커 URL
 *
 * @returns {{
 *   sendChat: (chatRequest: object) => void,
 *   disconnect: () => void,
 *   connected: boolean
 * }}
 */
export function useChatStomp(
  roomId,
  onMessageCallback,
  onPresenceCallback,
  brokerUrl
) {
  // URL 파라미터 port 처리
  let portParam = "8081";
  try {
    const params = new URLSearchParams(window.location.search);
    portParam = params.get("port") || portParam;
  } catch {}

  const effectiveBrokerUrl =
    brokerUrl || `http://localhost:${portParam}/ws-stomp`;
  const normalizedRoomId = String(roomId);

  // 내부 refs
  const clientRef = useRef(null);
  const msgSubRef = useRef(null);
  const presSubRef = useRef(null);

  // 콜백 refs (최신 callback 유지)
  const msgCbRef = useRef(onMessageCallback);
  const presCbRef = useRef(onPresenceCallback);
  useEffect(() => { msgCbRef.current = onMessageCallback; }, [onMessageCallback]);
  useEffect(() => { presCbRef.current = onPresenceCallback; }, [onPresenceCallback]);

  // 연결 상태
  const [connected, setConnected] = useState(false);

  // 채팅 전송 함수
  const sendChat = useCallback(
    (chatRequest) => {
      if (!clientRef.current?.active || !connected) return;
      const enriched = {
        ...chatRequest,
        sourcePort: portParam,
        // TODO: 필요하다면 프로필 정보도 여기에 추가 가능
        // senderAvatar: chatRequest.senderAvatar,
        // senderNickname: chatRequest.senderName,
      };
      clientRef.current.publish({
        destination: `/send/chat/${normalizedRoomId}`,
        body: JSON.stringify(enriched),
      });
    },
    [normalizedRoomId, connected, portParam]
  );

  // 연결 해제 함수
  const disconnect = useCallback(() => {
    msgSubRef.current?.unsubscribe();
    presSubRef.current?.unsubscribe();
    clientRef.current?.deactivate();
    msgSubRef.current = null;
    presSubRef.current = null;
    clientRef.current = null;
    setConnected(false);
  }, []);

  // STOMP 클라이언트 초기화 및 구독
  useEffect(() => {
    if (roomId == null) return;
    let isUnmount = false;

    // 기존 연결 정리
    disconnect();

    (async () => {
      const token = await getAccessToken();
      if (!token) return;

      const client = new Client({
        webSocketFactory: () => new SockJS(effectiveBrokerUrl),
        connectHeaders: { Authorization: `Bearer ${token}` },
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,

        onConnect: () => {
          if (isUnmount) return;
          setConnected(true);

          // 1) 채팅 메시지 구독
          msgSubRef.current = client.subscribe(
            `/topic/rooms/${normalizedRoomId}`,
            ({ body }) => {
              if (!body) return;
              try {
                const msg = JSON.parse(body);
                msgCbRef.current(msg);
              } catch (e) {
                console.error("메시지 JSON 파싱 오류:", e);
              }
            }
          );

          // 2) Presence(입장/퇴장) 구독
          presSubRef.current = client.subscribe(
            `/topic/presence/${normalizedRoomId}`,
            ({ body }) => {
              if (!body) return;
              try {
                const presenceData = JSON.parse(body);
                presCbRef.current(presenceData);
              } catch (e) {
                console.error("Presence JSON 파싱 오류:", e);
              }
            }
          );
        },

        onStompError: (frame) => console.error("STOMP 에러:", frame.headers.message),
        onWebSocketClose: () => setConnected(false),
        onWebSocketError: (err) => console.error("WebSocket 에러:", err),
      });

      client.activate();
      clientRef.current = client;
    })();

    return () => {
      isUnmount = true;
      disconnect();
    };
  }, [normalizedRoomId, effectiveBrokerUrl, disconnect, roomId]);

  return { sendChat, disconnect, connected };
}
