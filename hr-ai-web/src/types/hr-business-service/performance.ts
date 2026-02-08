import type { PageQuery, PageResponse } from './common';

export interface PerformancePredictionQuery extends PageQuery {
  orgUnitId?: number;
  keyword?: string;
  cycle?: string;
}

export interface PerformancePredictionItem {
  predictionId: number;
  employeeId: number;
  employeeName: string;
  cycle: string;
  predictedScore: number;
  calibratedScore?: number;
  confidence: number;
  factors: string[];
  updatedAt: string;
}

export type PerformancePredictionPage = PageResponse<PerformancePredictionItem>;

export interface PerformanceCalibrationRequest {
  calibratedScore: number;
  reason: string;
}
