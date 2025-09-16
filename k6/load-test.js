
import http from 'k6/http';
import { check, sleep } from 'k6';

const API_BASE_URL = 'http://localhost:8080'; // Adjust to your environment

export const options = {
  stages: [
    { duration: '5m', target: 100 }, // simulate ramp-up of traffic from 1 to 100 users over 5 minutes.
    { duration: '10m', target: 100 }, // stay at 100 users for 10 minutes
    { duration: '5m', target: 0 }, // ramp-down to 0 users
  ],
  thresholds: {
    'http_req_duration': ['p(99)<1500'], // 99% of requests must complete below 1.5s
  },
};

export default function () {
  // Test the POST /packing-solutions endpoint
  const packingSolutionsPayload = JSON.stringify({
    items: [
      { sku: 'SKU123', quantity: 1 },
      { sku: 'SKU456', quantity: 2 },
    ],
  });

  const packingSolutionsParams = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const packingSolutionsRes = http.post(`${API_BASE_URL}/packing-solutions`, packingSolutionsPayload, packingSolutionsParams);
  check(packingSolutionsRes, {
    'packing solutions status is 200': (r) => r.status === 200,
  });

  sleep(1);

  // Test the GET /cartons endpoint
  const cartonsRes = http.get(`${API_BASE_URL}/cartons`);
  check(cartonsRes, {
    'get cartons status is 200': (r) => r.status === 200,
  });

  sleep(1);
}
