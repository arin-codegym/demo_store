import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {
  chatUiActions,
  chatUiReduxStore,
} from '@/stores/chat-ui-redux-store';

export type MessageHandler = (message: IMessage) => void;

let stompClient: Client | null = null;
let sidebarSubscription: StompSubscription | null = null;
let conversationSubscription: StompSubscription | null = null;
let notificationSubscription: StompSubscription | null = null;

const API_BASE =
  process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080/api/backend';

function getWsBaseUrl() {
  return API_BASE.replace(/\/api\/backend$/, '');
}

function setConnectedState(connected: boolean) {
  // Zustand: useChatUiStore.getState().setSocketStatus(...)
  chatUiReduxStore.dispatch(
    chatUiActions.setSocketStatus(connected ? 'connected' : 'disconnected'),
  );
  // Zustand: useChatUiStore.getState().setStompConnected(...)
  chatUiReduxStore.dispatch(chatUiActions.setStompConnected(connected));
}

function log(...args: unknown[]) {
  // console.log('[socket]', ...args);
}

export function connectStomp() {
  if (stompClient?.active) {
    log('skip connect, already connected/connecting');
    return stompClient;
  }

  // Keep one STOMP client per browser tab; subscriptions are swapped as the
  // active conversation changes.
  // Zustand: useChatUiStore.getState().setSocketStatus('connecting')
  chatUiReduxStore.dispatch(chatUiActions.setSocketStatus('connecting'));

  const client = new Client({
    webSocketFactory: () => new SockJS(`${getWsBaseUrl()}/ws`),
    reconnectDelay: 5000,
    // debug: (str) => console.log('[stomp]', str),

    onConnect: () => {
      log('STOMP connected');
      setConnectedState(true);
    },

    onStompError: (frame) => {
      console.error('[socket] STOMP error', frame);
      setConnectedState(false);
    },

    onWebSocketClose: (event) => {
      console.error('[socket] WebSocket closed', event);
      setConnectedState(false);
    },

    onWebSocketError: (event) => {
      console.error('[socket] WebSocket error', event);
    },
  });

  stompClient = client;
  client.activate();

  return client;
}

export function subscribeSidebar(userId: string, onMessage: MessageHandler) {
  if (!stompClient?.connected) {
    log('cannot subscribe sidebar, stomp not connected');
    return;
  }

  const destination = '/user/queue/conversations';

  sidebarSubscription?.unsubscribe();
  sidebarSubscription = stompClient.subscribe(destination, (frame) => {
    // log('sidebar frame', { userId, destination, body: frame.body });
    onMessage(frame);
  });

  // log('subscribed', destination);
}

export function subscribeConversation(
  conversationId: string,
  onMessage: MessageHandler,
) {
  if (!stompClient?.connected) {
    log('cannot subscribe conversation, stomp not connected');
    return;
  }

  const destination = `/topic/conversations.${conversationId}`;

  conversationSubscription?.unsubscribe();
  conversationSubscription = stompClient.subscribe(destination, (frame) => {
    // log('conversation frame', { destination, body: frame.body });
    onMessage(frame);
  });

  // log('subscribed', destination);
}

export function unsubscribeSidebar() {
  if (sidebarSubscription) {
    log('unsubscribe sidebar');
    sidebarSubscription.unsubscribe();
    sidebarSubscription = null;
  }
}

export function unsubscribeConversation() {
  if (conversationSubscription) {
    log('unsubscribe conversation');
    conversationSubscription.unsubscribe();
    conversationSubscription = null;
  }
}

type SyncSubscriptionsParams = {
  stompConnected: boolean;
  userId: string;
  activeConversationId: string | null;
  onMessage: MessageHandler;
};

export function syncSubscriptions({
  stompConnected,
  userId,
  activeConversationId,
  onMessage,
}: SyncSubscriptionsParams) {
  if (!stompConnected) {
    // Dropping all subscriptions avoids receiving user-scoped frames after
    // logout or reconnect transitions.
    unsubscribeSidebar();
    unsubscribeConversation();
    unsubscribeNotifications();
    return;
  }

  subscribeSidebar(userId, onMessage);
  subscribeNotifications(userId, onMessage);
  if (activeConversationId) {
    subscribeConversation(activeConversationId, onMessage);
  } else {
    unsubscribeConversation();
  }
}

export function subscribeNotifications(
  userId: string,
  onMessage: MessageHandler,
) {
  if (!stompClient?.connected) {
    log('cannot subscribe notifications, stomp not connected');
    return;
  }

  const destination = '/user/queue/notifications';

  notificationSubscription?.unsubscribe();
  notificationSubscription = stompClient.subscribe(destination, (frame) => {
    // log('notification frame', { userId, destination, body: frame.body });
    onMessage(frame);
  });

  // log('subscribed', destination);
}

export function unsubscribeNotifications() {
  if (notificationSubscription) {
    log('unsubscribe notifications');
    notificationSubscription.unsubscribe();
    notificationSubscription = null;
  }
}

export function disconnectStomp() {
  log('disconnect');

  unsubscribeSidebar();
  unsubscribeConversation();
  unsubscribeNotifications();

  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
  }

  setConnectedState(false);
}
