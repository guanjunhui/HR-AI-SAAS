export const STATUS_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];

export const DATA_SCOPE_OPTIONS = [
  { label: '全部数据', value: 1 },
  { label: '本部门及下级', value: 2 },
  { label: '本部门', value: 3 },
  { label: '仅本人', value: 4 },
];

export const PLAN_TYPE_OPTIONS = [
  { label: 'free', value: 'free' },
  { label: 'pro', value: 'pro' },
  { label: 'enterprise', value: 'enterprise' },
];

export function getStatusLabel(value?: number | null): string {
  if (value === 1) return '启用';
  if (value === 0) return '停用';
  return '-';
}

export function getDataScopeLabel(value?: number | null): string {
  const match = DATA_SCOPE_OPTIONS.find((item) => item.value === value);
  return match ? match.label : '-';
}
