import React, { useCallback, useEffect, useState } from 'react';
import {
  Button,
  Card,
  DatePicker,
  Drawer,
  Form,
  Input,
  InputNumber,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { EyeOutlined, ReloadOutlined } from '@ant-design/icons';
import dayjs, { type Dayjs } from 'dayjs';
import type { AuditLogDetailResponse, AuditLogQueryParams } from '@/types/audit';
import { getAuditLogsApi } from '@/services/audit';

const { RangePicker } = DatePicker;
const { Text, Paragraph } = Typography;

interface AuditQueryForm {
  userId?: number;
  action?: string;
  resource?: string;
  keyword?: string;
  range?: [Dayjs, Dayjs];
}

const formatDateTime = (value?: string | null) => {
  if (!value) return '-';
  return dayjs(value).format('YYYY-MM-DD HH:mm:ss');
};

const renderResultTag = (value?: string | null) => {
  if (!value) return '-';
  const upper = value.toUpperCase();
  const color = upper.includes('SUCCESS') || upper.includes('OK') ? 'green' : upper.includes('FAIL') ? 'red' : 'blue';
  return <Tag color={color}>{value}</Tag>;
};

const AuditLogListPage: React.FC = () => {
  const [form] = Form.useForm<AuditQueryForm>();
  const [data, setData] = useState<AuditLogDetailResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [query, setQuery] = useState<AuditLogQueryParams>({ pageNo: 1, pageSize: 20 });
  const [pagination, setPagination] = useState({ pageNo: 1, pageSize: 20, total: 0 });
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [current, setCurrent] = useState<AuditLogDetailResponse | null>(null);

  const loadData = useCallback(async (params: AuditLogQueryParams) => {
    setLoading(true);
    try {
      const res = await getAuditLogsApi(params);
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
    loadData(query);
  }, [loadData, query]);

  const handleSearch = () => {
    const values = form.getFieldsValue();
    const range = values.range || [];
    const startTime = range[0] ? range[0].format('YYYY-MM-DDTHH:mm:ss') : undefined;
    const endTime = range[1] ? range[1].format('YYYY-MM-DDTHH:mm:ss') : undefined;
    setQuery({
      pageNo: 1,
      pageSize: query.pageSize || 20,
      userId: values.userId,
      action: values.action,
      resource: values.resource,
      keyword: values.keyword,
      startTime,
      endTime,
    });
  };

  const handleReset = () => {
    form.resetFields();
    setQuery({ pageNo: 1, pageSize: 20 });
  };

  const openDrawer = (record: AuditLogDetailResponse) => {
    setCurrent(record);
    setDrawerOpen(true);
  };

  const columns: ColumnsType<AuditLogDetailResponse> = [
    { title: '操作者', dataIndex: 'username', key: 'username', render: (v) => v || '-' },
    { title: '动作', dataIndex: 'action', key: 'action', render: (v) => v || '-' },
    { title: '资源', dataIndex: 'resource', key: 'resource', render: (v) => v || '-' },
    { title: '资源ID', dataIndex: 'resourceId', key: 'resourceId', render: (v) => v || '-' },
    { title: '结果', dataIndex: 'result', key: 'result', render: (v) => renderResultTag(v) },
    { title: '耗时(ms)', dataIndex: 'duration', key: 'duration', render: (v) => v ?? '-' },
    { title: '时间', dataIndex: 'createdAt', key: 'createdAt', render: (v) => formatDateTime(v) },
    {
      title: '操作',
      key: 'actions',
      render: (_, record) => (
        <Button type="link" icon={<EyeOutlined />} onClick={() => openDrawer(record)}>
          查看
        </Button>
      ),
    },
  ];

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card>
        <Form form={form} layout="inline">
          <Form.Item name="userId" label="用户ID">
            <InputNumber style={{ width: 120 }} placeholder="用户ID" />
          </Form.Item>
          <Form.Item name="action" label="动作">
            <Input placeholder="动作" allowClear />
          </Form.Item>
          <Form.Item name="resource" label="资源">
            <Input placeholder="资源" allowClear />
          </Form.Item>
          <Form.Item name="keyword" label="关键字">
            <Input placeholder="关键字" allowClear />
          </Form.Item>
          <Form.Item name="range" label="时间范围">
            <RangePicker showTime />
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
        title="审计日志"
        extra={
          <Button icon={<ReloadOutlined />} onClick={() => setQuery({ ...query })} />
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

      <Drawer
        title="审计详情"
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        width={640}
      >
        {current ? (
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Card size="small" title="基本信息">
              <Space direction="vertical" size="small" style={{ width: '100%' }}>
                <Text>操作者：{current.username || '-'}</Text>
                <Text>用户ID：{current.userId ?? '-'}</Text>
                <Text>动作：{current.action || '-'}</Text>
                <Text>资源：{current.resource || '-'}</Text>
                <Text>资源ID：{current.resourceId || '-'}</Text>
                <Text>资源名称：{current.resourceName || '-'}</Text>
                <Text>结果：{renderResultTag(current.result)}</Text>
                <Text>错误信息：{current.errorMessage || '-'}</Text>
                <Text>IP：{current.ip || '-'}</Text>
                <Text>耗时(ms)：{current.duration ?? '-'}</Text>
                <Text>TraceId：{current.traceId || '-'}</Text>
                <Text>时间：{formatDateTime(current.createdAt)}</Text>
              </Space>
            </Card>
            <Card size="small" title="详情内容">
              {current.detail ? (
                <Paragraph copyable style={{ whiteSpace: 'pre-wrap' }}>
                  {current.detail}
                </Paragraph>
              ) : (
                <Text type="secondary">无详情内容</Text>
              )}
            </Card>
            <Card size="small" title="User Agent">
              <Paragraph copyable style={{ whiteSpace: 'pre-wrap' }}>
                {current.userAgent || '-'}
              </Paragraph>
            </Card>
          </Space>
        ) : (
          <Text type="secondary">请选择日志查看详情</Text>
        )}
      </Drawer>
    </Space>
  );
};

export default AuditLogListPage;
