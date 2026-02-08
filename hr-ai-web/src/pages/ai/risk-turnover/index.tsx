import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Alert, Button, Card, Form, Modal, Select, Skeleton, Space, Statistic, Table, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { getTurnoverRiskDashboardApi, submitTurnoverRiskFeedbackApi } from '@/services/hr-business-service';
import { useDelayedSkeleton } from '@/hooks/useDelayedSkeleton';
import type { TurnoverRiskDashboard, TurnoverRiskItem } from '@/types/hr-business-service';

interface FeedbackFormValues {
  mark: 'false_positive' | 'confirmed' | 'followed';
  note?: string;
}

const LEVEL_COLOR_MAP: Record<string, string> = {
  high: 'red',
  medium: 'orange',
  low: 'green',
};

const LEVEL_TEXT_MAP: Record<string, string> = {
  high: '高',
  medium: '中',
  low: '低',
};

const TREND_TEXT_MAP: Record<string, string> = {
  up: '上升',
  down: '下降',
  stable: '稳定',
};

const RiskTurnoverPage: React.FC = () => {
  const [feedbackForm] = Form.useForm<FeedbackFormValues>();
  const [loading, setLoading] = useState(false);
  const [dashboard, setDashboard] = useState<TurnoverRiskDashboard | null>(null);
  const [fromFallback, setFromFallback] = useState(false);
  const [currentRiskId, setCurrentRiskId] = useState<number | null>(null);

  const showSkeleton = useDelayedSkeleton(loading, 500);

  const distributionMap = useMemo(() => {
    const map = new Map<string, number>();
    (dashboard?.distribution || []).forEach((item) => {
      map.set(item.level, item.count);
    });
    return map;
  }, [dashboard]);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const result = await getTurnoverRiskDashboardApi();
      setDashboard(result.data);
      setFromFallback(result.fromFallback);
    } catch {
      // 错误由统一拦截器处理
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleRefresh = useMemo(() => {
    let locked = false;
    return () => {
      if (locked) {
        return;
      }
      locked = true;
      loadData();
      window.setTimeout(() => {
        locked = false;
      }, 800);
    };
  }, [loadData]);

  const openFeedbackModal = (record: TurnoverRiskItem) => {
    setCurrentRiskId(record.riskId);
    feedbackForm.resetFields();
    feedbackForm.setFieldsValue({ mark: 'followed' });
  };

  const handleSubmitFeedback = async () => {
    if (!currentRiskId) {
      return;
    }
    const values = await feedbackForm.validateFields();
    try {
      await submitTurnoverRiskFeedbackApi(currentRiskId, values);
      message.success('反馈已提交');
      setCurrentRiskId(null);
      loadData();
    } catch {
      // 错误由统一拦截器处理
    }
  };

  const columns: ColumnsType<TurnoverRiskItem> = [
    { title: '员工', dataIndex: 'employeeName', key: 'employeeName', width: 140 },
    { title: '组织', dataIndex: 'orgUnitName', key: 'orgUnitName', width: 160 },
    {
      title: '风险等级',
      dataIndex: 'level',
      key: 'level',
      width: 120,
      render: (level: string) => <Tag color={LEVEL_COLOR_MAP[level] || 'default'}>{LEVEL_TEXT_MAP[level] || level}</Tag>,
    },
    { title: '风险分', dataIndex: 'score', key: 'score', width: 100 },
    {
      title: '风险原因',
      dataIndex: 'reasons',
      key: 'reasons',
      render: (reasons: string[]) => (
        <Space wrap>
          {(reasons || []).map((reason) => <Tag key={reason}>{reason}</Tag>)}
        </Space>
      ),
    },
    {
      title: '趋势',
      dataIndex: 'trend',
      key: 'trend',
      width: 100,
      render: (trend: string) => TREND_TEXT_MAP[trend] || trend,
    },
    {
      title: '操作',
      key: 'actions',
      width: 120,
      render: (_, record) => <Button type="link" onClick={() => openFeedbackModal(record)}>提交反馈</Button>,
    },
  ];

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card title="离职风险预警看板" extra={<Button onClick={handleRefresh}>刷新</Button>}>
        {showSkeleton ? (
          <Skeleton active paragraph={{ rows: 3 }} />
        ) : (
          <Space size="large">
            <Statistic title="员工总数" value={dashboard?.totalEmployees || 0} />
            <Statistic title="高风险" value={distributionMap.get('high') || 0} valueStyle={{ color: '#cf1322' }} />
            <Statistic title="中风险" value={distributionMap.get('medium') || 0} valueStyle={{ color: '#d46b08' }} />
            <Statistic title="低风险" value={distributionMap.get('low') || 0} valueStyle={{ color: '#389e0d' }} />
          </Space>
        )}
      </Card>

      {fromFallback && (
        <Alert
          type="warning"
          showIcon
          message="当前数据来自本地缓存兜底"
          description="离职风险服务不可用，建议恢复后重新生成看板。"
        />
      )}

      <Card title="高风险名单">
        {showSkeleton ? (
          <Skeleton active paragraph={{ rows: 8 }} />
        ) : (
          <Table rowKey="riskId" columns={columns} dataSource={dashboard?.highRiskList || []} loading={loading} pagination={false} />
        )}
      </Card>

      <Modal
        title="风险反馈"
        open={currentRiskId !== null}
        onCancel={() => setCurrentRiskId(null)}
        onOk={handleSubmitFeedback}
        destroyOnClose
      >
        <Form form={feedbackForm} layout="vertical">
          <Form.Item name="mark" label="反馈类型" rules={[{ required: true, message: '请选择反馈类型' }]}>
            <Select
              options={[
                { label: '误报', value: 'false_positive' },
                { label: '已确认', value: 'confirmed' },
                { label: '已跟进', value: 'followed' },
              ]}
            />
          </Form.Item>
          <Form.Item name="note" label="备注">
            <Select
              showSearch
              allowClear
              placeholder="可选备注"
              options={[
                { label: '已完成一对一沟通', value: '已完成一对一沟通' },
                { label: '员工已确认继续留任', value: '员工已确认继续留任' },
                { label: '建议加入重点关注名单', value: '建议加入重点关注名单' },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
};

export default RiskTurnoverPage;
