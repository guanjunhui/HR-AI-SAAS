import { RouterProvider } from 'react-router-dom';
import { ConfigProvider, App as AntdApp } from 'antd';
import { router } from '@/config/routes';
import NotificationCenter from '@/components/NotificationCenter';
import AppErrorBoundary from '@/components/AppErrorBoundary';

function App() {
  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: '#1677ff',
        },
      }}
    >
      <AntdApp>
        <AppErrorBoundary>
          <NotificationCenter />
          <RouterProvider router={router} />
        </AppErrorBoundary>
      </AntdApp>
    </ConfigProvider>
  );
}

export default App;
