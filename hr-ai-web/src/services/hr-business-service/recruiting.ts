import { hrClient } from './client';
import { withFallback } from '@/services/resilience/fallback';
import type { CandidatePatchRequest, ResumeParseRequest, ResumeParseResult } from '@/types/hr-business-service';

export async function parseResumeApi(candidateId: number, params: ResumeParseRequest): Promise<{ data: ResumeParseResult; fromFallback: boolean }> {
  return withFallback(
    `resume-parse-${candidateId}`,
    async () => {
      const response = await hrClient.post<ResumeParseResult>(`/v1/recruiting/candidates/${candidateId}/parse-resume`, params, {
        meta: { timeout: 30000, retry: 0 },
      });
      return response;
    },
    () => ({
      candidateId,
      summary: '当前使用本地兜底数据，请在服务恢复后重试。',
      matchScore: 0,
      fields: [],
    }),
  );
}

export function patchCandidateFieldsApi(candidateId: number, params: CandidatePatchRequest): Promise<void> {
  return hrClient.put<void>(`/v1/recruiting/candidates/${candidateId}`, params, {
    meta: { idempotent: true, timeout: 10000, retry: 0 },
  });
}
