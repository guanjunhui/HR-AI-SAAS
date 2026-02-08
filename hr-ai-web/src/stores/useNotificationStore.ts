import { create } from 'zustand';

export type NoticeType = 'success' | 'info' | 'warning' | 'error';

export interface AppNotice {
  id: string;
  type: NoticeType;
  title: string;
  description?: string;
  createdAt: number;
}

interface NotificationState {
  notices: AppNotice[];
  pushNotice: (notice: Omit<AppNotice, 'id' | 'createdAt'>) => void;
  removeNotice: (id: string) => void;
  clearNotices: () => void;
}

export const useNotificationStore = create<NotificationState>((set) => ({
  notices: [],

  pushNotice: (notice) => {
    const id = `${Date.now()}-${Math.random().toString(16).slice(2)}`;
    set((state) => ({
      notices: [...state.notices, { ...notice, id, createdAt: Date.now() }],
    }));
  },

  removeNotice: (id) => {
    set((state) => ({ notices: state.notices.filter((notice) => notice.id !== id) }));
  },

  clearNotices: () => {
    set({ notices: [] });
  },
}));
