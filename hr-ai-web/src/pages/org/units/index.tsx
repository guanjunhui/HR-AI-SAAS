import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Button,
  Card,
  Col,
  Descriptions,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Row,
  Select,
  Space,
  Spin,
  Tree,
  TreeSelect,
  Typography,
} from 'antd';
import type { DataNode } from 'antd/es/tree';
import {
  DeleteOutlined,
  EditOutlined,
  EyeOutlined,
  PlusOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import type { OrgUnitCreateRequest, OrgUnitDetailResponse, OrgUnitTreeNode, OrgUnitUpdateRequest } from '@/types/org';
import { createOrgUnitApi, deleteOrgUnitApi, getOrgUnitApi, getOrgUnitTreeApi, updateOrgUnitApi } from '@/services/org';
import { STATUS_OPTIONS, getStatusLabel } from '@/utils/dicts';

interface OrgUnitFormValues {
  parentId?: number | null;
  name: string;
  code: string;
  type: string;
  leaderId?: number | null;
  sortOrder?: number | null;
  status: number;
}

interface TreeSelectNode {
  title: string;
  value: number;
  key: number;
  children?: TreeSelectNode[];
}

const { Text } = Typography;

const buildTreeNodes = (nodes: OrgUnitTreeNode[]): DataNode[] =>
  nodes.map((node) => ({
    title: `${node.name} (${node.code})`,
    key: String(node.id),
    children: node.children ? buildTreeNodes(node.children) : undefined,
  }));

const buildTreeSelectData = (nodes: OrgUnitTreeNode[]): TreeSelectNode[] =>
  nodes.map((node) => ({
    title: `${node.name} (${node.code})`,
    value: node.id,
    key: node.id,
    children: node.children ? buildTreeSelectData(node.children) : undefined,
  }));

const OrgUnitListPage: React.FC = () => {
  const [treeData, setTreeData] = useState<OrgUnitTreeNode[]>([]);
  const [loadingTree, setLoadingTree] = useState(false);
  const [selectedKey, setSelectedKey] = useState<string | null>(null);
  const [detail, setDetail] = useState<OrgUnitDetailResponse | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [formMode, setFormMode] = useState<'create' | 'edit'>('create');
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm<OrgUnitFormValues>();
  const navigate = useNavigate();

  const treeNodes = useMemo(() => buildTreeNodes(treeData), [treeData]);
  const treeSelectData = useMemo(() => buildTreeSelectData(treeData), [treeData]);

  const loadTree = useCallback(async () => {
    setLoadingTree(true);
    try {
      const data = await getOrgUnitTreeApi();
      setTreeData(data || []);
    } catch {
      // errors handled by request interceptor
    } finally {
      setLoadingTree(false);
    }
  }, []);

  const loadDetail = useCallback(async (id: number) => {
    try {
      const res = await getOrgUnitApi(id);
      setDetail(res);
    } catch {
      setDetail(null);
    }
  }, []);

  useEffect(() => {
    loadTree();
  }, [loadTree]);

  const handleSelect = (keys: React.Key[]) => {
    if (!keys.length) {
      setSelectedKey(null);
      setDetail(null);
      return;
    }
    const key = String(keys[0]);
    setSelectedKey(key);
    const id = Number(key);
    if (!Number.isNaN(id)) {
      loadDetail(id);
    }
  };

  const openCreate = (parentId?: number | null) => {
    setFormMode('create');
    form.resetFields();
    form.setFieldsValue({
      parentId: parentId ?? null,
      status: 1,
    });
    setFormOpen(true);
  };

  const openEdit = () => {
    if (!detail) {
      message.warning('请先选择组织节点');
      return;
    }
    setFormMode('edit');
    form.resetFields();
    form.setFieldsValue({
      parentId: detail.parentId ?? null,
      name: detail.name,
      code: detail.code,
      type: detail.type,
      leaderId: detail.leaderId ?? null,
      sortOrder: detail.sortOrder ?? null,
      status: detail.status,
    });
    setFormOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      if (formMode === 'create') {
        const payload: OrgUnitCreateRequest = {
          parentId: values.parentId ?? null,
          name: values.name,
          code: values.code,
          type: values.type,
          leaderId: values.leaderId ?? null,
          sortOrder: values.sortOrder ?? null,
          status: values.status,
        };
        const newId = await createOrgUnitApi(payload);
        message.success('组织已创建');
        await loadTree();
        if (newId) {
          setSelectedKey(String(newId));
          await loadDetail(newId);
        }
      } else if (detail) {
        const payload: OrgUnitUpdateRequest = {
          parentId: values.parentId ?? null,
          name: values.name,
          code: values.code,
          type: values.type,
          leaderId: values.leaderId ?? null,
          sortOrder: values.sortOrder ?? null,
          status: values.status,
        };
        await updateOrgUnitApi(detail.id, payload);
        message.success('组织已更新');
        await loadTree();
        await loadDetail(detail.id);
      }
      setFormOpen(false);
    } catch {
      // errors handled by request interceptor
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = () => {
    if (!detail) {
      message.warning('请先选择组织节点');
      return;
    }
    Modal.confirm({
      title: '确认删除该组织？',
      content: '删除后不可恢复，请确认。',
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      async onOk() {
        try {
          await deleteOrgUnitApi(detail.id);
          message.success('组织已删除');
          setSelectedKey(null);
          setDetail(null);
          await loadTree();
        } catch {
          // errors handled by request interceptor
        }
      },
    });
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Row gutter={16}>
        <Col xs={24} lg={10}>
          <Card
            title="组织树"
            extra={
              <Space>
                <Button icon={<ReloadOutlined />} onClick={loadTree} />
                <Button type="primary" icon={<PlusOutlined />} onClick={() => openCreate(null)}>
                  新增根组织
                </Button>
              </Space>
            }
          >
            <Spin spinning={loadingTree}>
              <Tree
                treeData={treeNodes}
                onSelect={handleSelect}
                selectedKeys={selectedKey ? [selectedKey] : []}
              />
            </Spin>
            {!treeNodes.length && !loadingTree && (
              <Text type="secondary">暂无组织数据</Text>
            )}
          </Card>
        </Col>
        <Col xs={24} lg={14}>
          <Card
            title="组织详情"
            extra={
              <Space>
                <Button icon={<PlusOutlined />} onClick={() => openCreate(detail?.id ?? null)}>
                  新增子组织
                </Button>
                <Button icon={<EditOutlined />} onClick={openEdit}>
                  编辑
                </Button>
                <Button icon={<DeleteOutlined />} danger onClick={handleDelete}>
                  删除
                </Button>
                <Button icon={<EyeOutlined />} onClick={() => detail && navigate(`/org/units/${detail.id}`)}>
                  查看详情
                </Button>
              </Space>
            }
          >
            {detail ? (
              <Descriptions column={2} bordered size="small">
                <Descriptions.Item label="组织名称">{detail.name}</Descriptions.Item>
                <Descriptions.Item label="组织编码">{detail.code}</Descriptions.Item>
                <Descriptions.Item label="组织类型">{detail.type}</Descriptions.Item>
                <Descriptions.Item label="负责人ID">{detail.leaderId ?? '-'}</Descriptions.Item>
                <Descriptions.Item label="状态">{getStatusLabel(detail.status)}</Descriptions.Item>
                <Descriptions.Item label="排序">{detail.sortOrder ?? '-'}</Descriptions.Item>
                <Descriptions.Item label="层级">{detail.level ?? '-'}</Descriptions.Item>
                <Descriptions.Item label="路径">{detail.path ?? '-'}</Descriptions.Item>
                <Descriptions.Item label="创建时间">{detail.createdAt ?? '-'}</Descriptions.Item>
                <Descriptions.Item label="更新时间">{detail.updatedAt ?? '-'}</Descriptions.Item>
              </Descriptions>
            ) : (
              <Text type="secondary">请选择组织节点查看详情</Text>
            )}
          </Card>
        </Col>
      </Row>

      <Modal
        title={formMode === 'create' ? '新增组织' : '编辑组织'}
        open={formOpen}
        onCancel={() => setFormOpen(false)}
        onOk={handleSubmit}
        confirmLoading={submitting}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="parentId" label="上级组织">
            <TreeSelect
              treeData={treeSelectData}
              placeholder="请选择上级组织"
              allowClear
              treeDefaultExpandAll
            />
          </Form.Item>
          <Form.Item name="name" label="组织名称" rules={[{ required: true, message: '请输入组织名称' }]}>
            <Input placeholder="请输入组织名称" />
          </Form.Item>
          <Form.Item name="code" label="组织编码" rules={[{ required: true, message: '请输入组织编码' }]}>
            <Input placeholder="请输入组织编码" />
          </Form.Item>
          <Form.Item name="type" label="组织类型" rules={[{ required: true, message: '请输入组织类型' }]}>
            <Input placeholder="如：部门/中心/事业部" />
          </Form.Item>
          <Form.Item name="leaderId" label="负责人ID">
            <InputNumber style={{ width: '100%' }} placeholder="负责人用户ID" />
          </Form.Item>
          <Form.Item name="sortOrder" label="排序">
            <InputNumber style={{ width: '100%' }} placeholder="数值越小越靠前" />
          </Form.Item>
          <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
            <Select options={STATUS_OPTIONS} placeholder="请选择状态" />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
};

export default OrgUnitListPage;
