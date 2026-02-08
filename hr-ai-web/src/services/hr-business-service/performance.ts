import { hrClient } from './client';
import { withFallback } from '@/services/resilience/fallback';
import type { PerformanceCalibrationRequest, PerformancePredictionPage, PerformancePredictionQuery } from '@/types/hr-business-service';

function defaultPredictionPage(): PerformancePredictionPage {
  return {
    pageNo: 1,
    pageSize: 20,
    total: 0,
    records: [],
  };
}

export async function getPerformancePredictionsApi(params: PerformancePredictionQuery): Promise<{ data: PerformancePredictionPage; fromFallback: boolean }> {
  const cacheKey = `performance-predictions-${JSON.stringify(params)}`;
  return withFallback(
    cacheKey,
    async () => {
      const response = await hrClient.get<PerformancePredictionPage>('/v1/performance/predictions', {
        params,
        meta: { timeout: 10000, retry: 1, retryDelayMs: 500 },
      });
      return response;
    },
    defaultPredictionPage,
  );
}

export function calibratePerformancePredictionApi(predictionId: number, params: PerformanceCalibrationRequest): Promise<void> {
  return hrClient.post<void>(`/v1/performance/predictions/${predictionId}/calibrate`, params, {
    meta: { timeout: 10000, retry: 0, idempotent: true },
  });
}
