import { Client } from "@stomp/stompjs";

function websocketUrl() {
  if (import.meta.env.VITE_WS_URL) return import.meta.env.VITE_WS_URL;

  const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
  return `${protocol}//${window.location.host}/ws`;
}

export function createChatSocket({ onConnect, onDisconnect, onError }) {
  const token = localStorage.getItem("hackgov.accessToken");
  if (!token) return null;

  const client = new Client({
    brokerURL: websocketUrl(),
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    reconnectDelay: 3000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect,
    onDisconnect,
    onWebSocketClose: onDisconnect,
    onStompError: (frame) => {
      onError?.(frame.headers.message || "Falha na conexão de mensagens.");
    },
    debug: () => {},
  });

  client.activate();
  return client;
}

export function subscribeToChat(client, chatId, onMessage) {
  return client.subscribe(`/topic/chat/${chatId}`, (frame) => {
    onMessage(JSON.parse(frame.body));
  });
}

export function sendChatMessage(client, chatId, content) {
  client.publish({
    destination: "/app/chat.send",
    body: JSON.stringify({ chatId, content }),
  });
}
