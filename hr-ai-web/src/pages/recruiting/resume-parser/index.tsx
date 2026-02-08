import React, { useMemo, useState } from 'react';
import { Alert, Button, Card, Form, Input, InputNumber, Skeleton, Space, Table, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { parseResumeApi, patchCandidateFieldsApi } from '@/services/hr-business-service';
import { useDelayedSkeleton } from '@/hooks/useDelayedSkeleton';
import type { ParsedResumeField } from '@/types/hr-business-service';

interface ParseFormValues {
  candidateId: number;
  content: string;
}

interface EditableField extends ParsedResumeField {
  editableValue: string;
}

const ResumeParserPage: React.FC = () => {
  const [form] = Form.useForm<ParseFormValues>();
  const [fields, setFields] = useState<EditableField[]>([]);
  const [summary, setSummary] = useState('');
  const [matchScore, setMatchScore] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [fromFallback, setFromFallback] = useState(false);

  const showSkeleton = useDelayedSkeleton(loading, 500);

  const columns: ColumnsType<EditableField> = useMemo(() => [
    { title: '字段', dataIndex: 'label', key: 'label', width: 180 },
    { title: '置信度', dataIndex: 'confidence', key: 'confidence', width: 120, render: (value: number) => `${value}%` },
    {
      title: '建议值（可编辑）',
      dataIndex: 'editableValue',
      key: 'editableValue',
      render: (_, record) => (
        <Input
          value={record.editableValue}
          onChange={(event) => {
            const nextValue = event.target.value;
            setFields((prev) => prev.map((item) => (item.key === record.key ? { ...item, editableValue: nextValue } : item)));
          }}
        />
      ),
    },
  ], []);

  const handleParse = async () => {
    const values = await form.validateFields();
    setLoading(true);
    try {
      const result = await parseResumeApi(values.candidateId, { sourceType: 'text', content: values.content });
      setSummary(result.data.summary);
      setMatchScore(result.data.matchScore);
      setFields(result.data.fields.map((item) => ({ ...item, editableValue: item.value })));
      setFromFallback(result.fromFallback);
      message.success('简历解析完成');
    } catch {
      // 错误由统一拦截器处理
    } finally {
      setLoading(false);
    }
  };

  const handleWriteBack = async () => {
    const candidateId = form.getFieldValue('candidateId') as number | undefined;
    if (!candidateId) {
      message.error('请先输入候选人ID');
      return;
    }

    const payload = fields.reduce<Record<string, string>>((acc, field) => {
      acc[field.key] = field.editableValue;
      return acc;
    }, {});

    try {
      await patchCandidateFieldsApi(candidateId, { fields: payload });
      message.success('字段回填成功');
    } catch {
      // 错误由统一拦截器处理
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card title="智能简历解析与字段回填">
        <Form form={form} layout="vertical">
          <Form.Item name="candidateId" label="候选人ID" rules={[{ required: true, message: '请输入候选人ID' }]}>
            <InputNumber style={{ width: '100%' }} placeholder="候选人 ID" />
          </Form.Item>
          <Form.Item name="content" label="简历内容" rules={[{ required: true, message: '请输入简历内容' }]}>
            <Input.TextArea rows={8} placeholder="粘贴简历全文" />
          </Form.Item>
          <Button type="primary" loading={loading} onClick={handleParse}>解析简历</Button>
        </Form>
      </Card>

      {fromFallback && (
        <Alert
          type="warning"
          showIcon
          message="当前展示的是本地缓存结果"
          description="服务不可用时自动降级，回填前请确认字段。"
        />
      )}

      <Card
        title="解析结果"
        extra={<Button type="primary" disabled={fields.length === 0} onClick={handleWriteBack}>回填候选人字段</Button>}
      >
        {showSkeleton ? (
          <Skeleton active paragraph={{ rows: 6 }} />
        ) : (
          <>
            <p><strong>匹配得分：</strong>{matchScore ?? '-'}%</p>
            <p><strong>AI 摘要：</strong>{summary || '-'}</p>
            <Table rowKey="key" columns={columns} dataSource={fields} pagination={false} />
          </>
        )}
      </Card>
    </Space>
  );
};

export default ResumeParserPage;
