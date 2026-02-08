import { hrClient } from './client';
import { withFallback } from '@/services/resilience/fallback';
import type { OnboardingAutofillRequest, OnboardingAutofillResult, OnboardingDraftPayload } from '@/types/hr-business-service';

const ONBOARDING_AUTOFILL_FALLBACK_KEY = 'onboarding-autofill';

export async function autofillOnboardingFormApi(params: OnboardingAutofillRequest): Promise<{ data: OnboardingAutofillResult; fromFallback: boolean }> {
  return withFallback(
    ONBOARDING_AUTOFILL_FALLBACK_KEY,
    async () => {
      const response = await hrClient.post<OnboardingAutofillResult>('/v1/onboarding/forms/autofill', params, {
        meta: { timeout: 30000, retry: 0 },
      });
      return response;
    },
    () => ({
      fullName: '',
      confidenceScore: 0,
      unresolvedFields: ['fullName', 'phone', 'email', 'orgUnitId', 'positionId'],
    }),
  );
}

export function createOnboardingDraftApi(params: OnboardingDraftPayload): Promise<number> {
  return hrClient.post<number>('/v1/onboarding', params, {
    meta: { idempotent: true, timeout: 15000, retry: 0 },
  });
}
