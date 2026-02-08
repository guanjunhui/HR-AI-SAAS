import { Navigate, createBrowserRouter } from 'react-router-dom';
import type React from 'react';
import LoginPage from '@/pages/login';
import BasicLayout from '@/layouts/BasicLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import PermissionGuard from '@/components/PermissionGuard';

function wrapWithPermission(Component: React.ComponentType, permission?: string): React.ComponentType {
  const Wrapped: React.FC = () => (
    <PermissionGuard permission={permission}>
      <Component />
    </PermissionGuard>
  );
  return Wrapped;
}

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
              return { Component: wrapWithPermission(mod.default, 'org:unit:read') };
            },
          },
          {
            path: 'units/:id',
            lazy: async () => {
              const mod = await import('@/pages/org/units/detail');
              return { Component: wrapWithPermission(mod.default, 'org:unit:read') };
            },
          },
          {
            path: 'users',
            lazy: async () => {
              const mod = await import('@/pages/org/users');
              return { Component: wrapWithPermission(mod.default, 'org:user:read') };
            },
          },
          {
            path: 'users/:id',
            lazy: async () => {
              const mod = await import('@/pages/org/users/detail');
              return { Component: wrapWithPermission(mod.default, 'org:user:read') };
            },
          },
          {
            path: 'roles',
            lazy: async () => {
              const mod = await import('@/pages/org/roles');
              return { Component: wrapWithPermission(mod.default, 'org:role:read') };
            },
          },
          {
            path: 'roles/:id',
            lazy: async () => {
              const mod = await import('@/pages/org/roles/detail');
              return { Component: wrapWithPermission(mod.default, 'org:role:read') };
            },
          },
          {
            path: 'audit',
            lazy: async () => {
              const mod = await import('@/pages/org/audit');
              return { Component: wrapWithPermission(mod.default, 'org:audit:read') };
            },
          },
        ],
      },
      {
        path: 'hr',
        children: [
          { index: true, element: <Navigate to="/hr/employees" replace /> },
          {
            path: 'positions',
            lazy: async () => {
              const mod = await import('@/pages/hr/positions');
              return { Component: wrapWithPermission(mod.default, 'hr:position:read') };
            },
          },
          {
            path: 'headcounts',
            lazy: async () => {
              const mod = await import('@/pages/hr/headcounts');
              return { Component: wrapWithPermission(mod.default, 'hr:headcount:read') };
            },
          },
          {
            path: 'employees',
            lazy: async () => {
              const mod = await import('@/pages/hr/employees');
              return { Component: wrapWithPermission(mod.default, 'hr:employee:read') };
            },
          },
          {
            path: 'employees/:id',
            lazy: async () => {
              const mod = await import('@/pages/hr/employees/detail');
              return { Component: wrapWithPermission(mod.default, 'hr:employee:read') };
            },
          },
          {
            path: 'events',
            lazy: async () => {
              const mod = await import('@/pages/hr/events');
              return { Component: wrapWithPermission(mod.default, 'hr:event:read') };
            },
          },
        ],
      },
      {
        path: 'onboarding',
        children: [
          { index: true, element: <Navigate to="/onboarding/autofill" replace /> },
          {
            path: 'autofill',
            lazy: async () => {
              const mod = await import('@/pages/onboarding/autofill');
              return { Component: wrapWithPermission(mod.default, 'onboarding:write') };
            },
          },
        ],
      },
      {
        path: 'recruiting',
        children: [
          { index: true, element: <Navigate to="/recruiting/resume-parser" replace /> },
          {
            path: 'resume-parser',
            lazy: async () => {
              const mod = await import('@/pages/recruiting/resume-parser');
              return { Component: wrapWithPermission(mod.default, 'recruiting:candidate:write') };
            },
          },
        ],
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
        path: 'performance',
        children: [
          { index: true, element: <Navigate to="/performance/prediction" replace /> },
          {
            path: 'prediction',
            lazy: async () => {
              const mod = await import('@/pages/performance/prediction');
              return { Component: wrapWithPermission(mod.default, 'performance:review:read') };
            },
          },
        ],
      },
      {
        path: 'ai',
        children: [
          { index: true, element: <Navigate to="/ai/insights/turnover" replace /> },
          {
            path: 'insights/turnover',
            lazy: async () => {
              const mod = await import('@/pages/ai/risk-turnover');
              return { Component: wrapWithPermission(mod.default, 'ai:risk:read') };
            },
          },
          {
            path: 'risk-turnover',
            element: <Navigate to="/ai/insights/turnover" replace />,
          },
        ],
      },
    ],
  },
  {
    path: '*',
    element: <div>404 Not Found</div>,
  },
]);
