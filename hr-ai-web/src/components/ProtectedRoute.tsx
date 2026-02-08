import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { Spin } from 'antd';
import { useAuthStore } from '@/stores/useAuthStore';
import { usePermissionStore } from '@/stores/usePermissionStore';
import { getCurrentUserApi } from '@/services/auth';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, token, updateUser } = useAuthStore();
  const loadPermissions = usePermissionStore((state) => state.loadPermissions);
  const clearPermissions = usePermissionStore((state) => state.clearPermissions);
  const permissionsLoaded = usePermissionStore((state) => state.loaded);
  const [checked, setChecked] = useState(false);

  const checking = isAuthenticated && token && !checked;

  useEffect(() => {
    if (!checking) {
      return;
    }

    let active = true;
    getCurrentUserApi()
      .then((userInfo) => {
        if (active) {
          updateUser(userInfo);
        }
      })
      .catch(() => {
        // Token 无效时，request.ts 拦截器会尝试 refresh
      })
      .finally(() => {
        if (active) {
          setChecked(true);
        }
      });

    return () => {
      active = false;
    };
  }, [checking, updateUser]);

  useEffect(() => {
    if (!isAuthenticated || !checked || permissionsLoaded) {
      return;
    }
    loadPermissions();
  }, [checked, isAuthenticated, loadPermissions, permissionsLoaded]);

  useEffect(() => {
    if (!isAuthenticated) {
      clearPermissions();
    }
  }, [clearPermissions, isAuthenticated]);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (checking || !permissionsLoaded) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" tip="验证登录状态与权限中..." />
      </div>
    );
  }

  return <>{children}</>;
};

export default ProtectedRoute;
