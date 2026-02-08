import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
    Button,
    Card,
    Form,
    InputNumber,
    message,
    Modal,
    Progress,
    Select,
    Space,
    Table,
    Tag,
    TreeSelect,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import type { OrgUnitTreeNode } from '@/types/org';
import type { PositionDetailResponse, HeadcountCreateRequest, HeadcountDetailResponse, HeadcountQueryParams, HeadcountUpdateRequest } from '@/types/hr';
import { getOrgUnitTreeApi } from '@/services/org';
import { getEnabledPositionsApi, createHeadcountApi, deleteHeadcountApi, getHeadcountsApi, updateHeadcountApi } from '@/services/hr';
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

const currentYear = new Date().getFullYear();
const YEAR_OPTIONS = Array.from({ length: 5 }, (_, i) => ({ label: `${currentYear - 2 + i}年`, value: currentYear - 2 + i }));
const QUARTER_OPTIONS = [
    { label: 'Q1', value: 1 },
    { label: 'Q2', value: 2 },
    { label: 'Q3', value: 3 },
    { label: 'Q4', value: 4 },
];

const HeadcountListPage: React.FC = () => {
    const [form] = Form.useForm<HeadcountQueryParams>();
    const [headcountForm] = Form.useForm<HeadcountCreateRequest>();
    const [data, setData] = useState<HeadcountDetailResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [query, setQuery] = useState<HeadcountQueryParams>({ pageNo: 1, pageSize: 20 });
    const [pagination, setPagination] = useState({ pageNo: 1, pageSize: 20, total: 0 });
    const [orgTree, setOrgTree] = useState<OrgUnitTreeNode[]>([]);
    const [positions, setPositions] = useState<PositionDetailResponse[]>([]);
    const [modalOpen, setModalOpen] = useState(false);
    const [editingHeadcount, setEditingHeadcount] = useState<HeadcountDetailResponse | null>(null);

    const treeSelectData = useMemo(() => buildTreeSelectData(orgTree), [orgTree]);
    const orgUnitMap = useMemo(() => {
        const map = new Map<number, string>();
        const walk = (nodes: OrgUnitTreeNode[]) => {
            nodes.forEach((node) => {
                map.set(node.id, node.name);
                if (node.children) walk(node.children);
            });
        };
        walk(orgTree);
        return map;
    }, [orgTree]);

    const loadOptions = useCallback(async () => {
        try {
            const [orgData, posData] = await Promise.all([getOrgUnitTreeApi(), getEnabledPositionsApi()]);
            setOrgTree(orgData || []);
            setPositions(posData || []);
        } catch {
            // errors handled by request interceptor
        }
    }, []);

    const loadData = useCallback(async (params: HeadcountQueryParams) => {
        setLoading(true);
        try {
            const res = await getHeadcountsApi(params);
            setData(res.records || []);
            setPagination({ pageNo: res.pageNo, pageSize: res.pageSize, total: res.total });
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
        setEditingHeadcount(null);
        headcountForm.resetFields();
        headcountForm.setFieldsValue({ status: 1, year: currentYear });
        setModalOpen(true);
    };

    const openEdit = (record: HeadcountDetailResponse) => {
        setEditingHeadcount(record);
        headcountForm.resetFields();
        headcountForm.setFieldsValue({
            orgUnitId: record.orgUnitId,
            positionId: record.positionId,
            budgetCount: record.budgetCount,
            year: record.year,
            quarter: record.quarter,
            status: record.status,
        });
        setModalOpen(true);
    };

    const handleSubmit = async () => {
        const values = await headcountForm.validateFields();
        try {
            if (editingHeadcount) {
                const payload: HeadcountUpdateRequest = {
                    budgetCount: values.budgetCount,
                    year: values.year,
                    quarter: values.quarter,
                    status: values.status,
                };
                await updateHeadcountApi(editingHeadcount.id, payload);
                message.success('编制已更新');
            } else {
                await createHeadcountApi(values);
                message.success('编制已创建');
            }
            setModalOpen(false);
            setQuery({ ...query, pageNo: 1 });
        } catch {
            // errors handled by request interceptor
        }
    };

    const handleDelete = (record: HeadcountDetailResponse) => {
        Modal.confirm({
            title: `确认删除该编制配置？`,
            content: '删除后不可恢复。',
            okType: 'danger',
            okText: '删除',
            cancelText: '取消',
            async onOk() {
                try {
                    await deleteHeadcountApi(record.id);
                    message.success('编制已删除');
                    setQuery({ ...query, pageNo: 1 });
                } catch {
                    // errors handled by request interceptor
                }
            },
        });
    };

    const columns: ColumnsType<HeadcountDetailResponse> = [
        {
            title: '组织',
            dataIndex: 'orgUnitId',
            key: 'orgUnitId',
            render: (v: number) => orgUnitMap.get(v) || v,
        },
        {
            title: '岗位',
            dataIndex: 'positionName',
            key: 'positionName',
            render: (v) => v || '-',
        },
        { title: '编制数', dataIndex: 'budgetCount', key: 'budgetCount' },
        { title: '在职数', dataIndex: 'actualCount', key: 'actualCount' },
        {
            title: '使用率',
            key: 'usage',
            render: (_, record) => {
                const percent = record.budgetCount > 0 ? Math.round((record.actualCount / record.budgetCount) * 100) : 0;
                return <Progress percent={percent} size="small" status={percent >= 100 ? 'exception' : 'active'} />;
            },
        },
        { title: '年份', dataIndex: 'year', key: 'year', render: (v) => v || '-' },
        { title: '季度', dataIndex: 'quarter', key: 'quarter', render: (v) => (v ? `Q${v}` : '-') },
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
                    <Form.Item name="orgUnitId" label="组织">
                        <TreeSelect style={{ width: 220 }} treeData={treeSelectData} allowClear placeholder="选择组织" treeDefaultExpandAll />
                    </Form.Item>
                    <Form.Item name="positionId" label="岗位">
                        <Select style={{ width: 180 }} allowClear placeholder="选择岗位" options={positions.map((p) => ({ label: p.positionName, value: p.id }))} />
                    </Form.Item>
                    <Form.Item name="year" label="年份">
                        <Select style={{ width: 100 }} allowClear placeholder="年份" options={YEAR_OPTIONS} />
                    </Form.Item>
                    <Form.Item name="quarter" label="季度">
                        <Select style={{ width: 100 }} allowClear placeholder="季度" options={QUARTER_OPTIONS} />
                    </Form.Item>
                    <Form.Item name="status" label="状态">
                        <Select style={{ width: 100 }} allowClear placeholder="状态" options={STATUS_OPTIONS} />
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
                title="编制配置"
                extra={
                    <Space>
                        <Button icon={<ReloadOutlined />} onClick={() => setQuery({ ...query })} />
                        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>新增编制</Button>
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
                title={editingHeadcount ? '编辑编制' : '新增编制'}
                open={modalOpen}
                onCancel={() => setModalOpen(false)}
                onOk={handleSubmit}
                destroyOnClose
                width={500}
            >
                <Form form={headcountForm} layout="vertical">
                    <Form.Item name="orgUnitId" label="组织" rules={[{ required: true, message: '请选择组织' }]}>
                        <TreeSelect treeData={treeSelectData} placeholder="选择组织" allowClear treeDefaultExpandAll disabled={!!editingHeadcount} />
                    </Form.Item>
                    <Form.Item name="positionId" label="岗位" rules={[{ required: true, message: '请选择岗位' }]}>
                        <Select placeholder="选择岗位" options={positions.map((p) => ({ label: p.positionName, value: p.id }))} disabled={!!editingHeadcount} />
                    </Form.Item>
                    <Form.Item name="budgetCount" label="编制数" rules={[{ required: true, message: '请输入编制数' }]}>
                        <InputNumber min={1} style={{ width: '100%' }} placeholder="编制数量" />
                    </Form.Item>
                    <Form.Item name="year" label="年份">
                        <Select options={YEAR_OPTIONS} placeholder="选择年份" allowClear />
                    </Form.Item>
                    <Form.Item name="quarter" label="季度">
                        <Select options={QUARTER_OPTIONS} placeholder="选择季度" allowClear />
                    </Form.Item>
                    <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
                        <Select options={STATUS_OPTIONS} />
                    </Form.Item>
                </Form>
            </Modal>
        </Space>
    );
};

export default HeadcountListPage;
