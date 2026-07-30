import http from "k6/http";
import { check, sleep } from "k6";

/**
 * Charge légère — secrets via env uniquement.
 * docker run --rm -i --network host -e BASE_URL=http://127.0.0.1:8080 -e EMAIL=... -e PASSWORD=... grafana/k6:0.54.0 run - < performance/k6-load.js
 */
export const options = {
  stages: [
    { duration: "20s", target: 5 },
    { duration: "40s", target: 10 },
    { duration: "20s", target: 0 },
  ],
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<2000"],
  },
};

const BASE_URL = __ENV.BASE_URL || "http://127.0.0.1:8080";
const EMAIL = __ENV.EMAIL || "";
const PASSWORD = __ENV.PASSWORD || "";

export function setup() {
  if (!EMAIL || !PASSWORD) return { token: "" };
  const res = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: EMAIL, motDePasse: PASSWORD }),
    { headers: { "Content-Type": "application/json" } }
  );
  if (res.status !== 200) return { token: "" };
  return { token: res.json().token || "" };
}

export default function (data) {
  const health = http.get(`${BASE_URL}/actuator/health`);
  check(health, { "health ok": (r) => r.status === 200 });
  if (data.token) {
    http.get(`${BASE_URL}/api/decisions?page=0&size=20`, {
      headers: { Authorization: `Bearer ${data.token}` },
    });
  }
  sleep(0.5);
}
