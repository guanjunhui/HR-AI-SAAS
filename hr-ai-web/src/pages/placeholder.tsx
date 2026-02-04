import React from 'react';
import { Card, Typography } from 'antd';

interface PlaceholderProps {
  title: string;
  description?: string;
}

const PlaceholderPage: React.FC<PlaceholderProps> = ({ title, description }) => (
  <Card>
    <Typography.Title level={3}>{title}</Typography.Title>
    {description && <Typography.Paragraph>{description}</Typography.Paragraph>}
  </Card>
);

export const DashboardPage = () => (
  <PlaceholderPage
    title="Dashboard"
    description="Welcome to HR AI SaaS Workbench."
  />
);

export const HRPlaceholderPage = () => (
  <PlaceholderPage title="Core HR" />
);

export const RecruitingPlaceholderPage = () => (
  <PlaceholderPage title="Recruiting" />
);

export const AttendancePlaceholderPage = () => (
  <PlaceholderPage title="Attendance" />
);

export const PayrollPlaceholderPage = () => (
  <PlaceholderPage title="Payroll" />
);

export const PerformancePlaceholderPage = () => (
  <PlaceholderPage title="Performance" />
);

export const AIHubPlaceholderPage = () => (
  <PlaceholderPage title="AI Capability Center" />
);
