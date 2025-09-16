
import http from 'k6/http';
import { check, sleep } from 'k6';

const API_BASE_URL = 'http://localhost:8080'; // Adjust to your environment

export const options = {
  stages: [
    { duration: '10s', target: 100 }, // below normal load
    { duration: '1m', target: 100 },
    { duration: '10s', target: 1400 }, // spike to 1400 users
    { duration: '3m', target: 1400 }, // stay at 1400 for 3 minutes
    { duration: '10s', target: 100 }, // scale down. Recovery stage.
    { duration: '3m', target: 100 },
    { duration: '10s', target: 0 },
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
