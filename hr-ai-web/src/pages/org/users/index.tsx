import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Button,
  Card,
  Form,
  Input,
  message,
  Modal,
  Select,
  Space,
  Table,
  Tag,
  TreeSelect,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  DeleteOutlined,
  EditOutlined,
  KeyOutlined,
  PlusOutlined,
  ReloadOutlined,
  SafetyOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import type { OrgUnitTreeNode } from '@/types/org';
import type { RoleDetailResponse } from '@/types/role';
import type { UserCreateRequest, UserDetailResponse, UserQueryParams, UserUpdateRequest } from '@/types/user';
import { getOrgUnitTreeApi } from '@/services/org';
import { getRolesApi } from '@/services/role';
import {
  createUserApi,
  deleteUserApi,
  getUsersApi,
  updateUserApi,
  updateUserPasswordApi,
  updateUserRoleApi,
  updateUserStatusApi,
} from '@/services/user';
import { PLAN_TYPE_OPTIONS, STATUS_OPTIONS, getStatusLabel } from '@/utils/dicts';

interface UserFormValues {
  username: string;
  password?: string;
  realName?: string;
  email?: string;
  phone?: string;
  avatar?: string;
  orgUnitId?: number | null;
  roleId?: number | null;
  status: number;
  planType?: string;
}

interface TreeSelectNode {
  title: string;
  value: number;
  key: number;
  children?: TreeSelectNode[];
}

const buildTreeSelectData = (nodes: OrgUnitTreeNode[]): TreeSelectNode[] =>
  nodes.map((node) => ({
    title: `${node.name} (${node.code})`,
    value: node.id,
    key: node.id,
    children: node.children ? buildTreeSelectData(node.children) : undefined,
  }));

