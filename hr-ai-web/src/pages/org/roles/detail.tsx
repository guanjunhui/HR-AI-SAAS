import React, { useEffect, useState } from 'react';
import { Button, Card, Descriptions, Space, Spin, Tag, Typography } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getRoleApi } from '@/services/role';
import type { RoleDetailResponse } from '@/types/role';
import { getDataScopeLabel, getStatusLabel } from '@/utils/dicts';

const { Text } = Typography;

const RoleDetailPage: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [detail, setDetail] = useState<RoleDetailResponse | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const load = async () => {
      if (!id) return;
      setLoading(true);
      try {
        const data = await getRoleApi(Number(id));
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
      title="角色详情"
      extra={
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/org/roles')}>
          返回列表
        </Button>
      }
    >
      {loading ? (
        <Spin />
      ) : detail ? (
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
          <Descriptions column={2} bordered size="small">
            <Descriptions.Item label="角色名称">{detail.name}</Descriptions.Item>
            <Descriptions.Item label="角色编码">{detail.code}</Descriptions.Item>
            <Descriptions.Item label="数据范围">{getDataScopeLabel(detail.dataScope)}</Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={detail.status === 1 ? 'green' : 'red'}>{getStatusLabel(detail.status)}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="排序">{detail.sortOrder ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="描述">{detail.description || '-'}</Descriptions.Item>
            <Descriptions.Item label="创建时间">{detail.createdAt || '-'}</Descriptions.Item>
            <Descriptions.Item label="更新时间">{detail.updatedAt || '-'}</Descriptions.Item>
          </Descriptions>
          <Card size="small" title="权限列表">
            {detail.permissions && detail.permissions.length ? (
              <Space wrap>
                {detail.permissions.map((p) => (
                  <Tag key={p}>{p}</Tag>
                ))}
              </Space>
            ) : (
              <Text type="secondary">暂无权限</Text>
            )}
          </Card>
        </Space>
      ) : (
        <Text type="secondary">未找到角色信息</Text>
      )}
    </Card>
  );
};

export default RoleDetailPage;
