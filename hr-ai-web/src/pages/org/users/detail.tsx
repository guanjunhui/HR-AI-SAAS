import React, { useEffect, useState } from 'react';
import { Button, Card, Descriptions, Spin, Tag, Typography } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getUserApi } from '@/services/user';
import type { UserDetailResponse } from '@/types/user';
import { getStatusLabel } from '@/utils/dicts';

const { Text } = Typography;

const UserDetailPage: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [detail, setDetail] = useState<UserDetailResponse | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const load = async () => {
      if (!id) return;
      setLoading(true);
      try {
        const data = await getUserApi(Number(id));
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
      title="用户详情"
      extra={
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/org/users')}>
          返回列表
        </Button>
      }
    >
      {loading ? (
        <Spin />
      ) : detail ? (
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="账号">{detail.username}</Descriptions.Item>
          <Descriptions.Item label="姓名">{detail.realName || '-'}</Descriptions.Item>
          <Descriptions.Item label="邮箱">{detail.email || '-'}</Descriptions.Item>
          <Descriptions.Item label="手机号">{detail.phone || '-'}</Descriptions.Item>
          <Descriptions.Item label="角色">{detail.roleName || '-'}</Descriptions.Item>
          <Descriptions.Item label="角色编码">{detail.roleCode || '-'}</Descriptions.Item>
          <Descriptions.Item label="组织ID">{detail.orgUnitId ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="方案类型">{detail.planType || '-'}</Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={detail.status === 1 ? 'green' : 'red'}>{getStatusLabel(detail.status)}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="最近登录时间">{detail.lastLoginTime || '-'}</Descriptions.Item>
          <Descriptions.Item label="最近登录IP">{detail.lastLoginIp || '-'}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{detail.createdAt || '-'}</Descriptions.Item>
          <Descriptions.Item label="更新时间">{detail.updatedAt || '-'}</Descriptions.Item>
        </Descriptions>
      ) : (
        <Text type="secondary">未找到用户信息</Text>
      )}
    </Card>
  );
};

export default UserDetailPage;
