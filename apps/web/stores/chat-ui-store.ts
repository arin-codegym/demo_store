import { create } from 'zustand';

type SocketStatus = 'disconnected' | 'connecting' | 'connected';
type ConversationWidgetType = 'USER_ADMIN' | 'DIRECT' | 'USER_DIRECT' | null;

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

  setSocketStatus: (status: SocketStatus) => void;
  setStompConnected: (connected: boolean) => void;
  setSelectedConversationId: (conversationId: string | null) => void;

  setAdminConversationId: (id: string | null) => void;
  setAdminWidgetOpen: (open: boolean) => void;

  setAiConversationId: (id: string | null) => void;
  setAiWidgetOpen: (open: boolean) => void;

  setConversationWidgetConversationId: (id: string | null) => void;
  setConversationWidgetType: (type: ConversationWidgetType) => void;
  setConversationWidgetOpen: (open: boolean) => void;

  openConversationWidget: (
    conversationId: string,
    type: Exclude<ConversationWidgetType, null>,
  ) => void;
  closeConversationWidget: () => void;

  reset: () => void;
};

const initialState = {
  selectedConversationId: null,
  socketStatus: 'disconnected' as SocketStatus,
  stompConnected: false,

  adminConversationId: null,
  adminWidgetOpen: false,

  aiConversationId: null,
  aiWidgetOpen: false,

  conversationWidgetConversationId: null,
  conversationWidgetType: null as ConversationWidgetType,
  conversationWidgetOpen: false,
};

export const useChatUiStore = create<ChatUiState>((set) => ({
  ...initialState,

  setSocketStatus: (status) => set({ socketStatus: status }),
  setStompConnected: (connected) => set({ stompConnected: connected }),

  setSelectedConversationId: (conversationId) =>
    set({ selectedConversationId: conversationId }),

  setAdminConversationId: (id) => set({ adminConversationId: id }),
  setAdminWidgetOpen: (open) => set({ adminWidgetOpen: open }),

  setAiConversationId: (id) => set({ aiConversationId: id }),
  setAiWidgetOpen: (open) => set({ aiWidgetOpen: open }),

  setConversationWidgetConversationId: (id) =>
    set({ conversationWidgetConversationId: id }),
  setConversationWidgetType: (type) => set({ conversationWidgetType: type }),
  setConversationWidgetOpen: (open) => set({ conversationWidgetOpen: open }),

  openConversationWidget: (conversationId, type) =>
    set({
      conversationWidgetConversationId: conversationId,
      conversationWidgetType: type,
      conversationWidgetOpen: true,
      adminWidgetOpen: false,
      aiWidgetOpen: false,
    }),

  closeConversationWidget: () =>
    set({
      conversationWidgetOpen: false,
      conversationWidgetConversationId: null,
      conversationWidgetType: null,
    }),

  reset: () => set(initialState),
}));
