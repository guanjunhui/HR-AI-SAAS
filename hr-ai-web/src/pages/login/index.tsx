import React, { useState } from 'react';
import { Form, Input, Button, Checkbox, message, Typography } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/useAuthStore';

const { Title, Text } = Typography;

const LoginPage: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const login = useAuthStore(state => state.login);
    const navigate = useNavigate();

    const onFinish = async (values: any) => {
        setLoading(true);
        try {
            console.log('Received values of form: ', values);

            // Mock login for now
            setTimeout(() => {
                const mockUser = {
                    id: '1',
                    username: values.username,
                    name: 'Admin User',
                    roles: ['admin'],
                    tenantId: '1'
                };
                login('mock-jwt-token', mockUser);
                message.success('登录成功');
                navigate('/');
                setLoading(false);
            }, 1000);

        } catch (error) {
            console.error(error);
            setLoading(false);
        }
    };

    return (
        <div style={{
            display: 'flex',
            height: '100vh',
            overflow: 'hidden'
        }}>
            {/* Left Side - Illustration */}
            <div style={{
                flex: 1,
                background: 'linear-gradient(135deg, #001529 0%, #1677ff 100%)',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'center',
                alignItems: 'center',
                color: 'white',
                padding: '40px'
            }}>
                <div style={{ textAlign: 'center' }}>
                    <Title level={1} style={{ color: 'white', marginBottom: '10px' }}>HR AI SaaS</Title>
                    <Text style={{ color: 'rgba(255,255,255,0.8)', fontSize: '18px' }}>
                        连接人与AI，打造智能人力资源管理新体验
                    </Text>
                </div>
            </div>

            {/* Right Side - Login Form */}
            <div style={{
                flex: '0 0 500px',
                background: 'white',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'center',
                padding: '60px'
            }}>
                <div style={{ marginBottom: '40px' }}>
                    <Title level={2}>欢迎回来</Title>
                    <Text type="secondary">请登录您的账户以访问工作台</Text>
                </div>

                <Form
                    name="login_form"
                    initialValues={{ remember: true }}
                    onFinish={onFinish}
                    layout="vertical"
                    size="large"
                >
                    <Form.Item
                        name="username"
                        label="账号"
                        rules={[{ required: true, message: '请输入账号!' }]}
                    >
                        <Input prefix={<UserOutlined />} placeholder="Username" />
                    </Form.Item>
                    <Form.Item
                        name="password"
                        label="密码"
                        rules={[{ required: true, message: '请输入密码!' }]}
                    >
                        <Input.Password prefix={<LockOutlined />} placeholder="Password" />
                    </Form.Item>
                    <Form.Item>
                        <Form.Item name="remember" valuePropName="checked" noStyle>
                            <Checkbox>记住我</Checkbox>
                        </Form.Item>

                        <a style={{ float: 'right' }} href="">
                            忘记密码?
                        </a>
                    </Form.Item>

                    <Form.Item>
                        <Button type="primary" htmlType="submit" block loading={loading}>
                            登 录
                        </Button>
                    </Form.Item>

                    <div style={{ textAlign: 'center', marginTop: '20px' }}>
                        <Text type="secondary">还没有账号? </Text>
                        <a href="">立即注册</a>
                    </div>
                </Form>

                <div style={{ marginTop: 'auto', textAlign: 'center' }}>
                    <Text type="secondary" style={{ fontSize: '12px' }}>Powered by Enterprise AI</Text>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
