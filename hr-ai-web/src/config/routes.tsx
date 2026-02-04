import { Navigate, createBrowserRouter } from 'react-router-dom';
import LoginPage from '@/pages/login';
import BasicLayout from '@/layouts/BasicLayout';
import ProtectedRoute from '@/components/ProtectedRoute';

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
            {
                index: true,
                lazy: async () => {
                    const mod = await import('@/pages/placeholder');
                    return { Component: mod.DashboardPage };
                },
            },
            {
                path: 'org',
                children: [
                    { index: true, element: <Navigate to="/org/units" replace /> },
                    {
                        path: 'units',
                        lazy: async () => {
                            const mod = await import('@/pages/org/units');
                            return { Component: mod.default };
                        },
                    },
                    {
                        path: 'units/:id',
                        lazy: async () => {
                            const mod = await import('@/pages/org/units/detail');
                            return { Component: mod.default };
                        },
                    },
                    {
                        path: 'users',
                        lazy: async () => {
                            const mod = await import('@/pages/org/users');
                            return { Component: mod.default };
                        },
                    },
                    {
                        path: 'users/:id',
                        lazy: async () => {
                            const mod = await import('@/pages/org/users/detail');
                            return { Component: mod.default };
                        },
                    },
                    {
                        path: 'roles',
                        lazy: async () => {
                            const mod = await import('@/pages/org/roles');
                            return { Component: mod.default };
                        },
                    },
                    {
                        path: 'roles/:id',
                        lazy: async () => {
                            const mod = await import('@/pages/org/roles/detail');
                            return { Component: mod.default };
                        },
                    },
                    {
                        path: 'audit',
                        lazy: async () => {
                            const mod = await import('@/pages/org/audit');
                            return { Component: mod.default };
                        },
                    },
                ],
            },
            {
                path: 'hr/*',
                lazy: async () => {
                    const mod = await import('@/pages/placeholder');
                    return { Component: mod.HRPlaceholderPage };
                },
            },
            {
                path: 'recruiting/*',
                lazy: async () => {
                    const mod = await import('@/pages/placeholder');
                    return { Component: mod.RecruitingPlaceholderPage };
                },
            },
            {
                path: 'attendance/*',
                lazy: async () => {
                    const mod = await import('@/pages/placeholder');
                    return { Component: mod.AttendancePlaceholderPage };
                },
            },
            {
                path: 'payroll/*',
                lazy: async () => {
                    const mod = await import('@/pages/placeholder');
                    return { Component: mod.PayrollPlaceholderPage };
                },
            },
            {
                path: 'performance/*',
                lazy: async () => {
                    const mod = await import('@/pages/placeholder');
                    return { Component: mod.PerformancePlaceholderPage };
                },
            },
            {
                path: 'ai/*',
                lazy: async () => {
                    const mod = await import('@/pages/placeholder');
                    return { Component: mod.AIHubPlaceholderPage };
                },
            },
        ],
    },
    {
        path: '*',
        element: <div>404 Not Found</div>
    }
]);