const UserListPage: React.FC = () => {
  const [form] = Form.useForm<UserQueryParams>();
  const [userForm] = Form.useForm<UserFormValues>();
  const [roleForm] = Form.useForm<{ roleId: number }>();
  const [passwordForm] = Form.useForm<{ newPassword: string }>();
  const [data, setData] = useState<UserDetailResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [query, setQuery] = useState<UserQueryParams>({ pageNo: 1, pageSize: 20 });
  const [pagination, setPagination] = useState({ pageNo: 1, pageSize: 20, total: 0 });
  const [roles, setRoles] = useState<RoleDetailResponse[]>([]);
  const [orgTree, setOrgTree] = useState<OrgUnitTreeNode[]>([]);
  const [userModalOpen, setUserModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<UserDetailResponse | null>(null);
  const [roleModalOpen, setRoleModalOpen] = useState(false);
  const [roleTarget, setRoleTarget] = useState<UserDetailResponse | null>(null);
  const [passwordModalOpen, setPasswordModalOpen] = useState(false);
  const [passwordTarget, setPasswordTarget] = useState<UserDetailResponse | null>(null);
  const navigate = useNavigate();

  const treeSelectData = useMemo(() => buildTreeSelectData(orgTree), [orgTree]);
  const orgUnitMap = useMemo(() => {
    const map = new Map<number, string>();
    const walk = (nodes: OrgUnitTreeNode[]) => {
      nodes.forEach((node) => {
        map.set(node.id, node.name);
        if (node.children) {
          walk(node.children);
        }
      });
    };
    walk(orgTree);
    return map;
  }, [orgTree]);

  const loadOptions = useCallback(async () => {
    try {
      const [roleData, orgData] = await Promise.all([getRolesApi(), getOrgUnitTreeApi()]);
      setRoles(roleData || []);
      setOrgTree(orgData || []);
    } catch {
      // errors handled by request interceptor
    }
  }, []);

  const loadData = useCallback(async (params: UserQueryParams) => {
    setLoading(true);
    try {
      const res = await getUsersApi(params);
      setData(res.records || []);
      setPagination({
        pageNo: res.pageNo,
        pageSize: res.pageSize,
        total: res.total,
      });
    } catch {
      // errors handled by request interceptor
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadOptions();
  }, [loadOptions]);

  useEffect(() => {
    loadData(query);
  }, [loadData, query]);

  const handleSearch = () => {
    const values = form.getFieldsValue();
    setQuery({
      pageNo: 1,
      pageSize: query.pageSize || 20,
      ...values,
    });
  };

  const handleReset = () => {
    form.resetFields();
    setQuery({ pageNo: 1, pageSize: 20 });
  };

  const openCreate = () => {
    setEditingUser(null);
    userForm.resetFields();
    userForm.setFieldsValue({ status: 1, planType: 'free' });
    setUserModalOpen(true);
  };

  const openEdit = (record: UserDetailResponse) => {
    setEditingUser(record);
    userForm.resetFields();
    userForm.setFieldsValue({
      username: record.username,
      realName: record.realName || undefined,
      email: record.email || undefined,
      phone: record.phone || undefined,
      avatar: record.avatar || undefined,
      orgUnitId: record.orgUnitId ?? null,
      roleId: record.roleId ?? null,
      status: record.status,
      planType: record.planType || 'free',
    });
    setUserModalOpen(true);
  };

  const handleUserSubmit = async () => {
    const values = await userForm.validateFields();
    try {
      if (editingUser) {
        const payload: UserUpdateRequest = {
          username: values.username,
          realName: values.realName,
          email: values.email,
          phone: values.phone,
          avatar: values.avatar,
          orgUnitId: values.orgUnitId ?? null,
          status: values.status,
          planType: values.planType,
        };
        await updateUserApi(editingUser.id, payload);
        message.success('用户已更新');
      } else {
        const payload: UserCreateRequest = {
          username: values.username,
          password: values.password || '',
          realName: values.realName,
          email: values.email,
          phone: values.phone,
          avatar: values.avatar,
          orgUnitId: values.orgUnitId ?? null,
          roleId: Number(values.roleId),
          status: values.status,
          planType: values.planType,
        };
        await createUserApi(payload);
        message.success('用户已创建');
      }
      setUserModalOpen(false);
      setQuery({ ...query, pageNo: 1 });
    } catch {
      // errors handled by request interceptor
    }
  };

  const handleDelete = (record: UserDetailResponse) => {
    Modal.confirm({
      title: `确认删除用户 ${record.username}？`,
      content: '删除后不可恢复。',
      okType: 'danger',
      okText: '删除',
      cancelText: '取消',
      async onOk() {
        try {
          await deleteUserApi(record.id);
          message.success('用户已删除');
          setQuery({ ...query, pageNo: 1 });
        } catch {
          // errors handled by request interceptor
        }
      },
    });
  };

  const handleToggleStatus = async (record: UserDetailResponse) => {
    const nextStatus = record.status === 1 ? 0 : 1;
    try {
      await updateUserStatusApi(record.id, { status: nextStatus });
      message.success('状态已更新');
      setQuery({ ...query });
    } catch {
      // errors handled by request interceptor
    }
  };

  const openRoleModal = (record: UserDetailResponse) => {
    setRoleTarget(record);
    roleForm.resetFields();
    roleForm.setFieldsValue({ roleId: record.roleId ? Number(record.roleId) : undefined });
    setRoleModalOpen(true);
  };

  const handleRoleSubmit = async () => {
    const values = await roleForm.validateFields();
    if (!roleTarget) return;
    try {
      await updateUserRoleApi(roleTarget.id, { roleId: values.roleId });
      message.success('角色已更新');
      setRoleModalOpen(false);
      setQuery({ ...query });
    } catch {
      // errors handled by request interceptor
    }
  };

  const openPasswordModal = (record: UserDetailResponse) => {
    setPasswordTarget(record);
    passwordForm.resetFields();
    setPasswordModalOpen(true);
  };

  const handlePasswordSubmit = async () => {
    const values = await passwordForm.validateFields();
    if (!passwordTarget) return;
    try {
      await updateUserPasswordApi(passwordTarget.id, { newPassword: values.newPassword });
      message.success('密码已重置');
      setPasswordModalOpen(false);
    } catch {
      // errors handled by request interceptor
    }
  };

  const columns: ColumnsType<UserDetailResponse> = [
    {
      title: '账号',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: '姓名',
      dataIndex: 'realName',
      key: 'realName',
      render: (value) => value || '-',
    },
    {
      title: '组织',
      dataIndex: 'orgUnitId',
      key: 'orgUnitId',
      render: (value: number | undefined) => (value ? orgUnitMap.get(value) || value : '-'),
    },
    {
      title: '角色',
      dataIndex: 'roleName',
      key: 'roleName',
      render: (value) => value || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (value: number) => (
        <Tag color={value === 1 ? 'green' : 'red'}>{getStatusLabel(value)}</Tag>
      ),
    },
    {
      title: '最近登录',
      dataIndex: 'lastLoginTime',
      key: 'lastLoginTime',
      render: (value) => value || '-',
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Button type="link" icon={<UserOutlined />} onClick={() => navigate(`/org/users/${record.id}`)}>
            详情
          </Button>
          <Button type="link" icon={<EditOutlined />} onClick={() => openEdit(record)}>
            编辑
          </Button>
          <Button type="link" icon={<SafetyOutlined />} onClick={() => openRoleModal(record)}>
            分配角色
          </Button>
          <Button type="link" icon={<KeyOutlined />} onClick={() => openPasswordModal(record)}>
            重置密码
          </Button>
          <Button type="link" onClick={() => handleToggleStatus(record)}>
            {record.status === 1 ? '停用' : '启用'}
          </Button>
          <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card>
        <Form form={form} layout="inline">
          <Form.Item name="keyword" label="关键词">
            <Input placeholder="账号/姓名" allowClear />
          </Form.Item>
          <Form.Item name="orgUnitId" label="组织">
            <TreeSelect
              style={{ width: 220 }}
              treeData={treeSelectData}
              allowClear
              placeholder="选择组织"
              treeDefaultExpandAll
            />
          </Form.Item>
          <Form.Item name="roleId" label="角色">
            <Select
              style={{ width: 180 }}
              allowClear
              placeholder="选择角色"
              options={roles.map((role) => ({ label: role.name, value: role.id }))}
            />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select style={{ width: 120 }} allowClear placeholder="状态" options={STATUS_OPTIONS} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" onClick={handleSearch}>
                查询
              </Button>
              <Button onClick={handleReset}>重置</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card
        title="用户列表"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => setQuery({ ...query })} />
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
              新增用户
            </Button>
          </Space>
        }
      >
        <Table
          rowKey="id"
          columns={columns}
          dataSource={data}
          loading={loading}
          pagination={{
            current: pagination.pageNo,
            pageSize: pagination.pageSize,
            total: pagination.total,
            onChange: (page, pageSize) => setQuery({ ...query, pageNo: page, pageSize }),
          }}
        />
      </Card>

      <Modal
        title={editingUser ? '编辑用户' : '新增用户'}
        open={userModalOpen}
        onCancel={() => setUserModalOpen(false)}
        onOk={handleUserSubmit}
        destroyOnClose
      >
        <Form form={userForm} layout="vertical">
          <Form.Item name="username" label="账号" rules={[{ required: true, message: '请输入账号' }]}>
            <Input placeholder="账号" />
          </Form.Item>
          {!editingUser && (
            <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
              <Input.Password placeholder="初始化密码" />
            </Form.Item>
          )}
          <Form.Item name="realName" label="姓名">
            <Input placeholder="姓名" />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input placeholder="邮箱" />
          </Form.Item>
          <Form.Item name="phone" label="手机号">
            <Input placeholder="手机号" />
          </Form.Item>
          <Form.Item name="avatar" label="头像URL">
            <Input placeholder="头像地址" />
          </Form.Item>
          <Form.Item name="orgUnitId" label="组织">
            <TreeSelect
              treeData={treeSelectData}
              placeholder="选择组织"
              allowClear
              treeDefaultExpandAll
            />
          </Form.Item>
          <Form.Item
            name="roleId"
            label="角色"
            rules={editingUser ? [] : [{ required: true, message: '请选择角色' }]}
          >
            <Select
              placeholder="选择角色"
              options={roles.map((role) => ({ label: role.name, value: role.id }))}
              disabled={!!editingUser}
            />
          </Form.Item>
          <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
            <Select options={STATUS_OPTIONS} />
          </Form.Item>
          <Form.Item name="planType" label="方案类型">
            <Select options={PLAN_TYPE_OPTIONS} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="分配角色"
        open={roleModalOpen}
        onCancel={() => setRoleModalOpen(false)}
        onOk={handleRoleSubmit}
        destroyOnClose
      >
        <Form form={roleForm} layout="vertical">
          <Form.Item name="roleId" label="角色" rules={[{ required: true, message: '请选择角色' }]}>
            <Select
              placeholder="选择角色"
              options={roles.map((role) => ({ label: role.name, value: role.id }))}
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="重置密码"
        open={passwordModalOpen}
        onCancel={() => setPasswordModalOpen(false)}
        onOk={handlePasswordSubmit}
        destroyOnClose
      >
        <Form form={passwordForm} layout="vertical">
          <Form.Item name="newPassword" label="新密码" rules={[{ required: true, message: '请输入新密码' }]}>
            <Input.Password placeholder="请输入新密码" />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
};

export default UserListPage;
