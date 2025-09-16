
import http from 'k6/http';
import { check, sleep } from 'k6';

const API_BASE_URL = 'http://localhost:8080'; // Adjust to your environment

export const options = {
  stages: [
    { duration: '2m', target: 100 }, // below normal load
    { duration: '5m', target: 100 },
    { duration: '2m', target: 200 }, // normal load
    { duration: '5m', target: 200 },
    { duration: '2m', target: 300 }, // around the breaking point
    { duration: '5m', target: 300 },
    { duration: '2m', target: 400 }, // beyond the breaking point
    { duration: '5m', target: 400 },
    { duration: '10m', target: 0 }, // scale down. Recovery stage.
  ],
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
