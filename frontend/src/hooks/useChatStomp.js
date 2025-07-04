import { useEffect, useRef, useState, useCallback } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getAccessToken } from "../lib/axios.ts";
import { API_BASE_URL } from '../lib/constants.ts';

export function useChatStomp(
  roomId,
  onMessageCallback,
  onPresenceCallback,
  brokerUrl
) {
  let portParam = "8081";
  try {
    const params = new URLSearchParams(window.location.search);
    portParam = params.get("port") || portParam;
  } catch {}

  // 여기서 기본 brokerUrl이 API_BASE_URL + /ws-stomp 가 되도록 변경
  const effectiveBrokerUrl =
    brokerUrl || `${API_BASE_URL.replace(/\/$/, '')}/ws-stomp`;

  const normalizedRoomId = String(roomId);

  const clientRef = useRef(null);
  const msgSubRef = useRef(null);
  const presSubRef = useRef(null);

  const msgCbRef = useRef(onMessageCallback);
  const presCbRef = useRef(onPresenceCallback);
  useEffect(() => { msgCbRef.current = onMessageCallback; }, [onMessageCallback]);
  useEffect(() => { presCbRef.current = onPresenceCallback; }, [onPresenceCallback]);

  const [connected, setConnected] = useState(false);

  const sendChat = useCallback(
    (chatRequest) => {
      if (!clientRef.current?.active || !connected) return;
      const enriched = {
        ...chatRequest,
        sourcePort: portParam,
      };
      clientRef.current.publish({
        destination: `/send/chat/${normalizedRoomId}`,
        body: JSON.stringify(enriched),
      });
    },
    [normalizedRoomId, connected, portParam]
  );

  const disconnect = useCallback(() => {
    msgSubRef.current?.unsubscribe();
    presSubRef.current?.unsubscribe();
    clientRef.current?.deactivate();
    msgSubRef.current = null;
    presSubRef.current = null;
    clientRef.current = null;
    setConnected(false);
  }, []);

  useEffect(() => {
    if (roomId == null) return;
    let isUnmount = false;

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
