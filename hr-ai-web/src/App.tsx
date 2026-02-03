import { RouterProvider } from 'react-router-dom';
import { ConfigProvider } from 'antd';
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
      <RouterProvider router={router} />
    </ConfigProvider>
  );
}

export default App;
