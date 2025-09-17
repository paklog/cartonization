# Pre-load Cartons

This directory contains a Postman collection to pre-load the Cartonization Service with a set of 20 standard carton sizes.

## Prerequisites

- [Node.js and npm](https://nodejs.org/en/download/) installed on your local machine.
- [Newman](https://learning.postman.com/docs/running-collections/using-newman-cli/installing-running-newman/) installed on your local machine. You can install it with:
  ```bash
  npm install -g newman
  ```

## Usage

To run the collection, use the following command:

```bash
newman run load-cartons.postman_collection.json
```

### Configuring the API Base URL

By default, the collection is configured to send requests to `http://localhost:8080`. If your service is running on a different address, you can override the `API_BASE_URL` variable like this:

```bash
newman run load-cartons.postman_collection.json --env-var "API_BASE_URL=http://your-api-host:port"
```
