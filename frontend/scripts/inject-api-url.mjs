/**
 * Injects API_URL into environment.prod.ts at build time (Vercel / CI).
 * Empty API_URL keeps relative calls (Docker Nginx proxy /api → backend).
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const target = path.join(__dirname, '../src/environments/environment.prod.ts');
const apiUrl = (process.env.API_URL || process.env.NG_APP_API_URL || '').trim().replace(/\/$/, '');

const content = `export const environment = {
  production: true,
  /** Backend origin. Empty = same-origin (Nginx /api proxy). Set API_URL on Vercel. */
  apiUrl: ${JSON.stringify(apiUrl)},
};
`;

fs.writeFileSync(target, content, 'utf8');
console.log(`[inject-api-url] environment.prod.ts apiUrl=${apiUrl ? '(set)' : '(empty / relative)'}`);
