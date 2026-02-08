import React, { useMemo, useState } from 'react';
import { Alert, Button, Card, Col, Form, Input, InputNumber, Row, Skeleton, Space, message } from 'antd';
import { autofillOnboardingFormApi, createOnboardingDraftApi } from '@/services/hr-business-service';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';
import { useDelayedSkeleton } from '@/hooks/useDelayedSkeleton';
import type { OnboardingAutofillResult } from '@/types/hr-business-service';

interface AutofillFormValues {
  candidateId?: number;
  resumeText: string;
}

const OnboardingAutofillPage: React.FC = () => {
  const [inputForm] = Form.useForm<AutofillFormValues>();
  const [draftForm] = Form.useForm<OnboardingAutofillResult>();
  const [loading, setLoading] = useState(false);
  const [fromFallback, setFromFallback] = useState(false);

  const showSkeleton = useDelayedSkeleton(loading, 500);
  const debouncedResumeText = useDebouncedValue(Form.useWatch('resumeText', inputForm) || '', 300);
  const unresolvedFields = Form.useWatch('unresolvedFields', draftForm) as string[] | undefined;

  const unresolvedText = useMemo(() => {
    if (!unresolvedFields || unresolvedFields.length === 0) {
      return '无';
    }
    return unresolvedFields.join('、');
  }, [unresolvedFields]);

  const handleAutofill = async () => {
    const values = await inputForm.validateFields();
    setLoading(true);
    try {
      const result = await autofillOnboardingFormApi({
        candidateId: values.candidateId,
        resumeText: values.resumeText,
      });
      draftForm.setFieldsValue(result.data);
      setFromFallback(result.fromFallback);
      message.success('AI 自动补全完成');
    } catch {
      // 错误由统一拦截器处理
    } finally {
      setLoading(false);
    }
  };

  const handleCreateDraft = async () => {
    const payload = await draftForm.validateFields();
    if (!payload.fullName) {
      message.error('请先补全姓名');
      return;
    }

    try {
      await createOnboardingDraftApi({
        fullName: payload.fullName,
        gender: payload.gender,
        phone: payload.phone,
        email: payload.email,
        idCard: payload.idCard,
        expectedOnboardDate: payload.expectedOnboardDate,
        orgUnitId: payload.orgUnitId,
        positionId: payload.positionId,
        workLocation: payload.workLocation,
      });
      message.success('入职草稿已创建');
    } catch {
      // 错误由统一拦截器处理
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card title="员工入职 AI 表单自动补全">
        <Form form={inputForm} layout="vertical">
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="candidateId" label="候选人ID">
                <InputNumber style={{ width: '100%' }} placeholder="可选" />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item
            name="resumeText"
            label="简历文本"
            rules={[{ required: true, message: '请输入简历文本' }, { min: 20, message: '至少输入 20 个字符' }]}
          >
            <Input.TextArea rows={8} placeholder="粘贴简历原文或关键信息" />
          </Form.Item>
          <Space>
            <Button type="primary" loading={loading} onClick={handleAutofill}>开始自动补全</Button>
            <span style={{ color: '#888' }}>当前字数：{debouncedResumeText.length}</span>
          </Space>
        </Form>
      </Card>

      {fromFallback && (
        <Alert
          type="warning"
          showIcon
          message="当前数据来自本地兜底缓存"
          description="hr-business-service 当前不可用，请在服务恢复后刷新重试。"
        />
      )}

      <Card title="补全结果（可人工校正后提交）" extra={<Button type="primary" onClick={handleCreateDraft}>保存入职草稿</Button>}>
        {showSkeleton ? (
          <Skeleton active paragraph={{ rows: 8 }} />
        ) : (
          <Form form={draftForm} layout="vertical">
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item name="fullName" label="姓名" rules={[{ required: true, message: '姓名必填' }]}>
                  <Input placeholder="姓名" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="gender" label="性别">
                  <Input placeholder="性别" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="phone" label="手机号">
                  <Input placeholder="手机号" />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item name="email" label="邮箱">
                  <Input placeholder="邮箱" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="idCard" label="身份证">
                  <Input placeholder="身份证" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="workLocation" label="工作地点">
                  <Input placeholder="工作地点" />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item name="orgUnitId" label="部门ID">
                  <InputNumber style={{ width: '100%' }} placeholder="部门 ID" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="positionId" label="岗位ID">
                  <InputNumber style={{ width: '100%' }} placeholder="岗位 ID" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="expectedOnboardDate" label="预计入职日期">
                  <Input placeholder="YYYY-MM-DD" />
                </Form.Item>
              </Col>
            </Row>
            <Form.Item name="confidenceScore" label="AI 置信度">
              <Input disabled />
            </Form.Item>
            <Alert type="info" showIcon message={`仍需人工确认字段：${unresolvedText}`} />
          </Form>
        )}
      </Card>
    </Space>
  );
};

export default OnboardingAutofillPage;
