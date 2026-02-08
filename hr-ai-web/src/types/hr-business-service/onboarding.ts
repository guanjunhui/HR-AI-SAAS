export interface OnboardingAutofillRequest {
  candidateId?: number;
  resumeText?: string;
  attachments?: string[];
  manualInputs?: Record<string, string>;
}

export interface OnboardingAutofillResult {
  fullName?: string;
  gender?: string;
  phone?: string;
  email?: string;
  idCard?: string;
  expectedOnboardDate?: string;
  orgUnitId?: number;
  positionId?: number;
  workLocation?: string;
  confidenceScore?: number;
  unresolvedFields?: string[];
}

export interface OnboardingDraftPayload {
  fullName: string;
  gender?: string;
  phone?: string;
  email?: string;
  idCard?: string;
  expectedOnboardDate?: string;
  orgUnitId?: number;
  positionId?: number;
  workLocation?: string;
}
