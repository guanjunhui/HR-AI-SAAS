import React, { useEffect, useMemo, useState } from 'react';
import { Layout, Menu, Button, Space, Avatar, Input, type MenuProps } from 'antd';
import {
    MenuFoldOutlined,
    MenuUnfoldOutlined,
    DashboardOutlined,
    UserOutlined,
    TeamOutlined,
    ScheduleOutlined,
    PayCircleOutlined,
    LineChartOutlined,
    RobotOutlined,
    BellOutlined,
    SearchOutlined
} from '@ant-design/icons';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/useAuthStore';
import { logoutApi } from '@/services/auth';

const { Header, Sider, Content } = Layout;

const getMenuState = (pathname: string) => {
    if (pathname.startsWith('/org/units')) {
        return { selectedKeys: ['/org/units'], openKeys: ['/org'] };
    }
    if (pathname.startsWith('/org/users')) {
        return { selectedKeys: ['/org/users'], openKeys: ['/org'] };
    }
    if (pathname.startsWith('/org/roles')) {
        return { selectedKeys: ['/org/roles'], openKeys: ['/org'] };
    }
    if (pathname.startsWith('/org/audit')) {
        return { selectedKeys: ['/org/audit'], openKeys: ['/org'] };
    }
    if (pathname.startsWith('/org')) {
        return { selectedKeys: ['/org/units'], openKeys: ['/org'] };
    }
    const rootKey = pathname === '/' ? '/' : `/${pathname.split('/')[1]}`;
    return { selectedKeys: [rootKey], openKeys: [] };
};

const BasicLayout: React.FC = () => {
    const [collapsed, setCollapsed] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();
    const { user, logout } = useAuthStore();
    const menuState = useMemo(() => getMenuState(location.pathname), [location.pathname]);
    const [openKeys, setOpenKeys] = useState<string[]>(menuState.openKeys);

    const handleMenuClick: MenuProps['onClick'] = (info) => {
        navigate(String(info.key));
    };

    useEffect(() => {
        setOpenKeys(menuState.openKeys);
    }, [menuState]);

    const menuItems: MenuProps['items'] = [
        { key: '/', icon: <DashboardOutlined />, label: '工作台' },
        {
            key: '/org',
            icon: <TeamOutlined />,
            label: '组织与权限',
            children: [
                { key: '/org/units', label: '组织架构' },
                { key: '/org/users', label: '用户管理' },
                { key: '/org/roles', label: '角色管理' },
                { key: '/org/audit', label: '审计日志' },
            ],
        },
        { key: '/hr', icon: <UserOutlined />, label: 'Core HR' },
        { key: '/recruiting', icon: <TeamOutlined />, label: '招聘管理' },
        { key: '/attendance', icon: <ScheduleOutlined />, label: '考勤假勤' },
        { key: '/payroll', icon: <PayCircleOutlined />, label: '薪酬薪资' },
        { key: '/performance', icon: <LineChartOutlined />, label: '绩效管理' },
        { key: '/ai', icon: <RobotOutlined />, label: 'AI 能力中心' },
    ];

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Sider trigger={null} collapsible collapsed={collapsed} width={240} style={{
                overflow: 'auto',
                height: '100vh',
                position: 'fixed',
                left: 0,
                top: 0,
                bottom: 0,
                zIndex: 100
            }}>
                {/* Logo */}
                <div style={{
                    height: 64,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    background: '#002140'
                }}>
                    <div style={{
                        color: 'white',
                        fontWeight: 'bold',
                        fontSize: collapsed ? '16px' : '20px',
                        whiteSpace: 'nowrap'
                    }}>
                        {collapsed ? 'AI HR' : 'HR AI SaaS'}
                    </div>
                </div>

                <Menu
                    theme="dark"
                    mode="inline"
                    selectedKeys={menuState.selectedKeys}
                    openKeys={openKeys}
                    onOpenChange={(keys) => setOpenKeys(keys as string[])}
                    items={menuItems}
                    onClick={handleMenuClick}
                />

                {!collapsed && (
                    <div style={{
                        position: 'absolute',
                        bottom: 20,
                        width: '100%',
                        padding: '0 20px'
                    }}>
                        <div style={{
                            background: 'rgba(255,255,255,0.1)',
                            borderRadius: '8px',
                            padding: '12px',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '10px'
                        }}>
                            <Avatar src={user?.avatar || undefined} icon={!user?.avatar ? <UserOutlined /> : undefined} />
                            <div style={{ overflow: 'hidden' }}>
                                <div style={{ color: 'white', fontWeight: '500', whiteSpace: 'nowrap', textOverflow: 'ellipsis', overflow: 'hidden' }}>
                                    {user?.realName || user?.username || 'User'}
                                </div>
                                <div style={{ color: 'rgba(255,255,255,0.6)', fontSize: '12px' }}>{user?.roleName || ''}</div>
                            </div>
                        </div>
                    </div>
                )}
            </Sider>

            <Layout style={{ marginLeft: collapsed ? 80 : 240, transition: 'all 0.2s' }}>
                <Header style={{
                    padding: '0 24px',
                    background: 'white',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    position: 'sticky',
                    top: 0,
                    zIndex: 99,
                    boxShadow: '0 1px 4px rgba(0,21,41,0.08)'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center' }}>
                        <Button
                            type="text"
                            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                            onClick={() => setCollapsed(!collapsed)}
                            style={{ fontSize: '16px', width: 64, height: 64 }}
                        />

                        <Input
                            prefix={<SearchOutlined style={{ color: '#bfbfbf' }} />}
                            placeholder="Ask AI or search..."
                            style={{ width: 300, borderRadius: '20px', background: '#f5f5f5', border: 'none' }}
                        />
                    </div>

                    <Space size="large">
                        <Button type="text" shape="circle" icon={<BellOutlined />} />
                        <Button type="primary" shape="round" icon={<RobotOutlined />}>AI Assistant</Button>
                        <Button type="link" onClick={() => { logoutApi().catch(() => {}); logout(); }}>Logout</Button>
                    </Space>
                </Header>

                <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280, background: '#fff', borderRadius: '8px' }}>
                    <Outlet />
                </Content>
            </Layout>
        </Layout>
    );
};

export default BasicLayout;
