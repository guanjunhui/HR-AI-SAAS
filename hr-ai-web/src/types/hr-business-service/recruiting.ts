export interface ResumeParseRequest {
  sourceType: 'text' | 'url';
  content: string;
}

export interface ParsedResumeField {
  key: string;
  label: string;
  value: string;
  confidence: number;
}

export interface ResumeParseResult {
  candidateId: number;
  summary: string;
  matchScore: number;
  fields: ParsedResumeField[];
  rawText?: string;
}

export interface CandidatePatchRequest {
  fields: Record<string, string>;
}
