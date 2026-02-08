import React, { useCallback, useEffect, useState } from 'react';
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
    Upload,
    Row,
    Col,
    Statistic,
    Spin,
    Input as AntdInput,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { CheckOutlined, CloseOutlined, DeleteOutlined, PlusOutlined, ReloadOutlined, SendOutlined, UploadOutlined, DownloadOutlined, StopOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import type { EmploymentEventCreateRequest, EmploymentEventDetailResponse, EmploymentEventQueryParams, EmploymentEventStatisticsDTO, EmploymentEventType, EmploymentEventStatus } from '@/types/hr';
import {
    approveEmploymentEventApi,
    cancelEmploymentEventApi,
    createEmploymentEventApi,
    deleteEmploymentEventApi,
    downloadEmploymentEventTemplateApi,
    getEmployeesApi,
    getEmploymentEventsApi,
    getEmploymentEventsStatisticsApi,
    importEmploymentEventsApi,
    submitEmploymentEventApi,
} from '@/services/hr';

const EVENT_TYPE_OPTIONS: { label: string; value: EmploymentEventType }[] = [
    { label: '入职', value: 'entry' },
    { label: '转正', value: 'regular' },
    { label: '调岗', value: 'transfer' },
    { label: '晋升', value: 'promotion' },
    { label: '降级', value: 'demotion' },
    { label: '调薪', value: 'salary_change' },
    { label: '离职', value: 'resignation' },
];

const EVENT_STATUS_OPTIONS: { label: string; value: EmploymentEventStatus }[] = [
    { label: '草稿', value: 'draft' },
    { label: '待审批', value: 'pending' },
    { label: '已通过', value: 'approved' },
    { label: '已驳回', value: 'rejected' },
    { label: '已取消', value: 'cancelled' },
];

const EVENT_TYPE_TEXT: Record<string, string> = {
    entry: '入职',
    regular: '转正',
    transfer: '调岗',
    promotion: '晋升',
    demotion: '降级',
    salary_change: '调薪',
    resignation: '离职',
};

const EVENT_STATUS_COLOR: Record<string, string> = {
    draft: 'default',
    pending: 'processing',
    approved: 'success',
    rejected: 'error',
    cancelled: 'default',
};

const EVENT_STATUS_TEXT: Record<string, string> = {
    draft: '草稿',
    pending: '待审批',
    approved: '已通过',
    rejected: '已驳回',
    cancelled: '已取消',
};

interface EmployeeOption {
    id: number;
    realName: string;
    employeeCode: string;
}

const EmploymentEventListPage: React.FC = () => {
    const [form] = Form.useForm<EmploymentEventQueryParams>();
    const [eventForm] = Form.useForm();
    const [data, setData] = useState<EmploymentEventDetailResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [query, setQuery] = useState<EmploymentEventQueryParams>({ pageNo: 1, pageSize: 20 });
    const [pagination, setPagination] = useState({ pageNo: 1, pageSize: 20, total: 0 });
    const [employees, setEmployees] = useState<EmployeeOption[]>([]);
    const [modalOpen, setModalOpen] = useState(false);
    const [importModalOpen, setImportModalOpen] = useState(false);
    const [importing, setImporting] = useState(false);
    const [statistics, setStatistics] = useState<EmploymentEventStatisticsDTO | null>(null);
    const [statisticsLoading, setStatisticsLoading] = useState(false);

    const loadEmployees = useCallback(async () => {
        try {
            const res = await getEmployeesApi({ pageSize: 500 });
            setEmployees((res.records || []).map((e) => ({ id: e.id, realName: e.realName, employeeCode: e.employeeCode })));
        } catch {
            // errors handled by request interceptor
        }
    }, []);

    const loadData = useCallback(async (params: EmploymentEventQueryParams) => {
        setLoading(true);
        try {
            const res = await getEmploymentEventsApi(params);
            setData(res.records || []);
            setPagination({ pageNo: res.pageNo, pageSize: res.pageSize, total: res.total });
        } catch {
            // errors handled by request interceptor
        } finally {
            setLoading(false);
        }
    }, []);

    const loadStatistics = useCallback(async () => {
        setStatisticsLoading(true);
        try {
            const res = await getEmploymentEventsStatisticsApi();
            setStatistics(res);
        } catch {
            // errors handled by request interceptor
        } finally {
            setStatisticsLoading(false);
        }
    }, []);

    useEffect(() => {
        loadEmployees();
    }, [loadEmployees]);

    useEffect(() => {
        loadData(query);
    }, [loadData, query]);

    useEffect(() => {
        loadStatistics();
    }, [loadStatistics]);

    const handleSearch = () => {
        const values = form.getFieldsValue();
        setQuery({ pageNo: 1, pageSize: query.pageSize || 20, ...values });
    };

    const handleReset = () => {
        form.resetFields();
        setQuery({ pageNo: 1, pageSize: 20 });
    };

    const openCreate = () => {
        eventForm.resetFields();
        eventForm.setFieldsValue({ eventDate: dayjs() });
        setModalOpen(true);
    };

    const handleCreate = async () => {
        const values = await eventForm.validateFields();
        const payload: EmploymentEventCreateRequest = {
            ...values,
            eventDate: values.eventDate?.format('YYYY-MM-DD'),
        };
        try {
            await createEmploymentEventApi(payload);
            message.success('任职事件已创建');
            setModalOpen(false);
            setQuery({ ...query, pageNo: 1 });
        } catch {
            // errors handled by request interceptor
        }
    };

    const handleSubmit = async (record: EmploymentEventDetailResponse) => {
        try {
            await submitEmploymentEventApi(record.id);
            message.success('已提交审批');
            setQuery({ ...query });
        } catch {
            // errors handled by request interceptor
        }
    };

    const handleApprove = async (record: EmploymentEventDetailResponse) => {
        Modal.confirm({
            title: '确认审批通过？',
            content: `${EVENT_TYPE_TEXT[record.eventType] || record.eventType} 事件将被批准。`,
            okText: '通过',
            cancelText: '取消',
            async onOk() {
                try {
                    await approveEmploymentEventApi(record.id, { approved: true });
                    message.success('已审批通过');
                    setQuery({ ...query });
                } catch {
                    // errors handled by request interceptor
                }
            },
        });
    };

    const handleReject = async (record: EmploymentEventDetailResponse) => {
        let rejectReason = '';
        Modal.confirm({
            title: '确认驳回？',
            content: (
                <AntdInput.TextArea
                    rows={3}
                    placeholder="请输入驳回原因"
                    onChange={(event) => {
                        rejectReason = event.target.value;
                    }}
                />
            ),
            okText: '确认驳回',
            cancelText: '取消',
            async onOk() {
                if (!rejectReason.trim()) {
                    message.error('请填写驳回原因');
                    return Promise.reject(new Error('reject reason required'));
                }
                try {
                    await approveEmploymentEventApi(record.id, { approved: false, rejectReason: rejectReason.trim() });
                    message.success('已驳回');
                    setQuery({ ...query });
                } catch {
                    // errors handled by request interceptor
                }
            },
        });
    };

    const handleCancel = async (record: EmploymentEventDetailResponse) => {
        Modal.confirm({
            title: '确认取消？',
            content: '此操作不可恢复。',
            okType: 'danger',
            okText: '取消事件',
            cancelText: '返回',
            async onOk() {
                try {
                    await cancelEmploymentEventApi(record.id);
                    message.success('事件已取消');
                    setQuery({ ...query });
                } catch {
                    // errors handled by request interceptor
                }
            },
        });
    };

    const handleDelete = async (record: EmploymentEventDetailResponse) => {
        Modal.confirm({
            title: '确认删除？',
            content: '删除后不可恢复。',
            okType: 'danger',
            okText: '删除',
            cancelText: '取消',
            async onOk() {
                try {
                    await deleteEmploymentEventApi(record.id);
                    message.success('事件已删除');
                    setQuery({ ...query, pageNo: 1 });
                } catch {
                    // errors handled by request interceptor
                }
            },
        });
    };

    const openImportModal = () => {
        setImportModalOpen(true);
    };

    const handleDownloadTemplate = async () => {
        try {
            const blob = await downloadEmploymentEventTemplateApi();
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = '任职事件导入模版.xlsx';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
            message.success('模版下载成功');
        } catch {
            message.error('模版下载失败');
        }
    };

    const handleImport = async (file: File) => {
        setImporting(true);
        try {
            await importEmploymentEventsApi(file);
            message.success('导入成功');
            setImportModalOpen(false);
            setQuery({ ...query, pageNo: 1 });
            loadStatistics();
        } catch {
            // errors handled by request interceptor
        } finally {
            setImporting(false);
        }
    };

    const handleFilterByType = (eventType: EmploymentEventType) => {
        form.setFieldsValue({ eventType });
        handleSearch();
    };

    const handleFilterByStatus = (status: EmploymentEventStatus) => {
        form.setFieldsValue({ status });
        handleSearch();
    };

    const columns: ColumnsType<EmploymentEventDetailResponse> = [
        { title: '员工', dataIndex: 'employeeName', key: 'employeeName', render: (v) => v || '-' },
        {
            title: '事件类型',
            dataIndex: 'eventType',
            key: 'eventType',
            render: (v: string) => EVENT_TYPE_TEXT[v] || v,
        },
        { title: '事件日期', dataIndex: 'eventDate', key: 'eventDate' },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            render: (v: string) => <Tag color={EVENT_STATUS_COLOR[v] || 'default'}>{EVENT_STATUS_TEXT[v] || v}</Tag>,
        },
        { title: '原因', dataIndex: 'reason', key: 'reason', render: (v) => v || '-', ellipsis: true },
        { title: '审批人', dataIndex: 'approverName', key: 'approverName', render: (v) => v || '-' },
        { title: '审批时间', dataIndex: 'approvedAt', key: 'approvedAt', render: (v) => v || '-' },
        {
            title: '操作',
            key: 'actions',
            width: 280,
            render: (_, record) => (
                <Space>
                    {record.status === 'draft' && (
                        <Button type="link" icon={<SendOutlined />} onClick={() => handleSubmit(record)}>提交</Button>
                    )}
                    {record.status === 'pending' && (
                        <>
                            <Button type="link" icon={<CheckOutlined />} onClick={() => handleApprove(record)}>通过</Button>
                            <Button type="link" icon={<CloseOutlined />} onClick={() => handleReject(record)}>驳回</Button>
                            <Button type="link" danger icon={<StopOutlined />} onClick={() => handleCancel(record)}>取消</Button>
                        </>
                    )}
                    {(record.status === 'draft' || record.status === 'pending') && (
                        <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>删除</Button>
                    )}
                </Space>
            ),
        },
    ];

    return (
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Spin spinning={statisticsLoading}>
                <Card title="任职统计" size="small">
                    <Row gutter={16}>
                        <Col span={12}>
                            <Card title="按类型" size="small">
                                <Row gutter={[8, 8]}>
                                    {statistics?.byType?.map((item) => (
                                        <Col span={8} key={item.key}>
                                            <div onClick={() => handleFilterByType(item.key as EmploymentEventType)} style={{ cursor: 'pointer' }}>
                                                <Statistic
                                                    title={EVENT_TYPE_TEXT[item.key] || item.key}
                                                    value={item.count}
                                                    valueStyle={{ fontSize: 16 }}
                                                />
                                            </div>
                                        </Col>
                                    ))}
                                </Row>
                            </Card>
                        </Col>
                        <Col span={12}>
                            <Card title="按状态" size="small">
                                <Row gutter={[8, 8]}>
                                    {statistics?.byStatus?.map((item) => (
                                        <Col span={8} key={item.key}>
                                            <div onClick={() => handleFilterByStatus(item.key as EmploymentEventStatus)} style={{ cursor: 'pointer' }}>
                                                <Statistic
                                                    title={EVENT_STATUS_TEXT[item.key] || item.key}
                                                    value={item.count}
                                                    valueStyle={{ fontSize: 16 }}
                                                />
                                            </div>
                                        </Col>
                                    ))}
                                </Row>
                            </Card>
                        </Col>
                    </Row>
                </Card>
            </Spin>

            <Card>
                <Form form={form} layout="inline">
                    <Form.Item name="keyword" label="关键词">
                        <Input placeholder="员工姓名" allowClear />
                    </Form.Item>
                    <Form.Item name="eventType" label="事件类型">
                        <Select style={{ width: 120 }} allowClear placeholder="类型" options={EVENT_TYPE_OPTIONS} />
                    </Form.Item>
                    <Form.Item name="status" label="状态">
                        <Select style={{ width: 120 }} allowClear placeholder="状态" options={EVENT_STATUS_OPTIONS} />
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
                title="任职事件"
                extra={
                    <Space>
                        <Button icon={<ReloadOutlined />} onClick={() => { setQuery({ ...query }); loadStatistics(); }} />
                        <Button icon={<UploadOutlined />} onClick={openImportModal}>导入</Button>
                        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>新增事件</Button>
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
                title="新增任职事件"
                open={modalOpen}
                onCancel={() => setModalOpen(false)}
                onOk={handleCreate}
                destroyOnClose
                width={600}
            >
                <Form form={eventForm} layout="vertical">
                    <Form.Item name="employeeId" label="员工" rules={[{ required: true, message: '请选择员工' }]}>
                        <Select
                            showSearch
                            placeholder="选择员工"
                            optionFilterProp="label"
                            options={employees.map((e) => ({ label: `${e.realName} (${e.employeeCode})`, value: e.id }))}
                        />
                    </Form.Item>
                    <Form.Item name="eventType" label="事件类型" rules={[{ required: true, message: '请选择事件类型' }]}>
                        <Select options={EVENT_TYPE_OPTIONS} placeholder="选择事件类型" />
                    </Form.Item>
                    <Form.Item name="eventDate" label="事件日期" rules={[{ required: true, message: '请选择事件日期' }]}>
                        <DatePicker style={{ width: '100%' }} />
                    </Form.Item>
                    <Form.Item name="reason" label="原因">
                        <Input.TextArea rows={3} placeholder="请输入原因或备注" />
                    </Form.Item>
                    <Form.Item name="remark" label="备注">
                        <Input.TextArea rows={2} placeholder="其他备注信息" />
                    </Form.Item>
                </Form>
            </Modal>

            <Modal
                title="批量导入任职事件"
                open={importModalOpen}
                onCancel={() => setImportModalOpen(false)}
                footer={null}
                destroyOnClose
            >
                <Space direction="vertical" size="large" style={{ width: '100%' }}>
                    <Card size="small">
                        <Button icon={<DownloadOutlined />} onClick={handleDownloadTemplate}>下载导入模版</Button>
                    </Card>
                    <Upload.Dragger
                        name="file"
                        accept=".xlsx,.xls"
                        maxCount={1}
                        beforeUpload={(file) => {
                            handleImport(file);
                            return false;
                        }}
                        showUploadList={false}
                    >
                        <p className="ant-upload-drag-icon">
                            <UploadOutlined />
                        </p>
                        <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
                        <p className="ant-upload-hint">支持 .xlsx 或 .xls 格式</p>
                    </Upload.Dragger>
                    {importing && <Spin tip="正在导入..." />}
                </Space>
            </Modal>
        </Space>
    );
};

export default EmploymentEventListPage;
