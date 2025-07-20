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
        console.log(`🚀 [STOMP] sendChat 호출됨:`, {
          roomId: normalizedRoomId,
          connected,
          clientActive: clientRef.current?.active,
          chatRequest
        });

        if (!clientRef.current?.active || !connected) {
          console.warn("❌ [STOMP] 클라이언트가 연결되지 않음:", {
            clientActive: clientRef.current?.active,
            connected
          });
          return;
        }
        
        const enriched = {
          ...chatRequest,
          sourcePort: portParam,
        };
        
        const destination = `/send/chat.${normalizedRoomId}`;
        console.log(`📤 [STOMP] 메시지 발송:`, {
          destination,
          body: enriched
        });

        clientRef.current.publish({
          destination,
          body: JSON.stringify(enriched),
        });
        
        console.log(`✅ [STOMP] 메시지 발송 완료`);
      },
      [normalizedRoomId, connected, portParam]
  );

  const disconnect = useCallback(() => {
    console.log(`🔌 [STOMP] disconnect 호출됨`);
    msgSubRef.current?.unsubscribe();
    presSubRef.current?.unsubscribe();
    clientRef.current?.deactivate();
    msgSubRef.current = null;
    presSubRef.current = null;
    clientRef.current = null;
    setConnected(false);
    console.log(`🔌 [STOMP] disconnect 완료`);
  }, []);

  useEffect(() => {
    console.log(`🔄 [STOMP] useEffect 실행됨:`, { roomId, normalizedRoomId });
    
    if (roomId == null) {
      console.log(`⏹️ [STOMP] roomId가 null이므로 종료`);
      return;
    }
    let isUnmount = false;

    disconnect();

    (async () => {
      console.log(`🔐 [STOMP] 토큰 가져오는 중...`);
      const token = await getAccessToken();
      if (!token) {
        console.error("❌ [STOMP] 토큰을 가져올 수 없습니다");
        return;
      }
      console.log(`✅ [STOMP] 토큰 획득 완료`);

      console.log(`🔗 [STOMP] 클라이언트 연결 시작:`, {
        brokerUrl: effectiveBrokerUrl,
        roomId: normalizedRoomId
      });

      const client = new Client({
        webSocketFactory: () => {
          console.log(`🌐 [STOMP] SockJS 팩토리 실행:`, effectiveBrokerUrl);
          return new SockJS(effectiveBrokerUrl);
        },
        connectHeaders: {Authorization: `Bearer ${token}`},
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        debug: (str) => {
          console.log(`🐛 [STOMP DEBUG]:`, str);
        },

        onConnect: (frame) => {
          if (isUnmount) {
            console.log(`⚠️ [STOMP] 언마운트된 상태에서 연결됨, 무시`);
            return;
          }
          console.log(`🎉 [STOMP] 연결 성공:`, {
            roomId: normalizedRoomId,
            frame: frame.headers
          });
          setConnected(true);

          // 채팅 메시지 구독
          const chatDestination = `/topic/chat.${normalizedRoomId}`;
          console.log(`📥 [STOMP] 채팅 구독 시작:`, chatDestination);
          
          msgSubRef.current = client.subscribe(
              chatDestination,
              (message) => {
                console.log(`📨 [STOMP] 채팅 메시지 수신:`, {
                  destination: chatDestination,
                  headers: message.headers,
                  bodyLength: message.body?.length
                });
                
                if (!message.body) {
                  console.warn(`⚠️ [STOMP] 채팅 메시지 body가 없음`);
                  return;
                }
                try {
                  const msg = JSON.parse(message.body);
                  console.log(`✅ [STOMP] 채팅 메시지 파싱 성공:`, msg);
                  msgCbRef.current(msg);
                  console.log(`✅ [STOMP] 채팅 콜백 실행 완료`);
                } catch (e) {
                  console.error("❌ [STOMP] 채팅 메시지 JSON 파싱 오류:", e, message.body);
                }
              }
          );
          console.log(`✅ [STOMP] 채팅 구독 완료`);

          // Presence 구독
          const presenceDestination = `/topic/presence.${normalizedRoomId}`;
          console.log(`👥 [STOMP] Presence 구독 시작:`, presenceDestination);
          
          presSubRef.current = client.subscribe(
              presenceDestination,
              (message) => {
                console.log(`👤 [STOMP] Presence 메시지 수신:`, {
                  destination: presenceDestination,
                  headers: message.headers,
                  bodyLength: message.body?.length
                });
                
                if (!message.body) {
                  console.warn(`⚠️ [STOMP] Presence 메시지 body가 없음`);
                  return;
                }
                try {
                  const presenceData = JSON.parse(message.body);
                  console.log(`✅ [STOMP] Presence 메시지 파싱 성공:`, presenceData);
                  presCbRef.current(presenceData);
                  console.log(`✅ [STOMP] Presence 콜백 실행 완료`);
                } catch (e) {
                  console.error("❌ [STOMP] Presence JSON 파싱 오류:", e, message.body);
                }
              }
          );
          console.log(`✅ [STOMP] Presence 구독 완료`);
        },

        onStompError: (frame) => {
          console.error("💥 [STOMP] STOMP 에러:", {
            message: frame.headers.message,
            headers: frame.headers,
            body: frame.body
          });
          setConnected(false);
        },
        
        onWebSocketClose: (event) => {
          console.log("🔌 [STOMP] WebSocket 연결 종료:", {
            code: event.code,
            reason: event.reason,
            wasClean: event.wasClean
          });
          setConnected(false);
        },
        
        onWebSocketError: (err) => {
          console.error("🌐 [STOMP] WebSocket 에러:", err);
          setConnected(false);
        },
        
        onDisconnect: (frame) => {
          console.log("🔌 [STOMP] STOMP 연결 해제:", frame);
          setConnected(false);
        }
      });

      console.log(`🚀 [STOMP] 클라이언트 활성화 중...`);
      client.activate();
      clientRef.current = client;
      console.log(`✅ [STOMP] 클라이언트 활성화 완료`);
    })();

    return () => {
      console.log(`🧹 [STOMP] useEffect cleanup 실행됨`);
      isUnmount = true;
      disconnect();
    };
  }, [normalizedRoomId, effectiveBrokerUrl, disconnect, roomId]);

  return {sendChat, disconnect, connected};
}