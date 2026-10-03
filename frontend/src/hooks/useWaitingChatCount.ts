import { useEffect, useState } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { supportChat } from '@/features/chat/services/support-chat.service';
import { watchSupportChat } from '@/features/chat/services/support-chat-events';

export function useWaitingChatCount() {
  const { token, user } = useAuth();
  const [waitingCount, setWaitingCount] = useState<number>(0);
  const isStaff = user?.roles?.some(r => ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_FRONT_DESK'].includes(r));

  useEffect(() => {
    if (!token || !isStaff) {
      setWaitingCount(0);
      return;
    }

    let isMounted = true;
    const fetchWaiting = async () => {
      try {
        const rooms = await supportChat.list();
        if (isMounted) {
          const waiting = rooms.filter(r => r.status === 'WAITING').length;
          setWaitingCount(waiting);
        }
      } catch {
        // Silent catch for background badge updates
      }
    };

    void fetchWaiting();
    const stopWatch = watchSupportChat(token, () => void fetchWaiting(), () => {});
    const timer = window.setInterval(() => {
      if (document.visibilityState === 'visible') void fetchWaiting();
    }, 15000);

    return () => {
      isMounted = false;
      stopWatch();
      window.clearInterval(timer);
    };
  }, [token, isStaff]);

  return waitingCount;
}
