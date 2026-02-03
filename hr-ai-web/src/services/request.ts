import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { message } from 'antd';
import { useAuthStore } from '@/stores/useAuthStore';

// Create axios instance
const request: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor
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

// Response interceptor
request.interceptors.response.use(
    (response) => {
        const { code, message: msg, data } = response.data;

        // If wrapping response (Result object)
        if (code !== undefined) {
            if (code === 200) {
                return data;
            } else {
                message.error(msg || 'Request failed');
                return Promise.reject(new Error(msg || 'Request failed'));
            }
        }

        return response.data;
    },
    (error: AxiosError) => {
        const { response } = error;

        if (response) {
            const { status } = response;
            if (status === 401) {
                useAuthStore.getState().logout();
                window.location.href = '/login';
                message.error('Session expired, please login again');
            } else if (status === 403) {
                message.error('Permission denied');
            } else if (status === 500) {
                message.error('Server error');
            } else {
                message.error(`Request failed: ${status}`);
            }
        } else {
            message.error(error.message || 'Network Error');
        }

        return Promise.reject(error);
    }
);

export default request;
