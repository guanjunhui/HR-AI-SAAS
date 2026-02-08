import React, { useMemo, useState } from 'react';
import { Avatar, Button, Input, Layout, Menu, Space, type MenuProps } from 'antd';
import {
  BellOutlined,
  DashboardOutlined,
  LineChartOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  RobotOutlined,
  SearchOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/useAuthStore';
import { usePermissionStore } from '@/stores/usePermissionStore';
import { logoutApi } from '@/services/auth';

const { Header, Sider, Content } = Layout;

interface MenuItemConfig {
  key: string;
  label: string;
  icon?: React.ReactNode;
  permission?: string;
  children?: MenuItemConfig[];
}

const menuConfig: MenuItemConfig[] = [
  { key: '/', label: '工作台', icon: <DashboardOutlined /> },
  {
    key: '/org',
    label: '组织与权限',
    icon: <TeamOutlined />,
    children: [
      { key: '/org/units', label: '组织架构', permission: 'org:unit:read' },
      { key: '/org/users', label: '用户管理', permission: 'org:user:read' },
      { key: '/org/roles', label: '角色管理', permission: 'org:role:read' },
      { key: '/org/audit', label: '审计日志', permission: 'org:audit:read' },
    ],
  },
  {
    key: '/hr',
    label: 'Core HR',
    icon: <UserOutlined />,
    children: [
      { key: '/hr/positions', label: '岗位管理', permission: 'hr:position:read' },
      { key: '/hr/headcounts', label: '编制管理', permission: 'hr:headcount:read' },
      { key: '/hr/employees', label: '员工管理', permission: 'hr:employee:read' },
      { key: '/hr/events', label: '任职事件', permission: 'hr:event:read' },
    ],
  },
  {
    key: '/onboarding',
    label: '入职管理',
    icon: <TeamOutlined />,
    children: [
      { key: '/onboarding/autofill', label: 'AI 表单补全', permission: 'onboarding:write' },
    ],
  },
  {
    key: '/recruiting',
    label: '招聘管理',
    icon: <TeamOutlined />,
    children: [
      { key: '/recruiting/resume-parser', label: '简历解析回填', permission: 'recruiting:candidate:write' },
    ],
  },
  {
    key: '/performance',
    label: '绩效管理',
    icon: <LineChartOutlined />,
    children: [
      { key: '/performance/prediction', label: '预测与校准', permission: 'performance:review:read' },
    ],
  },
  {
    key: '/ai',
    label: 'AI 能力中心',
    icon: <RobotOutlined />,
    children: [
      { key: '/ai/insights/turnover', label: '离职风险看板', permission: 'ai:risk:read' },
    ],
  },
];

function getMenuState(pathname: string): { selectedKeys: string[]; openKeys: string[] } {
  const candidates = menuConfig.flatMap((item) => (item.children || [item]));
  const directMatch = candidates
    .filter((item) => {
      if (item.key === '/') {
        return pathname === '/';
      }
      return pathname.startsWith(item.key);
    })
    .sort((a, b) => b.key.length - a.key.length)[0];

  if (!directMatch) {
    return { selectedKeys: ['/'], openKeys: [] };
  }

  const parent = menuConfig.find((item) => item.children?.some((child) => child.key === directMatch.key));
  return {
    selectedKeys: [directMatch.key],
    openKeys: parent ? [parent.key] : [],
  };
}

function filterMenuByPermission(items: MenuItemConfig[], hasPermission: (permission?: string) => boolean): MenuProps['items'] {
  return items
    .map((item) => {
      const children = item.children ? filterMenuByPermission(item.children, hasPermission) : undefined;

      if (item.children && (!children || children.length === 0)) {
        return null;
      }

      if (!item.children && !hasPermission(item.permission)) {
        return null;
      }

      return {
        key: item.key,
        icon: item.icon,
        label: item.label,
        children,
      };
    })
    .filter((item) => item !== null);
}

const BasicLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuthStore();
  const hasPermission = usePermissionStore((state) => state.hasPermission);

  const menuItems = useMemo(() => filterMenuByPermission(menuConfig, hasPermission), [hasPermission]);
  const menuState = useMemo(() => getMenuState(location.pathname), [location.pathname]);
  const [openKeys, setOpenKeys] = useState<string[]>([]);
  const resolvedOpenKeys = useMemo(() => {
    if (collapsed) {
      return [];
    }
    if (menuState.openKeys.length === 0) {
      return openKeys;
    }
    const currentRoot = menuState.openKeys[0];
    return openKeys.includes(currentRoot) ? openKeys : menuState.openKeys;
  }, [collapsed, menuState.openKeys, openKeys]);

  const handleMenuClick: MenuProps['onClick'] = (info) => {
    navigate(String(info.key));
  };

  const handleLogout = async () => {
    try {
      await logoutApi();
    } catch {
      // 忽略登出接口异常，继续本地退出
    }
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        width={240}
        style={{ overflow: 'auto', height: '100vh', position: 'fixed', left: 0, top: 0, bottom: 0, zIndex: 100 }}
      >
        <div style={{ height: 64, display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#001529' }}>
          <div style={{ color: '#fff', fontWeight: 700, fontSize: collapsed ? 14 : 18 }}>{collapsed ? 'AI HR' : 'HR AI SaaS'}</div>
        </div>

        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={menuState.selectedKeys}
          openKeys={resolvedOpenKeys}
          onOpenChange={(keys) => setOpenKeys(keys as string[])}
          items={menuItems}
          onClick={handleMenuClick}
        />

        {!collapsed && (
          <div style={{ position: 'absolute', bottom: 20, width: '100%', padding: '0 20px' }}>
            <div style={{ background: 'rgba(255,255,255,0.08)', borderRadius: 8, padding: 12, display: 'flex', gap: 10 }}>
              <Avatar icon={<UserOutlined />} src={user?.avatar || undefined} />
              <div style={{ overflow: 'hidden' }}>
                <div style={{ color: '#fff', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                  {user?.realName || user?.username || 'User'}
                </div>
                <div style={{ color: 'rgba(255,255,255,0.6)', fontSize: 12 }}>{user?.roleName || '-'}</div>
              </div>
            </div>
          </div>
        )}
      </Sider>

      <Layout style={{ marginLeft: collapsed ? 80 : 240, transition: 'all 0.2s' }}>
        <Header
          style={{
            padding: '0 24px',
            background: '#fff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            position: 'sticky',
            top: 0,
            zIndex: 99,
            boxShadow: '0 1px 4px rgba(0,21,41,0.08)',
          }}
        >
          <Space>
            <Button
              type="text"
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              onClick={() => setCollapsed(!collapsed)}
            />
            <Input
              prefix={<SearchOutlined style={{ color: '#999' }} />}
              placeholder="搜索页面或输入 AI 指令"
              style={{ width: 320, borderRadius: 20 }}
            />
          </Space>

          <Space>
            <Button type="text" shape="circle" icon={<BellOutlined />} />
            <Button type="primary" shape="round" icon={<RobotOutlined />} onClick={() => navigate('/ai/insights/turnover')}>AI 看板</Button>
            <Button icon={<LogoutOutlined />} onClick={handleLogout}>退出</Button>
          </Space>
        </Header>

        <Content style={{ margin: '24px 16px', padding: 24, background: '#fff', borderRadius: 8 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default BasicLayout;
