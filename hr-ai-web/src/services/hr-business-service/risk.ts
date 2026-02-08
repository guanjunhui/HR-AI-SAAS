import { hrClient } from './client';
import { withFallback } from '@/services/resilience/fallback';
import type { TurnoverRiskDashboard, TurnoverRiskFeedbackRequest } from '@/types/hr-business-service';

function defaultRiskDashboard(): TurnoverRiskDashboard {
  return {
    generatedAt: new Date().toISOString(),
    totalEmployees: 0,
    distribution: [
      { level: 'high', count: 0 },
      { level: 'medium', count: 0 },
      { level: 'low', count: 0 },
    ],
    highRiskList: [],
  };
}

export async function getTurnoverRiskDashboardApi(): Promise<{ data: TurnoverRiskDashboard; fromFallback: boolean }> {
  return withFallback(
    'turnover-risk-dashboard',
    async () => {
      const response = await hrClient.get<TurnoverRiskDashboard>('/v1/ai/risk/turnover/dashboard', {
        meta: { timeout: 30000, retry: 1, retryDelayMs: 1000 },
      });
      return response;
    },
    defaultRiskDashboard,
  );
}

export function submitTurnoverRiskFeedbackApi(riskId: number, params: TurnoverRiskFeedbackRequest): Promise<void> {
  return hrClient.post<void>(`/v1/ai/risk/turnover/${riskId}/feedback`, params, {
    meta: { timeout: 10000, retry: 0, idempotent: true },
  });
}
