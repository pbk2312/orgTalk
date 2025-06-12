import { useEffect, useRef, useState, useCallback } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getAccessToken } from "../lib/axios.ts";

/**
 * useChatStomp
 *
 * JWT 기반 STOMP/WebSocket 연결을 관리하고,
 * 지정된 roomId로 메시지를 주고받을 수 있는 훅입니다.
 *
 * @param {string|number} roomId                     채팅방 ID
 * @param {(message: any) => void} onMessageCallback 서버로부터 새 메시지를 받을 때 호출되는 콜백
 * @param {string} [brokerUrl]                       STOMP/WebSocket 브로커 URL (우선 brokerUrl, 없으면 쿼리 param 'port')
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
  brokerUrl
) {
  // 쿼리에서 port 읽기 (없으면 8081)
  let portParam = '8081';
  try {
    const params = new URLSearchParams(window.location.search);
    portParam = params.get('port') || portParam;
  } catch (e) {
    // SSR 등 window가 없으면 기본값 유지
  }
  // 실제 연결할 브로커 URL 결정
  const effectiveBrokerUrl = brokerUrl || `http://localhost:${portParam}/ws-stomp`;

  const normalizedRoomId = String(roomId);
  const clientRef = useRef(null);
  const subscriptionRef = useRef(null);
  const [connected, setConnected] = useState(false);

  // 콜백 레퍼런스
  const callbackRef = useRef(onMessageCallback);
  useEffect(() => {
    callbackRef.current = onMessageCallback;
  }, [onMessageCallback]);

  // 메시지 전송, sourcePort 포함 및 디버그 로그 추가
  const sendChat = useCallback(
    (chatRequest) => {
      if (!clientRef.current?.active || !connected) {
        console.warn("WebSocket이 연결되지 않았거나 활성화되지 않았습니다.");
        return;
      }
      const enriched = { ...chatRequest, sourcePort: portParam };
      console.log(`[useChatStomp] sending from port ${portParam}:`, enriched);
      try {
        clientRef.current.publish({
          destination: `/send/chat/${normalizedRoomId}`,
          body: JSON.stringify(enriched),
        });
      } catch (err) {
        console.error("메시지 전송 중 에러:", err);
      }
    },
    [normalizedRoomId, connected, portParam]
  );

  // 연결 끊기
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
        console.log(`[useChatStomp] connecting to ${effectiveBrokerUrl} for room ${normalizedRoomId}`);
        const client = new Client({
          webSocketFactory: () => new SockJS(effectiveBrokerUrl),
          connectHeaders: { Authorization: `Bearer ${token}` },
          reconnectDelay: 5000,
          heartbeatIncoming: 10000,
          heartbeatOutgoing: 10000,

          onConnect: () => {
            if (isUnmounting) return;
            setConnected(true);
            console.log("[useChatStomp] STOMP 연결 성공");
            try {
              const destination = `/topic/rooms/${normalizedRoomId}`;
              subscriptionRef.current = client.subscribe(destination, ({ body }) => {
                if (!body) return;
                try {
                  const parsed = JSON.parse(body);
                  console.log(`[useChatStomp] received message:`, parsed);
                  callbackRef.current(parsed);
                } catch (e) {
                  console.error("수신 메시지 파싱 오류:", e);
                }
              });
            } catch (e) {
              console.error("구독 중 오류:", e);
            }
          },

          onStompError: (frame) => console.error("STOMP 에러:", frame.headers.message),
          onWebSocketClose: () => { setConnected(false); console.warn("[useChatStomp] WebSocket closed"); },
          onWebSocketError: (evt) => console.error("WebSocket 오류:", evt),
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
  }, [normalizedRoomId, disconnect, effectiveBrokerUrl]);

  return { sendChat, disconnect, connected };
}