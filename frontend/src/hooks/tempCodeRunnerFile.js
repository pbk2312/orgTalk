import {useCallback, useEffect, useRef, useState} from "react";
import {Client} from "@stomp/stompjs";
import SockJS from "sockjs-client";
import {getAccessToken} from "../lib/axios.ts";
import {CHAT_BASE_URL} from '../lib/constants.ts';

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
  } catch {
  }

  // RabbitMQ 연동을 위한 WebSocket 엔드포인트
  const effectiveBrokerUrl =
      brokerUrl || `${CHAT_BASE_URL.replace(/\/$/, '')}/ws-stomp`;

  const normalizedRoomId = String(roomId);

  const clientRef = useRef(null);
  const msgSubRef = useRef(null);
  const presSubRef = useRef(null);

  const msgCbRef = useRef(onMessageCallback);
  const presCbRef = useRef(onPresenceCallback);
  useEffect(() => {
    msgCbRef.current = onMessageCallback;
  }, [onMessageCallback]);
  useEffect(() => {
    presCbRef.current = onPresenceCallback;
  }, [onPresenceCallback]);

  const [connected, setConnected] = useState(false);

  const sendChat = useCallback(
      (chatRequest) => {
        if (!clientRef.current?.active || !connected) {
          console.warn("클라이언트가 연결되지 않음");
          return;
        }
        const enriched = {
          ...chatRequest,
          sourcePort: portParam,
        };
        
        // RabbitMQ로 메시지 전송
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
    if (roomId == null) {
      return;
    }
    let isUnmount = false;

    disconnect();

    (async () => {
      const token = await getAccessToken();
      if (!token) {
        console.error("토큰을 가져올 수 없습니다");
        return;
      }

      const client = new Client({
        webSocketFactory: () => new SockJS(effectiveBrokerUrl),
        connectHeaders: {Authorization: `Bearer ${token}`},
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,

        onConnect: () => {
          if (isUnmount) {
            return;
          }
          console.log(`채팅방 ${normalizedRoomId} 연결됨`);
          setConnected(true);

          // RabbitMQ Topic Exchange를 통한 채팅 메시지 구독
          msgSubRef.current = client.subscribe(
              `/topic/chat/${normalizedRoomId}`,
              ({body}) => {
                if (!body) {
                  return;
                }
                try {
                  const msg = JSON.parse(body);
                  msgCbRef.current(msg);
                } catch (e) {
                  console.error("메시지 JSON 파싱 오류:", e);
                }
              }
          );

          // Presence 구독 (필요한 경우)
          presSubRef.current = client.subscribe(
              `/topic/presence/${normalizedRoomId}`,
              ({body}) => {
                if (!body) {
                  return;
                }
                try {
                  const presenceData = JSON.parse(body);
                  presCbRef.current(presenceData);
                } catch (e) {
                  console.error("Presence JSON 파싱 오류:", e);
                }
              }
          );
        },

        onStompError: (frame) => {
          console.error("STOMP 에러:", frame.headers.message);
          setConnected(false);
        },
        onWebSocketClose: (event) => {
          console.log("WebSocket 연결 종료:", event);
          setConnected(false);
        },
        onWebSocketError: (err) => {
          console.error("WebSocket 에러:", err);
          setConnected(false);
        },
      });

      client.activate();
      clientRef.current = client;
    })();

    return () => {
      isUnmount = true;
      disconnect();
    };
  }, [normalizedRoomId, effectiveBrokerUrl, disconnect, roomId]);

  return {sendChat, disconnect, connected};
}