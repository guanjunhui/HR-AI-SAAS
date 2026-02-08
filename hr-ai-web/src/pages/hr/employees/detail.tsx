import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button, Card, Descriptions, Skeleton, Space, Tag, Timeline, message } from 'antd';
import { ArrowLeftOutlined, EditOutlined } from '@ant-design/icons';
import type { EmployeeDetailResponse, EmploymentEventDetailResponse } from '@/types/hr';
import { getEmployeeApi, getEmploymentEventsApi } from '@/services/hr';

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

const EVENT_TYPE_TEXT: Record<string, string> = {
    entry: '入职',
    regular: '转正',
    transfer: '调岗',
    promotion: '晋升',
    demotion: '降级',
    salary_change: '调薪',
    resignation: '离职',
};

const EVENT_STATUS_TEXT: Record<string, string> = {
    draft: '草稿',
    pending: '待审批',
    approved: '已通过',
    rejected: '已驳回',
    cancelled: '已取消',
};

const EmployeeDetailPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [employee, setEmployee] = useState<EmployeeDetailResponse | null>(null);
    const [events, setEvents] = useState<EmploymentEventDetailResponse[]>([]);
    const [loading, setLoading] = useState(true);

    const loadData = useCallback(async () => {
        if (!id) return;
        setLoading(true);
        try {
            const [empData, eventsData] = await Promise.all([
                getEmployeeApi(Number(id)),
                getEmploymentEventsApi({ employeeId: Number(id), pageSize: 50 }),
            ]);
            setEmployee(empData);
            setEvents(eventsData.records || []);
        } catch {
            message.error('加载数据失败');
        } finally {
            setLoading(false);
        }
    }, [id]);

    useEffect(() => {
        loadData();
    }, [loadData]);

    if (loading) {
        return (
            <Card>
                <Skeleton active />
            </Card>
        );
    }

    if (!employee) {
        return (
            <Card>
                <div style={{ textAlign: 'center', padding: '40px 0' }}>
                    <p>员工不存在或已删除</p>
                    <Button type="primary" onClick={() => navigate('/hr/employees')}>返回列表</Button>
                </div>
            </Card>
        );
    }

    return (
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Card
                title={
                    <Space>
                        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/hr/employees')} />
                        员工档案 - {employee.realName}
                    </Space>
                }
                extra={
                    <Button type="primary" icon={<EditOutlined />} onClick={() => navigate(`/hr/employees?edit=${employee.id}`)}>
                        编辑
                    </Button>
                }
            >
                <Descriptions bordered column={3}>
                    <Descriptions.Item label="工号">{employee.employeeCode}</Descriptions.Item>
                    <Descriptions.Item label="姓名">{employee.realName}</Descriptions.Item>
                    <Descriptions.Item label="状态">
                        <Tag color={STATUS_COLOR_MAP[employee.employeeStatus] || 'default'}>
                            {STATUS_TEXT_MAP[employee.employeeStatus] || employee.employeeStatus}
                        </Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="性别">{employee.gender === 'male' ? '男' : employee.gender === 'female' ? '女' : '-'}</Descriptions.Item>
                    <Descriptions.Item label="手机">{employee.phone || '-'}</Descriptions.Item>
                    <Descriptions.Item label="邮箱">{employee.email || '-'}</Descriptions.Item>
                    <Descriptions.Item label="身份证">{employee.idCard || '-'}</Descriptions.Item>
                    <Descriptions.Item label="组织">{employee.orgUnitName || '-'}</Descriptions.Item>
                    <Descriptions.Item label="岗位">{employee.positionName || '-'}</Descriptions.Item>
                    <Descriptions.Item label="直属上级">{employee.directManagerName || '-'}</Descriptions.Item>
                    <Descriptions.Item label="工作地点">{employee.workLocation || '-'}</Descriptions.Item>
                    <Descriptions.Item label="入职日期">{employee.entryDate || '-'}</Descriptions.Item>
                    <Descriptions.Item label="试用期结束">{employee.probationEndDate || '-'}</Descriptions.Item>
                    <Descriptions.Item label="转正日期">{employee.regularDate || '-'}</Descriptions.Item>
                    <Descriptions.Item label="离职日期">{employee.resignationDate || '-'}</Descriptions.Item>
                </Descriptions>
            </Card>

            <Card title="任职历史">
                {events.length === 0 ? (
                    <div style={{ textAlign: 'center', color: '#999', padding: '20px 0' }}>暂无任职记录</div>
                ) : (
                    <Timeline
                        mode="left"
                        items={events.map((event) => ({
                            color: event.status === 'approved' ? 'green' : event.status === 'rejected' ? 'red' : 'blue',
                            label: event.eventDate,
                            children: (
                                <div>
                                    <strong>{EVENT_TYPE_TEXT[event.eventType] || event.eventType}</strong>
                                    <Tag style={{ marginLeft: 8 }}>{EVENT_STATUS_TEXT[event.status] || event.status}</Tag>
                                    {event.reason && <div style={{ color: '#666', marginTop: 4 }}>原因: {event.reason}</div>}
                                    {event.fromPositionName && event.toPositionName && (
                                        <div style={{ color: '#666', marginTop: 4 }}>
                                            {event.fromPositionName} → {event.toPositionName}
                                        </div>
                                    )}
                                </div>
                            ),
                        }))}
                    />
                )}
            </Card>
        </Space>
    );
};

export default EmployeeDetailPage;
