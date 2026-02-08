# 性能报告（iter-1）

## 构建产物
- 主包：`dist/assets/index-BI0eOESB.js` 288.98 kB（gzip 93.58 kB）
- antd chunk：`dist/assets/antd-upjfZCpt.js` 1183.23 kB（gzip 374.50 kB）

## 前端体验优化
- 对慢接口（>500ms）启用延迟 Skeleton：
  - `src/pages/onboarding/autofill/index.tsx`
  - `src/pages/recruiting/resume-parser/index.tsx`
  - `src/pages/performance/prediction/index.tsx`
  - `src/pages/ai/risk-turnover/index.tsx`
- 防抖：
  - `src/hooks/useDebouncedValue.ts`
  - 应用于入职补全输入与绩效查询关键词
- 节流：
  - 风险看板刷新按钮 800ms 节流

## 后续建议
- 在 CI 增加 Lighthouse 自动化并输出 score 阈值校验。
