# 上线手册（iter-1）

## 发布步骤
1. 执行前端质量门禁：
   - `npm run lint`
   - `npm run typecheck`
   - `npm test`
   - `npm run build`
2. 同步 OpenAPI：`npm run openapi:sync`
3. 发布前验证关键链路：
   - 入职 AI 自动补全
   - 简历解析回填
   - 绩效预测校准
   - 离职风险看板反馈

## 回滚方案
1. 回滚前端：切回上一个已发布前端镜像/静态包。
2. 回滚接口契约：恢复 `contracts/hr-business-service.openapi.yaml` 到上版本。
3. 关闭新功能入口：路由层移除新页面菜单（`BasicLayout.tsx`）并重发版。

## 风险提示
- 若后端未实现新增 AI 接口，前端会自动使用本地缓存兜底并提示告警。
