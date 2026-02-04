import React, { useEffect, useState } from 'react';
import { Button, Card, Descriptions, Space, Spin, Tag, Typography } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getOrgUnitApi } from '@/services/org';
import type { OrgUnitDetailResponse } from '@/types/org';
import { getStatusLabel } from '@/utils/dicts';

const { Text } = Typography;

const OrgUnitDetailPage: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [detail, setDetail] = useState<OrgUnitDetailResponse | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const load = async () => {
      if (!id) return;
      setLoading(true);
      try {
        const data = await getOrgUnitApi(Number(id));
        setDetail(data);
      } catch {
        setDetail(null);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id]);

  return (
    <Card
      title="组织详情"
      extra={
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/org/units')}>
          返回列表
        </Button>
      }
    >
      {loading ? (
        <Spin />
      ) : detail ? (
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="组织名称">{detail.name}</Descriptions.Item>
          <Descriptions.Item label="组织编码">{detail.code}</Descriptions.Item>
          <Descriptions.Item label="组织类型">{detail.type}</Descriptions.Item>
          <Descriptions.Item label="上级组织ID">{detail.parentId ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="负责人ID">{detail.leaderId ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={detail.status === 1 ? 'green' : 'red'}>{getStatusLabel(detail.status)}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="层级">{detail.level ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="路径">{detail.path ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="排序">{detail.sortOrder ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{detail.createdAt ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="更新时间">{detail.updatedAt ?? '-'}</Descriptions.Item>
        </Descriptions>
      ) : (
        <Space direction="vertical">
          <Text type="secondary">未找到组织信息</Text>
        </Space>
      )}
    </Card>
  );
};

export default OrgUnitDetailPage;
