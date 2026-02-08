import React from 'react';
import { Button, Card, Result } from 'antd';
import { useNotificationStore } from '@/stores/useNotificationStore';

interface ErrorBoundaryState {
  hasError: boolean;
  errorMessage?: string;
}

class AppErrorBoundaryInner extends React.Component<React.PropsWithChildren, ErrorBoundaryState> {
  state: ErrorBoundaryState = { hasError: false };

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, errorMessage: error.message };
  }

  componentDidCatch(error: Error): void {
    // 统一记录前端运行时异常
    useNotificationStore.getState().pushNotice({
      type: 'error',
      title: '页面发生异常',
      description: error.message,
    });
  }

  handleReload = () => {
    window.location.reload();
  };

  render(): React.ReactNode {
    if (!this.state.hasError) {
      return this.props.children;
    }

    return (
      <Card style={{ margin: 24 }}>
        <Result
          status="error"
          title="系统异常"
          subTitle={this.state.errorMessage || '页面加载失败，请稍后重试。'}
          extra={<Button type="primary" onClick={this.handleReload}>刷新页面</Button>}
        />
      </Card>
    );
  }
}

const AppErrorBoundary: React.FC<React.PropsWithChildren> = ({ children }) => {
  return <AppErrorBoundaryInner>{children}</AppErrorBoundaryInner>;
};

export default AppErrorBoundary;
