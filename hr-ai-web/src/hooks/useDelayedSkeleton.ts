import { useEffect, useState } from 'react';

export function useDelayedSkeleton(loading: boolean, delayMs = 500): boolean {
  const [showSkeleton, setShowSkeleton] = useState(false);

  useEffect(() => {
    if (!loading) {
      const resetTimer = window.setTimeout(() => {
        setShowSkeleton(false);
      }, 0);
      return () => window.clearTimeout(resetTimer);
    }

    if (showSkeleton) {
      return;
    }

    const timer = window.setTimeout(() => {
      setShowSkeleton(true);
    }, delayMs);

    return () => window.clearTimeout(timer);
  }, [loading, delayMs, showSkeleton]);

  return showSkeleton;
}
