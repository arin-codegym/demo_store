'use client';

import {
  configureStore,
  createSlice,
  type Middleware,
  type PayloadAction,
} from '@reduxjs/toolkit';
import {
  TypedUseSelectorHook,
  useDispatch,
  useSelector,
} from 'react-redux';

export type SocketStatus = 'disconnected' | 'connecting' | 'connected';
export type ConversationWidgetType =
  | 'USER_ADMIN'
  | 'DIRECT'
  | 'USER_DIRECT'
  | null;

type ChatUiState = {
  socketStatus: SocketStatus;
  stompConnected: boolean;
  selectedConversationId: string | null;

  adminConversationId: string | null;
  adminWidgetOpen: boolean;

  aiConversationId: string | null;
  aiWidgetOpen: boolean;

  conversationWidgetConversationId: string | null;
  conversationWidgetType: ConversationWidgetType;
  conversationWidgetOpen: boolean;
};

const initialState: ChatUiState = {
  selectedConversationId: null,
  socketStatus: 'disconnected',
  stompConnected: false,

  adminConversationId: null,
  adminWidgetOpen: false,

  aiConversationId: null,
  aiWidgetOpen: false,

  conversationWidgetConversationId: null,
  conversationWidgetType: null,
  conversationWidgetOpen: false,
};

const chatUiSlice = createSlice({
  name: 'chatUi',
  initialState,
  reducers: {
    setSocketStatus(state, action: PayloadAction<SocketStatus>) {
      state.socketStatus = action.payload;
    },
    setStompConnected(state, action: PayloadAction<boolean>) {
      state.stompConnected = action.payload;
    },
    setSelectedConversationId(state, action: PayloadAction<string | null>) {
      state.selectedConversationId = action.payload;
    },
    setAdminConversationId(state, action: PayloadAction<string | null>) {
      state.adminConversationId = action.payload;
    },
    setAdminWidgetOpen(state, action: PayloadAction<boolean>) {
      state.adminWidgetOpen = action.payload;
    },
    setAiConversationId(state, action: PayloadAction<string | null>) {
      state.aiConversationId = action.payload;
    },
    setAiWidgetOpen(state, action: PayloadAction<boolean>) {
      state.aiWidgetOpen = action.payload;
    },
    setConversationWidgetConversationId(
      state,
      action: PayloadAction<string | null>,
    ) {
      state.conversationWidgetConversationId = action.payload;
    },
    setConversationWidgetType(
      state,
      action: PayloadAction<ConversationWidgetType>,
    ) {
      state.conversationWidgetType = action.payload;
    },
    setConversationWidgetOpen(state, action: PayloadAction<boolean>) {
      state.conversationWidgetOpen = action.payload;
    },
    openConversationWidget(
      state,
      action: PayloadAction<{
        conversationId: string;
        type: Exclude<ConversationWidgetType, null>;
      }>,
    ) {
      state.conversationWidgetConversationId = action.payload.conversationId;
      state.conversationWidgetType = action.payload.type;
      state.conversationWidgetOpen = true;
      state.adminWidgetOpen = false;
      state.aiWidgetOpen = false;
    },
    closeConversationWidget(state) {
      state.conversationWidgetOpen = false;
      state.conversationWidgetConversationId = null;
      state.conversationWidgetType = null;
    },
    reset() {
      return initialState;
    },
  },
});

export const chatUiActions = chatUiSlice.actions;

export type ChatUiRootState = {
  chatUi: ChatUiState;
};

const actionLogger: Middleware<{}, ChatUiRootState> =
  (store) => (next) => (action) => {
    if (!isReduxAction(action)) {
      return next(action);
    }

    console.groupCollapsed(`[redux] ${action.type}`);
    console.log('Payload:', action.payload);
    console.log('Prev state:', store.getState());

    const result = next(action);

    console.log('Next state:', store.getState());
    console.groupEnd();

    return result;
  };

function isReduxAction(action: unknown): action is {
  type: string;
  payload?: unknown;
} {
  return (
    typeof action === 'object' &&
    action !== null &&
    'type' in action &&
    typeof (action as { type?: unknown }).type === 'string'
  );
}

export const chatUiReduxStore = configureStore({
  reducer: {
    chatUi: chatUiSlice.reducer,
  },
  middleware: (getDefaultMiddleware) => {
    const middleware = getDefaultMiddleware();

    if (process.env.NODE_ENV === 'production') {
      return middleware;
    }

    return middleware.concat(actionLogger);
  },
});

export type ChatUiDispatch = typeof chatUiReduxStore.dispatch;

export const useChatUiReduxDispatch = () => useDispatch<ChatUiDispatch>();
export const useChatUiReduxSelector: TypedUseSelectorHook<ChatUiRootState> =
  useSelector;
