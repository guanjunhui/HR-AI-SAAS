import { RouterProvider } from 'react-router-dom';
import { ConfigProvider, App as AntdApp } from 'antd';
import { router } from '@/config/routes';

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
        <RouterProvider router={router} />
      </AntdApp>
    </ConfigProvider>
  );
}

export default App;
