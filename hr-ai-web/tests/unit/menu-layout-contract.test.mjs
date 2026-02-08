import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const layoutCode = readFileSync('src/layouts/BasicLayout.tsx', 'utf-8');

test('侧边菜单应通过 onOpenChange 管理展开状态，避免 openKeys 锁死', () => {
  assert.equal(layoutCode.includes('const [openKeys, setOpenKeys] = useState<string[]>([]);'), true);
  assert.equal(layoutCode.includes('const resolvedOpenKeys = useMemo(() => {'), true);
  assert.equal(layoutCode.includes('onOpenChange={(keys) => setOpenKeys(keys as string[])}'), true);
  assert.equal(layoutCode.includes('openKeys={resolvedOpenKeys}'), true);
});
