import React, { useEffect, useState } from 'react';
import { Navigate, createBrowserRouter } from 'react-router-dom';
import { Spin } from 'antd';
import LoginPage from '@/pages/login';
import BasicLayout from '@/layouts/BasicLayout';
import { useAuthStore } from '@/stores/useAuthStore';
import { getCurrentUserApi } from '@/services/auth';

// Initialize pages with lazy loading placeholders for now
const Dashboard = () => <div><h2>Dashboard</h2><p>Welcome to HR AI SaaS Workbench.</p></div>;
const Org = () => <div><h2>Organization Management</h2></div>;
const HR = () => <div><h2>Core HR</h2></div>;
const Recruiting = () => <div><h2>Recruiting</h2></div>;
const Attendance = () => <div><h2>Attendance</h2></div>;
const Payroll = () => <div><h2>Payroll</h2></div>;
const Performance = () => <div><h2>Performance</h2></div>;
const AIHub = () => <div><h2>AI Capability Center</h2></div>;

// 路由守卫：验证 Token 有效性
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
    const { isAuthenticated, token, updateUser } = useAuthStore();
    const [checking, setChecking] = useState(false);
    const [checked, setChecked] = useState(false);

    useEffect(() => {
        // 仅首次挂载时验证 Token
        if (isAuthenticated && token && !checked) {
            setChecking(true);
            getCurrentUserApi()
                .then((userInfo) => {
                    updateUser(userInfo);
                })
                .catch(() => {
                    // Token 无效时，request.ts 拦截器会尝试 refresh，
                    // 最终失败会自动 logout 并跳转登录页
                })
                .finally(() => {
                    setChecking(false);
                    setChecked(true);
                });
        }
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (checking) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <Spin size="large" tip="验证登录状态..." />
            </div>
        );
    }

    return children;
};

export const router = createBrowserRouter([
    {
        path: '/login',
        element: <LoginPage />,
    },
    {
        path: '/',
        element: (
            <ProtectedRoute>
                <BasicLayout />
            </ProtectedRoute>
        ),
        children: [
            { path: '', element: <Dashboard /> },
            { path: 'org/*', element: <Org /> },
            { path: 'hr/*', element: <HR /> },
            { path: 'recruiting/*', element: <Recruiting /> },
            { path: 'attendance/*', element: <Attendance /> },
            { path: 'payroll/*', element: <Payroll /> },
            { path: 'performance/*', element: <Performance /> },
            { path: 'ai/*', element: <AIHub /> },
        ],
    },
    {
        path: '*',
        element: <div>404 Not Found</div>
    }
]);
