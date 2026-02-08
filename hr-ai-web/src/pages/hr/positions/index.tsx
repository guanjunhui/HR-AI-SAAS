import React, { useCallback, useEffect, useState } from 'react';
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
import { DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import type { OrgUnitTreeNode } from '@/types/org';
import type { PositionCreateRequest, PositionDetailResponse, PositionQueryParams, PositionUpdateRequest } from '@/types/hr';
import { getOrgUnitTreeApi } from '@/services/org';
import { createPositionApi, deletePositionApi, getPositionsApi, updatePositionApi } from '@/services/hr';
import { STATUS_OPTIONS, getStatusLabel } from '@/utils/dicts';

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

const PositionListPage: React.FC = () => {
    const [form] = Form.useForm<PositionQueryParams>();
    const [positionForm] = Form.useForm<PositionCreateRequest>();
    const [data, setData] = useState<PositionDetailResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [query, setQuery] = useState<PositionQueryParams>({ pageNo: 1, pageSize: 20 });
    const [pagination, setPagination] = useState({ pageNo: 1, pageSize: 20, total: 0 });
    const [orgTree, setOrgTree] = useState<OrgUnitTreeNode[]>([]);
    const [modalOpen, setModalOpen] = useState(false);
    const [editingPosition, setEditingPosition] = useState<PositionDetailResponse | null>(null);

    const treeSelectData = React.useMemo(() => buildTreeSelectData(orgTree), [orgTree]);

    const loadOptions = useCallback(async () => {
        try {
            const orgData = await getOrgUnitTreeApi();
            setOrgTree(orgData || []);
        } catch {
            // errors handled by request interceptor
        }
    }, []);

    const loadData = useCallback(async (params: PositionQueryParams) => {
        setLoading(true);
        try {
            const res = await getPositionsApi(params);
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
        setQuery({ pageNo: 1, pageSize: query.pageSize || 20, ...values });
    };

    const handleReset = () => {
        form.resetFields();
        setQuery({ pageNo: 1, pageSize: 20 });
    };

    const openCreate = () => {
        setEditingPosition(null);
        positionForm.resetFields();
        positionForm.setFieldsValue({ status: 1 });
        setModalOpen(true);
    };

    const openEdit = (record: PositionDetailResponse) => {
        setEditingPosition(record);
        positionForm.resetFields();
        positionForm.setFieldsValue({
            positionCode: record.positionCode,
            positionName: record.positionName,
            positionLevel: record.positionLevel,
            jobFamily: record.jobFamily,
            orgUnitId: record.orgUnitId,
            description: record.description,
            requirements: record.requirements,
            status: record.status,
        });
        setModalOpen(true);
    };

    const handleSubmit = async () => {
        const values = await positionForm.validateFields();
        try {
            if (editingPosition) {
                const payload: PositionUpdateRequest = { ...values };
                await updatePositionApi(editingPosition.id, payload);
                message.success('岗位已更新');
            } else {
                await createPositionApi(values);
                message.success('岗位已创建');
            }
            setModalOpen(false);
            setQuery({ ...query, pageNo: 1 });
        } catch {
            // errors handled by request interceptor
        }
    };

    const handleDelete = (record: PositionDetailResponse) => {
        Modal.confirm({
            title: `确认删除岗位 ${record.positionName}？`,
            content: '删除后不可恢复。',
            okType: 'danger',
            okText: '删除',
            cancelText: '取消',
            async onOk() {
                try {
                    await deletePositionApi(record.id);
                    message.success('岗位已删除');
                    setQuery({ ...query, pageNo: 1 });
                } catch {
                    // errors handled by request interceptor
                }
            },
        });
    };

    const columns: ColumnsType<PositionDetailResponse> = [
        { title: '岗位编码', dataIndex: 'positionCode', key: 'positionCode' },
        { title: '岗位名称', dataIndex: 'positionName', key: 'positionName' },
        { title: '岗位级别', dataIndex: 'positionLevel', key: 'positionLevel', render: (v) => v || '-' },
        { title: '职位序列', dataIndex: 'jobFamily', key: 'jobFamily', render: (v) => v || '-' },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            render: (value: number) => <Tag color={value === 1 ? 'green' : 'red'}>{getStatusLabel(value)}</Tag>,
        },
        {
            title: '操作',
            key: 'actions',
            render: (_, record) => (
                <Space>
                    <Button type="link" icon={<EditOutlined />} onClick={() => openEdit(record)}>编辑</Button>
                    <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>删除</Button>
                </Space>
            ),
        },
    ];

    return (
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Card>
                <Form form={form} layout="inline">
                    <Form.Item name="keyword" label="关键词">
                        <Input placeholder="岗位编码/名称" allowClear />
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
                    <Form.Item name="status" label="状态">
                        <Select style={{ width: 120 }} allowClear placeholder="状态" options={STATUS_OPTIONS} />
                    </Form.Item>
                    <Form.Item>
                        <Space>
                            <Button type="primary" onClick={handleSearch}>查询</Button>
                            <Button onClick={handleReset}>重置</Button>
                        </Space>
                    </Form.Item>
                </Form>
            </Card>

            <Card
                title="岗位列表"
                extra={
                    <Space>
                        <Button icon={<ReloadOutlined />} onClick={() => setQuery({ ...query })} />
                        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>新增岗位</Button>
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
                title={editingPosition ? '编辑岗位' : '新增岗位'}
                open={modalOpen}
                onCancel={() => setModalOpen(false)}
                onOk={handleSubmit}
                destroyOnClose
                width={600}
            >
                <Form form={positionForm} layout="vertical">
                    <Form.Item name="positionCode" label="岗位编码" rules={[{ required: true, message: '请输入岗位编码' }]}>
                        <Input placeholder="岗位编码" />
                    </Form.Item>
                    <Form.Item name="positionName" label="岗位名称" rules={[{ required: true, message: '请输入岗位名称' }]}>
                        <Input placeholder="岗位名称" />
                    </Form.Item>
                    <Form.Item name="positionLevel" label="岗位级别">
                        <Input placeholder="如：P6, M2 等" />
                    </Form.Item>
                    <Form.Item name="jobFamily" label="职位序列">
                        <Input placeholder="如：技术、产品、运营" />
                    </Form.Item>
                    <Form.Item name="orgUnitId" label="所属组织">
                        <TreeSelect treeData={treeSelectData} placeholder="选择组织" allowClear treeDefaultExpandAll />
                    </Form.Item>
                    <Form.Item name="description" label="岗位描述">
                        <Input.TextArea rows={3} placeholder="岗位职责描述" />
                    </Form.Item>
                    <Form.Item name="requirements" label="任职要求">
                        <Input.TextArea rows={3} placeholder="任职要求" />
                    </Form.Item>
                    <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
                        <Select options={STATUS_OPTIONS} />
                    </Form.Item>
                </Form>
            </Modal>
        </Space>
    );
};

export default PositionListPage;
