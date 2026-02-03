import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { UserInfo } from '@/types/auth';

interface AuthState {
    token: string | null;
    refreshToken: string | null;
    tokenExpiresAt: number | null;
    user: UserInfo | null;
    isAuthenticated: boolean;
    /** 登录成功后设置认证状态 */
    setAuth: (accessToken: string, refreshToken: string, expiresIn: number, user: UserInfo) => void;
    /** 静默刷新时更新 Token */
    setTokens: (accessToken: string, refreshToken: string, expiresIn: number) => void;
    /** 更新用户信息 */
    updateUser: (user: Partial<UserInfo>) => void;
    logout: () => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            token: null,
            refreshToken: null,
            tokenExpiresAt: null,
            user: null,
            isAuthenticated: false,

            setAuth: (accessToken, refreshToken, expiresIn, user) =>
                set({
                    token: accessToken,
                    refreshToken,
                    tokenExpiresAt: Date.now() + expiresIn * 1000,
                    user,
                    isAuthenticated: true,
                }),

            setTokens: (accessToken, refreshToken, expiresIn) =>
                set({
                    token: accessToken,
                    refreshToken,
                    tokenExpiresAt: Date.now() + expiresIn * 1000,
                }),

            updateUser: (updates) =>
                set((state) => ({
                    user: state.user ? { ...state.user, ...updates } : null,
                })),

            logout: () =>
                set({
                    token: null,
                    refreshToken: null,
                    tokenExpiresAt: null,
                    user: null,
                    isAuthenticated: false,
                }),
        }),
        {
            name: 'hr-ai-auth',
        }
    )
);
