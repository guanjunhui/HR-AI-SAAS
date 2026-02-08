import { create } from 'zustand';
import { getPermissionListApi } from '@/services/permission';
import { hasPermissionByList } from '@/utils/permissionMatcher';

interface PermissionState {
  loaded: boolean;
  loading: boolean;
  permissions: string[];
  setPermissions: (permissions: string[]) => void;
  clearPermissions: () => void;
  hasPermission: (permission?: string) => boolean;
  loadPermissions: () => Promise<void>;
}

export const usePermissionStore = create<PermissionState>((set, get) => ({
  loaded: false,
  loading: false,
  permissions: [],

  setPermissions: (permissions) => {
    set({ permissions, loaded: true, loading: false });
  },

  clearPermissions: () => {
    set({ permissions: [], loaded: false, loading: false });
  },

  hasPermission: (permission) => {
    return hasPermissionByList(get().permissions, permission);
  },

  loadPermissions: async () => {
    if (get().loading || get().loaded) {
      return;
    }
    set({ loading: true });
    try {
      const permissions = await getPermissionListApi();
      set({ permissions: permissions || [], loaded: true, loading: false });
    } catch {
      set({ permissions: [], loaded: true, loading: false });
    }
  },
}));
