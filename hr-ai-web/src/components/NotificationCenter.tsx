import { useEffect } from 'react';
import { App as AntdApp } from 'antd';
import { useNotificationStore } from '@/stores/useNotificationStore';

const NotificationCenter: React.FC = () => {
  const { notification } = AntdApp.useApp();
  const notices = useNotificationStore((state) => state.notices);
  const removeNotice = useNotificationStore((state) => state.removeNotice);

  useEffect(() => {
    notices.forEach((notice) => {
      notification[notice.type]({
        key: notice.id,
        message: notice.title,
        description: notice.description,
        placement: 'topRight',
      });
      removeNotice(notice.id);
    });
  }, [notices, notification, removeNotice]);

  return null;
};

export default NotificationCenter;
