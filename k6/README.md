# K6 Load Testing

This directory contains k6 scripts for load testing the Cartonization Service API.

## Prerequisites

- [k6](https://k6.io/docs/getting-started/installation/) installed on your local machine.
- [Node.js and npm](https://nodejs.org/en/download/) installed on your local machine.
- The Cartonization Service running and accessible at the `API_BASE_URL` specified in the scripts (default: `http://localhost:8080`).

## Dependencies

This project uses `npm` to manage dependencies. To install the required dependencies, run the following command in the `k6` directory:

```bash
npm install
```

## Scripts

This directory contains three types of load testing scripts:

### 1. Load Test (`load-test.js`)

This script simulates a normal load on the API. It's designed to test the system's performance under expected traffic conditions.

**Execution:**

```bash
k6 run load-test.js
```
or
```bash
npm test
```

**Stages:**

- **Ramp-up:** Ramps up from 1 to 100 virtual users over 5 minutes.
- **Sustain:** Stays at 100 virtual users for 10 minutes.
- **Ramp-down:** Ramps down from 100 to 0 virtual users over 5 minutes.

**Thresholds:**

- 99% of requests must complete in under 1.5 seconds.

### 2. Stress Test (`stress-test.js`)

This script is designed to find the limits of the system by gradually increasing the load beyond normal operational capacity. This helps to identify performance bottlenecks and determine how the system fails under extreme conditions.

**Execution:**

```bash
k6 run stress-test.js
```
or
```bash
npm run stress
```

**Stages:**

- **Stage 1:** Ramps up to 100 users over 2 minutes and sustains for 5 minutes (below normal load).
- **Stage 2:** Ramps up to 200 users over 2 minutes and sustains for 5 minutes (normal load).
- **Stage 3:** Ramps up to 300 users over 2 minutes and sustains for 5 minutes (around the breaking point).
- **Stage 4:** Ramps up to 400 users over 2 minutes and sustains for 5 minutes (beyond the breaking point).
- **Stage 5:** Ramps down to 0 users over 10 minutes (recovery stage).

### 3. Spike Test (`spike-test.js`)

This script tests how the system responds to sudden, massive spikes in traffic. This is useful for understanding the system's ability to recover from unexpected surges.

**Execution:**

```bash
k6 run spike-test.js
```
or
```bash
npm run spike
```

**Stages:**

- **Stage 1:** Ramps up to 100 users over 10 seconds and sustains for 1 minute (below normal load).
- **Stage 2:** Spikes to 1400 users in 10 seconds and sustains for 3 minutes.
- **Stage 3:** Ramps down to 100 users in 10 seconds and sustains for 3 minutes (recovery stage).
- **Stage 4:** Ramps down to 0 users in 10 seconds.

## Endpoints Tested

Both scripts target the following endpoints:

- `POST /packing-solutions`: The core business logic for calculating packing solutions.
- `GET /cartons`: A simple read-only endpoint for listing available cartons.

## Customization

You can customize the tests by modifying the `options` object in each script. For example, you can change the `stages`, `thresholds`, and `API_BASE_URL`.