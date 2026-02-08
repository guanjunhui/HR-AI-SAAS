import { compareMetrics } from './lib/consistency.mjs';

const FRONT_URL = process.env.FRONTEND_METRIC_SOURCE || 'http://127.0.0.1:5173/api/metrics/cache';
const BACK_URL = process.env.BACKEND_METRIC_SOURCE || 'http://127.0.0.1:8082/api/v1/metrics/key-indicators';
const THRESHOLD = Number(process.env.DIFF_THRESHOLD || 0.01);

async function loadJson(url) {
  const response = await fetch(url, { headers: { Accept: 'application/json' } });
  if (!response.ok) {
    throw new Error(`请求失败: ${url} -> ${response.status}`);
  }
  return response.json();
}

async function main() {
  try {
    const [frontData, backData] = await Promise.all([loadJson(FRONT_URL), loadJson(BACK_URL)]);
    const frontMetrics = frontData?.data || frontData || {};
    const backMetrics = backData?.data || backData || {};

    const diffs = compareMetrics(frontMetrics, backMetrics, THRESHOLD);
    if (diffs.length > 0) {
      console.error('数据差异超阈值:');
      diffs.forEach((item) => {
        console.error(`${item.key}: front=${item.frontValue}, back=${item.backValue}, rate=${(item.diffRate * 100).toFixed(2)}%`);
      });
      process.exit(2);
    }

    console.log('数据一致性检查通过');
  } catch (error) {
    console.error('数据一致性检查失败:', error.message);
    process.exit(1);
  }
}

main();
