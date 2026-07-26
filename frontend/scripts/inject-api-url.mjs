/**
 * Injects API_URL into environment.prod.ts at build time.
 *
 * - Unset API_URL → production default (Render backend)
 * - API_URL="" (Docker Nginx) → relative same-origin /api proxy
 * - API_URL=https://... → explicit override (Vercel)
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const DEFAULT_PROD_API_URL = 'https://tracabilite-ia-backend.onrender.com';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const target = path.join(__dirname, '../src/environments/environment.prod.ts');

const envKey = Object.prototype.hasOwnProperty.call(process.env, 'API_URL')
  ? 'API_URL'
  : Object.prototype.hasOwnProperty.call(process.env, 'NG_APP_API_URL')
    ? 'NG_APP_API_URL'
    : null;

const apiUrl = envKey
  ? String(process.env[envKey] || '')
      .trim()
      .replace(/\/$/, '')
  : DEFAULT_PROD_API_URL;

const content = `export const environment = {
  production: true,
  apiUrl: ${JSON.stringify(apiUrl)},
};
`;

fs.writeFileSync(target, content, 'utf8');
console.log(
  `[inject-api-url] environment.prod.ts apiUrl=${apiUrl ? apiUrl : '(empty / relative)'}`,
);
