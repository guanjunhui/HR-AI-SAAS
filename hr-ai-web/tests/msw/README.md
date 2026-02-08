# MSW 启用说明

当前仓库未安装 `msw` 依赖。安装后可按以下结构启用：

- `tests/msw/handlers.ts`：定义接口 mock
- `tests/msw/server.ts`：node 测试服务器
- `tests/jest/setup.ts`：`beforeAll/afterEach/afterAll` 生命周期

建议命令：
- `npm install -D msw jest ts-jest @types/jest @testing-library/react @testing-library/jest-dom`
