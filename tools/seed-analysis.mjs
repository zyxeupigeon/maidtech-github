// Seed analysis: slime chunks + random_spread structure candidates around a
// center point, reimplementing vanilla Java RNG semantics (BigInt = Java long).
//
// Usage: node seed-analysis.mjs [worldSeed] [rangeBlocks] [centerX] [centerZ] [outFile]
// Defaults: 7584197480721263469, 3000, 0, 0, stdout

import fs from 'fs';

const WORLD_SEED = BigInt(process.argv[2] ?? 7584197480721263469n);
const RANGE = Number(process.argv[3] ?? 3000);
const CX = Number(process.argv[4] ?? 0);
const CZ = Number(process.argv[5] ?? 0);
const OUT = process.argv[6] ?? null;

const M48 = 0xFFFFFFFFFFFFn, MULT = 0x5DEECE66Dn, ADD = 0xBn;

class JR {
  constructor(seed) { this.setSeed(seed); }
  setSeed(seed) { this.s = Number(BigInt.asUintN(48, BigInt.asIntN(64, seed) ^ MULT)); }
  next(bits) {
    this.s = Number((BigInt(this.s) * MULT + ADD) & M48);
    return Number(BigInt.asIntN(32, BigInt(this.s) >> BigInt(48 - bits)));
  }
  nextLong() { const hi = this.next(32), lo = this.next(32); return BigInt.asIntN(64, (BigInt(hi) << 32n) + BigInt(lo)); }
  nextInt(bound) {
    let r = this.next(31); const m = bound - 1;
    if ((bound & m) === 0) return Number((BigInt(bound) * BigInt(r)) >> 31n);
    let u = r, rr = this.next(31);
    while (u - rr + m < 0) { u = rr; rr = this.next(31); }
    return u % bound;
  }
}
const wrap64 = v => BigInt.asIntN(64, v);

// ---- Slime chunks (vanilla World#getSeed insomnia check, exact) --------------
const base = new JR(WORLD_SEED);
const oddSeed = base.nextLong() / 2n * 2n + 1n;
const slimeChunks = [];
const cmin = -Math.ceil(RANGE / 16) + Math.floor(CX / 16), cmax = Math.ceil(RANGE / 16) + Math.floor(CX / 16);
const zmin = -Math.ceil(RANGE / 16) + Math.floor(CZ / 16), zmax = Math.ceil(RANGE / 16) + Math.floor(CZ / 16);
for (let cx = cmin; cx <= cmax; cx++) {
  for (let cz = zmin; cz <= zmax; cz++) {
    const s = wrap64(BigInt(cx) * oddSeed + BigInt(cz) * oddSeed ^ WORLD_SEED);
    const r = new JR(s);
    if (r.nextInt(10) === 0) slimeChunks.push([cx, cz]);
  }
}

// ---- Structure candidates (random_spread structure sets) ---------------------
// Vanilla 1.21.1 structure_set values: [name, spacing, separation, salt]
const SETS = [
  ['village', 34, 8, 10387312], ['pillager_outpost', 32, 8, 165745296],
  ['desert_pyramid', 32, 8, 14357617], ['jungle_temple', 32, 8, 14357619],
  ['swamp_hut', 32, 8, 14357620], ['igloo', 32, 8, 14357618],
  ['ocean_monument', 32, 5, 10387313], ['woodland_mansion', 80, 20, 10387319],
  ['ruined_portal', 40, 15, 34222645], ['shipwreck', 24, 4, 165745295],
  ['ocean_ruin', 20, 8, 14357621], ['ancient_city', 24, 8, 20083232],
  ['trail_ruins', 34, 8, 83469867], ['trial_chambers', 32, 12, 94251327],
];
const results = {};
for (const [name, spacing, sep, salt] of SETS) {
  const pts = [];
  const rmin = Math.floor((CX - RANGE) / 16 / spacing) - 1, rmax = Math.ceil((CX + RANGE) / 16 / spacing) + 1;
  const zrmin = Math.floor((CZ - RANGE) / 16 / spacing) - 1, zrmax = Math.ceil((CZ + RANGE) / 16 / spacing) + 1;
  for (let rx = rmin; rx <= rmax; rx++) {
    for (let rz = zrmin; rz <= zrmax; rz++) {
      const r = new JR(wrap64(BigInt(rx) * 341873128712n + BigInt(rz) * 132897987541n + WORLD_SEED + BigInt(salt)));
      const n = spacing - sep;
      const off1 = (r.nextInt(n) + r.nextInt(n)) >> 1;
      const off2 = (r.nextInt(n) + r.nextInt(n)) >> 1;
      const cx = rx * spacing + off1, cz = rz * spacing + off2;
      const bx = cx * 16 + 8, bz = cz * 16 + 8;
      if (Math.abs(bx - CX) <= RANGE && Math.abs(bz - CZ) <= RANGE) pts.push([bx, bz]);
    }
  }
  results[name] = pts.sort((a, b) => ((a[0]-CX)**2 + (a[1]-CZ)**2) - ((b[0]-CX)**2 + (b[1]-CZ)**2));
}

// ---- Output ------------------------------------------------------------------
const lines = [];
lines.push(`# Seed ${WORLD_SEED} — structures & slime chunks within ${RANGE} blocks of (${CX}, ${CZ})`);
lines.push('');
lines.push(`## Slime chunks (exact)`);
lines.push(`Total in range: **${slimeChunks.length}** chunks (~${(slimeChunks.length / ((cmax - cmin + 1) * (zmax - zmin + 1)) * 100).toFixed(1)}%).`);
lines.push(`Nearest 15 to center (chunk coords; block = chunk*16+8):`);
const near = slimeChunks.map(([x, z]) => [x, z, Math.hypot(x * 16 + 8 - CX, z * 16 + 8 - CZ)]).sort((a, b) => a[2] - b[2]).slice(0, 15);
for (const [x, z, d] of near) lines.push(`- chunk(${x}, ${z}) → block ≈ (${x * 16 + 8}, ?, ${z * 16 + 8}), distance ${Math.round(d)}`);
lines.push('');
lines.push('## Structure candidates (random_spread; biome validation NOT included)');
for (const [name, pts] of Object.entries(results)) {
  lines.push(`### ${name} (${pts.length})`);
  for (const [x, z] of pts.slice(0, 12)) lines.push(`- (${x}, ${z})`);
  if (pts.length > 12) lines.push(`- … ${pts.length} total`);
  lines.push('');
}
lines.push('Note: structure points are generation ATTEMPT positions; final generation still requires a matching biome (e.g. desert pyramid needs desert). Slime chunk results are exact.');
const report = lines.join('\n');
if (OUT) fs.writeFileSync(OUT, report);
else console.log(report);
console.log(`\nslime=${slimeChunks.length}, structures computed.`);
