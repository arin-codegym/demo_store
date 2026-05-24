import { create } from 'zustand';

export type AdminNotice = {
  id: string;
  type: 'new-message';
  conversationId: string;
  senderName?: string;
  content?: string;
  createdAt: string;
  read: boolean;
};

type NotificationStore = {
  notices: AdminNotice[];
  addNotice: (notice: AdminNotice) => void;
  markNoticeRead: (id: string) => void;
  markAllNoticesRead: () => void;
  removeNotice: (id: string) => void;
};

export const useNotificationStore = create<NotificationStore>((set) => ({
  notices: [],

  addNotice: (notice) =>
    set((state) => ({
      notices: [notice, ...state.notices].slice(0, 50),
    })),

  markNoticeRead: (id) =>
    set((state) => ({
      notices: state.notices.map((item) =>
        item.id === id ? { ...item, read: true } : item,
      ),
    })),

  markAllNoticesRead: () =>
    set((state) => ({
      notices: state.notices.map((item) => ({ ...item, read: true })),
    })),

  removeNotice: (id) =>
    set((state) => ({
      notices: state.notices.filter((item) => item.id !== id),
    })),
}));
