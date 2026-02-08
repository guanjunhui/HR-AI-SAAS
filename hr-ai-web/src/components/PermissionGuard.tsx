import React from 'react';
import { Result, Spin } from 'antd';
import { usePermissionStore } from '@/stores/usePermissionStore';

interface PermissionGuardProps {
  permission?: string;
  children: React.ReactNode;
}

const PermissionGuard: React.FC<PermissionGuardProps> = ({ permission, children }) => {
  const loaded = usePermissionStore((state) => state.loaded);
  const hasPermission = usePermissionStore((state) => state.hasPermission(permission));

  if (!loaded) {
    return (
      <div style={{ minHeight: 320, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Spin tip="加载权限中..." />
      </div>
    );
  }

  if (!hasPermission) {
    return <Result status="403" title="403" subTitle="当前账号没有访问该页面的权限。" />;
  }

  return <>{children}</>;
};

export default PermissionGuard;
