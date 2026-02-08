import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
    Button,
    Card,
    DatePicker,
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
import { DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import type { OrgUnitTreeNode } from '@/types/org';
import type { PositionDetailResponse, EmployeeCreateRequest, EmployeeDetailResponse, EmployeeQueryParams, EmployeeUpdateRequest } from '@/types/hr';
import { getOrgUnitTreeApi } from '@/services/org';
import { getEnabledPositionsApi, createEmployeeApi, deleteEmployeeApi, getEmployeesApi, updateEmployeeApi } from '@/services/hr';

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

const EMPLOYEE_STATUS_OPTIONS = [
    { label: '试用期', value: 'trial' },
    { label: '正式', value: 'regular' },
    { label: '离职', value: 'resigned' },
];

const GENDER_OPTIONS = [
    { label: '男', value: 'male' },
    { label: '女', value: 'female' },
];

const STATUS_COLOR_MAP: Record<string, string> = {
    trial: 'orange',
    regular: 'green',
    resigned: 'default',
};

const STATUS_TEXT_MAP: Record<string, string> = {
    trial: '试用期',
    regular: '正式',
    resigned: '离职',
};

const EmployeeListPage: React.FC = () => {
    const [form] = Form.useForm<EmployeeQueryParams>();
    const [employeeForm] = Form.useForm();
    const [data, setData] = useState<EmployeeDetailResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [query, setQuery] = useState<EmployeeQueryParams>({ pageNo: 1, pageSize: 20 });
    const [pagination, setPagination] = useState({ pageNo: 1, pageSize: 20, total: 0 });
    const [orgTree, setOrgTree] = useState<OrgUnitTreeNode[]>([]);
    const [positions, setPositions] = useState<PositionDetailResponse[]>([]);
    const [modalOpen, setModalOpen] = useState(false);
    const [editingEmployee, setEditingEmployee] = useState<EmployeeDetailResponse | null>(null);
    const navigate = useNavigate();

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

    const loadData = useCallback(async (params: EmployeeQueryParams) => {
        setLoading(true);
        try {
            const res = await getEmployeesApi(params);
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
        setEditingEmployee(null);
        employeeForm.resetFields();
        employeeForm.setFieldsValue({ employeeStatus: 'trial' });
        setModalOpen(true);
    };

    const openEdit = (record: EmployeeDetailResponse) => {
        setEditingEmployee(record);
        employeeForm.resetFields();
        employeeForm.setFieldsValue({
            employeeCode: record.employeeCode,
            realName: record.realName,
            gender: record.gender,
            phone: record.phone,
            email: record.email,
            idCard: record.idCard,
            orgUnitId: record.orgUnitId,
            positionId: record.positionId,
            directManagerId: record.directManagerId,
            entryDate: record.entryDate ? dayjs(record.entryDate) : undefined,
            probationEndDate: record.probationEndDate ? dayjs(record.probationEndDate) : undefined,
            employeeStatus: record.employeeStatus,
            workLocation: record.workLocation,
        });
        setModalOpen(true);
    };

    const handleSubmit = async () => {
        const values = await employeeForm.validateFields();
        const payload = {
            ...values,
            entryDate: values.entryDate?.format('YYYY-MM-DD'),
            probationEndDate: values.probationEndDate?.format('YYYY-MM-DD'),
        };
        try {
            if (editingEmployee) {
                await updateEmployeeApi(editingEmployee.id, payload as EmployeeUpdateRequest);
                message.success('员工已更新');
            } else {
                await createEmployeeApi(payload as EmployeeCreateRequest);
                message.success('员工已创建');
            }
            setModalOpen(false);
            setQuery({ ...query, pageNo: 1 });
        } catch {
            // errors handled by request interceptor
        }
    };

    const handleDelete = (record: EmployeeDetailResponse) => {
        Modal.confirm({
            title: `确认删除员工 ${record.realName}？`,
            content: '删除后不可恢复。',
            okType: 'danger',
            okText: '删除',
            cancelText: '取消',
            async onOk() {
                try {
                    await deleteEmployeeApi(record.id);
                    message.success('员工已删除');
                    setQuery({ ...query, pageNo: 1 });
                } catch {
                    // errors handled by request interceptor
                }
            },
        });
    };

    const columns: ColumnsType<EmployeeDetailResponse> = [
        { title: '工号', dataIndex: 'employeeCode', key: 'employeeCode' },
        { title: '姓名', dataIndex: 'realName', key: 'realName' },
        {
            title: '组织',
            dataIndex: 'orgUnitId',
            key: 'orgUnitId',
            render: (v: number) => (v ? orgUnitMap.get(v) || v : '-'),
        },
        {
            title: '岗位',
            dataIndex: 'positionName',
            key: 'positionName',
            render: (v) => v || '-',
        },
        { title: '入职日期', dataIndex: 'entryDate', key: 'entryDate', render: (v) => v || '-' },
        {
            title: '状态',
            dataIndex: 'employeeStatus',
            key: 'employeeStatus',
            render: (v: string) => <Tag color={STATUS_COLOR_MAP[v] || 'default'}>{STATUS_TEXT_MAP[v] || v}</Tag>,
        },
        { title: '手机', dataIndex: 'phone', key: 'phone', render: (v) => v || '-' },
        {
            title: '操作',
            key: 'actions',
            render: (_, record) => (
                <Space>
                    <Button type="link" icon={<UserOutlined />} onClick={() => navigate(`/hr/employees/${record.id}`)}>详情</Button>
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
                        <Input placeholder="工号/姓名/手机" allowClear />
                    </Form.Item>
                    <Form.Item name="orgUnitId" label="组织">
                        <TreeSelect style={{ width: 220 }} treeData={treeSelectData} allowClear placeholder="选择组织" treeDefaultExpandAll />
                    </Form.Item>
                    <Form.Item name="positionId" label="岗位">
                        <Select style={{ width: 150 }} allowClear placeholder="选择岗位" options={positions.map((p) => ({ label: p.positionName, value: p.id }))} />
                    </Form.Item>
                    <Form.Item name="employeeStatus" label="状态">
                        <Select style={{ width: 120 }} allowClear placeholder="状态" options={EMPLOYEE_STATUS_OPTIONS} />
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
                title="员工花名册"
                extra={
                    <Space>
                        <Button icon={<ReloadOutlined />} onClick={() => setQuery({ ...query })} />
                        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>新增员工</Button>
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
                title={editingEmployee ? '编辑员工' : '新增员工'}
                open={modalOpen}
                onCancel={() => setModalOpen(false)}
                onOk={handleSubmit}
                destroyOnClose
                width={700}
            >
                <Form form={employeeForm} layout="vertical">
                    <Space size="large" style={{ display: 'flex', flexWrap: 'wrap' }}>
                        <Form.Item name="employeeCode" label="工号" rules={[{ required: true, message: '请输入工号' }]} style={{ width: 200 }}>
                            <Input placeholder="工号" />
                        </Form.Item>
                        <Form.Item name="realName" label="姓名" rules={[{ required: true, message: '请输入姓名' }]} style={{ width: 200 }}>
                            <Input placeholder="姓名" />
                        </Form.Item>
                        <Form.Item name="gender" label="性别" style={{ width: 120 }}>
                            <Select options={GENDER_OPTIONS} placeholder="性别" allowClear />
                        </Form.Item>
                    </Space>
                    <Space size="large" style={{ display: 'flex', flexWrap: 'wrap' }}>
                        <Form.Item name="phone" label="手机" style={{ width: 200 }}>
                            <Input placeholder="手机号" />
                        </Form.Item>
                        <Form.Item name="email" label="邮箱" style={{ width: 250 }}>
                            <Input placeholder="邮箱" />
                        </Form.Item>
                        <Form.Item name="idCard" label="身份证" style={{ width: 200 }}>
                            <Input placeholder="身份证号" />
                        </Form.Item>
                    </Space>
                    <Space size="large" style={{ display: 'flex', flexWrap: 'wrap' }}>
                        <Form.Item name="orgUnitId" label="组织" style={{ width: 250 }}>
                            <TreeSelect treeData={treeSelectData} placeholder="选择组织" allowClear treeDefaultExpandAll />
                        </Form.Item>
                        <Form.Item name="positionId" label="岗位" style={{ width: 200 }}>
                            <Select placeholder="选择岗位" options={positions.map((p) => ({ label: p.positionName, value: p.id }))} allowClear />
                        </Form.Item>
                    </Space>
                    <Space size="large" style={{ display: 'flex', flexWrap: 'wrap' }}>
                        <Form.Item name="entryDate" label="入职日期" style={{ width: 200 }}>
                            <DatePicker style={{ width: '100%' }} />
                        </Form.Item>
                        <Form.Item name="probationEndDate" label="试用期结束" style={{ width: 200 }}>
                            <DatePicker style={{ width: '100%' }} />
                        </Form.Item>
                        <Form.Item name="employeeStatus" label="状态" rules={[{ required: true, message: '请选择状态' }]} style={{ width: 150 }}>
                            <Select options={EMPLOYEE_STATUS_OPTIONS} />
                        </Form.Item>
                    </Space>
                    <Form.Item name="workLocation" label="工作地点">
                        <Input placeholder="工作地点" />
                    </Form.Item>
                </Form>
            </Modal>
        </Space>
    );
};

export default EmployeeListPage;
