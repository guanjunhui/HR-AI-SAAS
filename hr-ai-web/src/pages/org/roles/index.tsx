import React, { useCallback, useEffect, useState } from 'react';
import {
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Select,
  Space,
  Table,
  Tag,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  DeleteOutlined,
  EditOutlined,
  EyeOutlined,
  PlusOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import type { RoleCreateRequest, RoleDetailResponse, RoleUpdateRequest } from '@/types/role';
import { createRoleApi, deleteRoleApi, getRolesApi, updateRoleApi } from '@/services/role';
import { getPermissionListApi } from '@/services/permission';
import { DATA_SCOPE_OPTIONS, STATUS_OPTIONS, getDataScopeLabel, getStatusLabel } from '@/utils/dicts';

interface RoleFormValues {
  name: string;
  code: string;
  description?: string;
  permissions?: string[];
  dataScope: number;
  status: number;
  sortOrder?: number;
}

const RoleListPage: React.FC = () => {
  const [form] = Form.useForm<{ keyword?: string; status?: number }>();
  const [roleForm] = Form.useForm<RoleFormValues>();
  const [data, setData] = useState<RoleDetailResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [query, setQuery] = useState<{ keyword?: string; status?: number }>({});
  const [roleModalOpen, setRoleModalOpen] = useState(false);
  const [editingRole, setEditingRole] = useState<RoleDetailResponse | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const navigate = useNavigate();

  const loadData = useCallback(async (params: { keyword?: string; status?: number }) => {
    setLoading(true);
    try {
      const res = await getRolesApi(params);
      setData(res || []);
    } catch {
      // errors handled by request interceptor
    } finally {
      setLoading(false);
    }
  }, []);

  const loadPermissions = useCallback(async () => {
    try {
      const res = await getPermissionListApi();
      setPermissions(res || []);
    } catch {
      // errors handled by request interceptor
    }
  }, []);

  useEffect(() => {
    loadData(query);
  }, [loadData, query]);

  useEffect(() => {
    loadPermissions();
  }, [loadPermissions]);

  const handleSearch = () => {
    const values = form.getFieldsValue();
    setQuery({ ...values });
  };

  const handleReset = () => {
    form.resetFields();
    setQuery({});
  };

  const openCreate = () => {
    setEditingRole(null);
    roleForm.resetFields();
    roleForm.setFieldsValue({ status: 1, dataScope: 1 });
    setRoleModalOpen(true);
  };

  const openEdit = (record: RoleDetailResponse) => {
    setEditingRole(record);
    roleForm.resetFields();
    roleForm.setFieldsValue({
      name: record.name,
      code: record.code,
      description: record.description || undefined,
      permissions: record.permissions || [],
      dataScope: record.dataScope,
      status: record.status,
      sortOrder: record.sortOrder ?? undefined,
    });
    setRoleModalOpen(true);
  };

  const handleSubmit = async () => {
    const values = await roleForm.validateFields();
    try {
      if (editingRole) {
        const payload: RoleUpdateRequest = {
          name: values.name,
          code: values.code,
          description: values.description,
          permissions: values.permissions,
          dataScope: values.dataScope,
          status: values.status,
          sortOrder: values.sortOrder,
        };
        await updateRoleApi(editingRole.id, payload);
        message.success('角色已更新');
      } else {
        const payload: RoleCreateRequest = {
          name: values.name,
          code: values.code,
          description: values.description,
          permissions: values.permissions,
          dataScope: values.dataScope,
          status: values.status,
          sortOrder: values.sortOrder,
        };
        await createRoleApi(payload);
        message.success('角色已创建');
      }
      setRoleModalOpen(false);
      setQuery({ ...query });
    } catch {
      // errors handled by request interceptor
    }
  };

  const handleDelete = (record: RoleDetailResponse) => {
    Modal.confirm({
      title: `确认删除角色 ${record.name}？`,
      okType: 'danger',
      okText: '删除',
      cancelText: '取消',
      async onOk() {
        try {
          await deleteRoleApi(record.id);
          message.success('角色已删除');
          setQuery({ ...query });
        } catch {
          // errors handled by request interceptor
        }
      },
    });
  };

  const columns: ColumnsType<RoleDetailResponse> = [
    { title: '角色名称', dataIndex: 'name', key: 'name' },
    { title: '角色编码', dataIndex: 'code', key: 'code' },
    {
      title: '数据范围',
      dataIndex: 'dataScope',
      key: 'dataScope',
      render: (value: number) => getDataScopeLabel(value),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (value: number) => (
        <Tag color={value === 1 ? 'green' : 'red'}>{getStatusLabel(value)}</Tag>
      ),
    },
    { title: '描述', dataIndex: 'description', key: 'description', render: (v) => v || '-' },
    {
      title: '操作',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Button type="link" icon={<EyeOutlined />} onClick={() => navigate(`/org/roles/${record.id}`)}>
            详情
          </Button>
          <Button type="link" icon={<EditOutlined />} onClick={() => openEdit(record)}>
            编辑
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
            <Input placeholder="角色名称/编码" allowClear />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select style={{ width: 160 }} allowClear placeholder="状态" options={STATUS_OPTIONS} />
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
        title="角色列表"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => setQuery({ ...query })} />
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
              新增角色
            </Button>
          </Space>
        }
      >
        <Table rowKey="id" columns={columns} dataSource={data} loading={loading} pagination={false} />
      </Card>

      <Modal
        title={editingRole ? '编辑角色' : '新增角色'}
        open={roleModalOpen}
        onCancel={() => setRoleModalOpen(false)}
        onOk={handleSubmit}
        destroyOnClose
      >
        <Form form={roleForm} layout="vertical">
          <Form.Item name="name" label="角色名称" rules={[{ required: true, message: '请输入角色名称' }]}>
            <Input placeholder="角色名称" />
          </Form.Item>
          <Form.Item name="code" label="角色编码" rules={[{ required: true, message: '请输入角色编码' }]}>
            <Input placeholder="角色编码" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="角色描述" />
          </Form.Item>
          <Form.Item name="dataScope" label="数据范围" rules={[{ required: true, message: '请选择数据范围' }]}>
            <Select options={DATA_SCOPE_OPTIONS} />
          </Form.Item>
          <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
            <Select options={STATUS_OPTIONS} />
          </Form.Item>
          <Form.Item name="sortOrder" label="排序">
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="permissions" label="权限列表">
            <Select
              mode="multiple"
              placeholder="选择权限"
              options={permissions.map((p) => ({ label: p, value: p }))}
            />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
};

export default RoleListPage;
