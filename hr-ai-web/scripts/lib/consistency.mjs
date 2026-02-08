export function computeDiffRate(frontValue, backValue) {
  if (typeof frontValue !== 'number' || typeof backValue !== 'number') {
    return 1;
  }
  if (backValue === 0) {
    return frontValue === 0 ? 0 : 1;
  }
  return Math.abs(frontValue - backValue) / Math.abs(backValue);
}

export function compareMetrics(frontMetrics, backMetrics, threshold = 0.01) {
  const keys = new Set([...Object.keys(frontMetrics || {}), ...Object.keys(backMetrics || {})]);
  const diffs = [];

  keys.forEach((key) => {
    const frontValue = frontMetrics?.[key];
    const backValue = backMetrics?.[key];
    const diffRate = computeDiffRate(frontValue, backValue);
    if (diffRate > threshold) {
      diffs.push({ key, frontValue, backValue, diffRate });
    }
  });

  return diffs;
}
