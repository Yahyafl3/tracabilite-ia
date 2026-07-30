import http from "k6/http";
import { check, sleep } from "k6";

/**
 * Smoke k6 — exécuter via Docker :
 * docker run --rm -i --network host -e BASE_URL=http://127.0.0.1:8080 -e EMAIL=... -e PASSWORD=... grafana/k6:0.54.0 run - < performance/k6-smoke.js
 * Ne pas coder de secrets dans le fichier.
 */
export const options = {
  vus: 1,
  duration: "45s",
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["p(95)<3000"],
  },
};

const BASE_URL = __ENV.BASE_URL || "http://127.0.0.1:8080";
const EMAIL = __ENV.EMAIL || "";
const PASSWORD = __ENV.PASSWORD || "";

export function setup() {
  if (!EMAIL || !PASSWORD) {
    return { token: "" };
  }
  const res = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: EMAIL, motDePasse: PASSWORD }),
    { headers: { "Content-Type": "application/json" } }
  );
  if (res.status !== 200) {
    return { token: "" };
  }
  const body = res.json();
  return { token: body.token || "" };
}

export default function (data) {
  const health = http.get(`${BASE_URL}/actuator/health`);
  check(health, { "health 200": (r) => r.status === 200 });

  if (data.token) {
    const headers = { Authorization: `Bearer ${data.token}` };
    const list = http.get(`${BASE_URL}/api/decisions?page=0&size=10`, { headers });
    check(list, { "list 200": (r) => r.status === 200 });
  }

  sleep(1);
}
