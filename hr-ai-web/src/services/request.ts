import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { message } from 'antd';
import { useAuthStore } from '@/stores/useAuthStore';

// 创建 axios 实例
const request: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json',
    },
});

// ---------- Token 静默刷新相关 ----------
let isRefreshing = false;
let pendingRequests: Array<(token: string) => void> = [];

function onTokenRefreshed(newToken: string) {
    pendingRequests.forEach((cb) => cb(newToken));
    pendingRequests = [];
}

function addPendingRequest(cb: (token: string) => void) {
    pendingRequests.push(cb);
}

// 请求拦截器
request.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const { token } = useAuthStore.getState();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error: AxiosError) => {
        return Promise.reject(error);
    }
);

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        const { code, message: msg, data } = response.data;

        // Result<T> 包装格式
        if (code !== undefined) {
            if (code === 200) {
                return data;
            } else {
                message.error(msg || '请求失败');
                return Promise.reject(new Error(msg || '请求失败'));
            }
        }

        return response.data;
    },
    async (error: AxiosError) => {
        const { response, config } = error;

        if (response && response.status === 401 && config) {
            const { refreshToken, logout } = useAuthStore.getState();

            // 没有 refreshToken，直接登出
            if (!refreshToken) {
                logout();
                window.location.href = '/login';
                return Promise.reject(error);
            }

            // 如果正在刷新，将请求加入等待队列
            if (isRefreshing) {
                return new Promise((resolve) => {
                    addPendingRequest((newToken: string) => {
                        if (config.headers) {
                            config.headers.Authorization = `Bearer ${newToken}`;
                        }
                        resolve(request(config));
                    });
                });
            }

            isRefreshing = true;

            try {
                // 用原生 axios 发刷新请求，绕过拦截器避免死循环
                const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
                const res = await axios.post(`${baseURL}/v1/auth/refresh`, {
                    refreshToken,
                });

                const result = res.data;
                if (result.code === 200 && result.data) {
                    const { accessToken, refreshToken: newRefreshToken, expiresIn } = result.data;
                    useAuthStore.getState().setTokens(accessToken, newRefreshToken, expiresIn);

                    // 通知等待队列
                    onTokenRefreshed(accessToken);

                    // 重试原始请求
                    if (config.headers) {
                        config.headers.Authorization = `Bearer ${accessToken}`;
                    }
                    return request(config);
                } else {
                    // 刷新失败，登出
                    logout();
                    window.location.href = '/login';
                    return Promise.reject(error);
                }
            } catch {
                // 刷新请求本身失败，登出
                useAuthStore.getState().logout();
                window.location.href = '/login';
                return Promise.reject(error);
            } finally {
                isRefreshing = false;
            }
        }

        // 非 401 错误处理
        if (response) {
            const { status } = response;
            if (status === 403) {
                message.error('没有权限');
            } else if (status === 500) {
                message.error('服务器错误');
            } else {
                message.error(`请求失败: ${status}`);
            }
        } else {
            message.error(error.message || '网络异常');
        }

        return Promise.reject(error);
    }
);

export default request;
