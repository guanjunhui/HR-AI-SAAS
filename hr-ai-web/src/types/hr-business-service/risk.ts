export type RiskLevel = 'high' | 'medium' | 'low';

export interface RiskDistribution {
  level: RiskLevel;
  count: number;
}

export interface TurnoverRiskItem {
  riskId: number;
  employeeId: number;
  employeeName: string;
  orgUnitName: string;
  level: RiskLevel;
  score: number;
  reasons: string[];
  trend: 'up' | 'down' | 'stable';
  updatedAt: string;
}

export interface TurnoverRiskDashboard {
  generatedAt: string;
  totalEmployees: number;
  distribution: RiskDistribution[];
  highRiskList: TurnoverRiskItem[];
}

export interface TurnoverRiskFeedbackRequest {
  mark: 'false_positive' | 'confirmed' | 'followed';
  note?: string;
}
