import React, { useCallback, useEffect, useState } from 'react';
import { Alert, Button, Card, Form, Input, InputNumber, Modal, Skeleton, Space, Table, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { calibratePerformancePredictionApi, getPerformancePredictionsApi } from '@/services/hr-business-service';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';
import { useDelayedSkeleton } from '@/hooks/useDelayedSkeleton';
import type { PerformancePredictionItem, PerformancePredictionQuery } from '@/types/hr-business-service';

interface CalibrationFormValues {
  calibratedScore: number;
  reason: string;
}

const PerformancePredictionPage: React.FC = () => {
  const [queryForm] = Form.useForm<PerformancePredictionQuery>();
  const [calibrationForm] = Form.useForm<CalibrationFormValues>();
  const [data, setData] = useState<PerformancePredictionItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [fromFallback, setFromFallback] = useState(false);
  const [query, setQuery] = useState<PerformancePredictionQuery>({ pageNo: 1, pageSize: 20 });
  const [pagination, setPagination] = useState({ pageNo: 1, pageSize: 20, total: 0 });
  const [currentPredictionId, setCurrentPredictionId] = useState<number | null>(null);

  const debouncedKeyword = useDebouncedValue(query.keyword || '', 300);
  const showSkeleton = useDelayedSkeleton(loading, 500);

  const loadData = useCallback(async (params: PerformancePredictionQuery) => {
    setLoading(true);
    try {
      const result = await getPerformancePredictionsApi(params);
      setData(result.data.records || []);
      setPagination({
        pageNo: result.data.pageNo,
        pageSize: result.data.pageSize,
        total: result.data.total,
      });
      setFromFallback(result.fromFallback);
    } catch {
      // 错误由统一拦截器处理
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData({ ...query, keyword: debouncedKeyword });
  }, [query, debouncedKeyword, loadData]);

  const handleSearch = () => {
    const values = queryForm.getFieldsValue();
    setQuery({ ...values, pageNo: 1, pageSize: query.pageSize || 20 });
  };

  const openCalibrate = (record: PerformancePredictionItem) => {
    setCurrentPredictionId(record.predictionId);
    calibrationForm.setFieldsValue({
      calibratedScore: record.calibratedScore ?? record.predictedScore,
      reason: '',
    });
  };

  const handleCalibrate = async () => {
    if (!currentPredictionId) {
      return;
    }
    const values = await calibrationForm.validateFields();
    try {
      await calibratePerformancePredictionApi(currentPredictionId, values);
      message.success('校准已保存');
      setCurrentPredictionId(null);
      loadData({ ...query, keyword: debouncedKeyword });
    } catch {
      // 错误由统一拦截器处理
    }
  };

  const columns: ColumnsType<PerformancePredictionItem> = [
    { title: '员工', dataIndex: 'employeeName', key: 'employeeName', width: 150 },
    { title: '周期', dataIndex: 'cycle', key: 'cycle', width: 120 },
    { title: '预测分', dataIndex: 'predictedScore', key: 'predictedScore', width: 100 },
    { title: '校准分', dataIndex: 'calibratedScore', key: 'calibratedScore', width: 100, render: (value) => value ?? '-' },
    { title: '置信度', dataIndex: 'confidence', key: 'confidence', width: 120, render: (value) => `${value}%` },
    {
      title: '影响因子',
      dataIndex: 'factors',
      key: 'factors',
      render: (factors: string[]) => (
        <Space wrap>
          {(factors || []).map((factor) => <Tag key={factor}>{factor}</Tag>)}
        </Space>
      ),
    },
    {
      title: '操作',
      key: 'actions',
      width: 100,
      render: (_, record) => <Button type="link" onClick={() => openCalibrate(record)}>人工校准</Button>,
    },
  ];

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card title="绩效预测模型结果">
        <Form form={queryForm} layout="inline">
          <Form.Item name="keyword" label="关键词">
            <Input placeholder="员工姓名" allowClear />
          </Form.Item>
          <Form.Item name="cycle" label="周期">
            <Input placeholder="如 2026-Q1" allowClear />
          </Form.Item>
          <Form.Item>
            <Button type="primary" onClick={handleSearch}>查询</Button>
          </Form.Item>
        </Form>
      </Card>

      {fromFallback && (
        <Alert
          type="warning"
          showIcon
          message="当前展示的是本地兜底数据"
          description="性能预测服务不可用时自动降级，请谨慎用于正式决策。"
        />
      )}

      <Card title="预测列表">
        {showSkeleton ? (
          <Skeleton active paragraph={{ rows: 6 }} />
        ) : (
          <Table
            rowKey="predictionId"
            dataSource={data}
            columns={columns}
            loading={loading}
            pagination={{
              current: pagination.pageNo,
              pageSize: pagination.pageSize,
              total: pagination.total,
              onChange: (page, pageSize) => setQuery({ ...query, pageNo: page, pageSize }),
            }}
          />
        )}
      </Card>

      <Modal
        title="人工校准"
        open={currentPredictionId !== null}
        onCancel={() => setCurrentPredictionId(null)}
        onOk={handleCalibrate}
        destroyOnClose
      >
        <Form form={calibrationForm} layout="vertical">
          <Form.Item
            name="calibratedScore"
            label="校准分"
            rules={[{ required: true, message: '请输入校准分' }]}
          >
            <InputNumber min={0} max={100} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="reason"
            label="校准原因"
            rules={[{ required: true, message: '请输入校准原因' }, { min: 5, message: '至少输入 5 个字符' }]}
          >
            <Input.TextArea rows={4} placeholder="请说明人工校准依据" />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
};

export default PerformancePredictionPage;
