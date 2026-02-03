import React from 'react';
import { Navigate, createBrowserRouter } from 'react-router-dom';
import LoginPage from '@/pages/login';
import BasicLayout from '@/layouts/BasicLayout';
import { useAuthStore } from '@/stores/useAuthStore';

// Initialize pages with lazy loading placeholders for now
const Dashboard = () => <div><h2>Dashboard</h2><p>Welcome to HR AI SaaS Workbench.</p></div>;
const Org = () => <div><h2>Organization Management</h2></div>;
const HR = () => <div><h2>Core HR</h2></div>;
const Recruiting = () => <div><h2>Recruiting</h2></div>;
const Attendance = () => <div><h2>Attendance</h2></div>;
const Payroll = () => <div><h2>Payroll</h2></div>;
const Performance = () => <div><h2>Performance</h2></div>;
const AIHub = () => <div><h2>AI Capability Center</h2></div>;

// Route Guard
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
    const { isAuthenticated } = useAuthStore();
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
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
