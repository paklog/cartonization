# Cartonization API - Postman Collection

This directory contains a comprehensive Postman collection for testing the Cartonization Service API, which provides intelligent packing solutions for items into cartons.

## 📁 Files

- **`Cartonization-API.postman_collection.json`** - Main Postman collection with all API endpoints
- **`Cartonization-Local.postman_environment.json`** - Environment variables for local development
- **`Cartonization-Docker.postman_environment.json`** - Environment variables for Docker deployment

## 🚀 Quick Start

### 1. Import Collection and Environment

1. Open Postman
2. Click **Import** button
3. Select and import the collection file: `Cartonization-API.postman_collection.json`
4. Import the appropriate environment file:
   - For local development: `Cartonization-Local.postman_environment.json`
   - For Docker: `Cartonization-Docker.postman_environment.json`

### 2. Select Environment

1. In Postman, select the imported environment from the dropdown in the top-right corner
2. Verify that `baseUrl` is set correctly (default: `http://localhost:8080`)

### 3. Start the Service

Make sure the Cartonization Service is running:

```bash
# Local development
./mvnw spring-boot:run

# Or with Docker
docker-compose up
```

### 4. Run the Collection

You can run individual requests or the entire collection:

- **Individual requests**: Click on any request and hit **Send**
- **Collection runner**: Click on the collection → **Run collection** → **Run Cartonization API**

## 📋 Collection Structure

### 1. 📦 Packing Solutions

Calculate optimal packing arrangements for items:

- **Calculate Packing Solution - Simple**: Basic packing request with books and clothing
- **Calculate Packing Solution - Complex**: Complex order with multiple electronics
- **Calculate Packing Solution - Fragile Items**: Handling of fragile items with special rules
- **Invalid Request - Missing Items**: Validation error testing

### 2. 📏 Carton Management

Manage carton types and specifications:

- **Create Carton - Small Box**: Create a 20x15x10cm carton for small items
- **Create Carton - Medium Box**: Create a 35x25x20cm carton for medium items  
- **Create Carton - Large Box**: Create a 50x40x30cm carton for large items
- **List All Cartons**: Retrieve all carton types
- **List Active Cartons Only**: Filter to show only active cartons
- **Get Carton by ID**: Retrieve specific carton details
- **Deactivate Carton**: Remove carton from active use
- **Invalid Carton Creation**: Validation error testing

### 3. 🏥 Health & Monitoring

System health and monitoring:

- **Health Check**: Basic health status of the service
- **System Information**: Detailed system metrics and JVM information

### 4. 📚 API Documentation

Access to API documentation:

- **OpenAPI Spec (JSON)**: Raw OpenAPI specification
- **Swagger UI**: Interactive API documentation

## 🧪 Test Scenarios

### Basic Workflow

1. **Health Check** - Verify service is running
2. **Create Cartons** - Set up available carton types
3. **Calculate Packing Solution** - Get optimal packing for items
4. **Manage Cartons** - List, retrieve, and deactivate cartons

### Advanced Testing

1. **Validation Testing** - Test invalid inputs and error handling
2. **Business Rules** - Test fragile items and category mixing rules
3. **Performance Testing** - Test complex packing scenarios
4. **System Monitoring** - Check health and system metrics

## 🔧 Environment Variables

The collection uses the following environment variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `baseUrl` | Service base URL | `http://localhost:8080` |
| `requestId` | Request tracking ID | `req-1642234567890` |
| `testCartonId` | Stored carton ID from tests | `carton-123` |
| `lastSolutionId` | Last packing solution ID | `sol-789` |
| `apiVersion` | API version | `v1` |
| `contentType` | Default content type | `application/json` |

## 📊 Test Automation

The collection includes automated tests for:

- **Response Status Codes**: Verify correct HTTP status codes
- **Response Structure**: Check required fields and data types
- **Business Logic**: Validate packing rules and calculations
- **Error Handling**: Ensure proper error responses
- **Performance**: Response time thresholds

### Example Test Output

```javascript
✓ Status code is 200
✓ Response has packing solution
✓ Packages contain items
✓ Response time is less than 2000ms
```

## 🛠️ Customization

### Adding New Tests

1. Create a new request in the appropriate folder
2. Add pre-request scripts for data setup:
   ```javascript
   pm.environment.set('requestId', 'req-' + Date.now());
   ```
3. Add test scripts for validation:
   ```javascript
   pm.test('Status code is 200', function () {
       pm.response.to.have.status(200);
   });
   ```

### Environment Configuration

Modify environment files to match your deployment:

```json
{
  "key": "baseUrl",
  "value": "https://your-service-url.com",
  "enabled": true
}
```

## 📖 API Endpoints Reference

### Packing Solutions
- `POST /api/v1/packing-solutions` - Calculate packing solution

### Carton Management  
- `POST /api/v1/cartons` - Create new carton
- `GET /api/v1/cartons` - List cartons
- `GET /api/v1/cartons/{id}` - Get carton by ID
- `DELETE /api/v1/cartons/{id}` - Deactivate carton

### Health & Monitoring
- `GET /api/v1/health` - Health check
- `GET /api/v1/health/info` - System information

### Documentation
- `GET /api-docs` - OpenAPI specification
- `GET /swagger-ui.html` - Swagger UI

## 🐛 Troubleshooting

### Common Issues

1. **Connection refused**: Ensure service is running on correct port
2. **404 Not Found**: Check API endpoints and version
3. **Validation errors**: Review request body format and required fields
4. **Timeout**: Increase timeout in Postman settings

### Debug Tips

1. Check the **Console** tab for detailed logs
2. Use **Pre-request Script** to log variables:
   ```javascript
   console.log('Base URL:', pm.environment.get('baseUrl'));
   ```
3. Enable **Postman Console** for detailed debugging

## 📞 Support

For issues and questions:

1. Check the [API Documentation](http://localhost:8080/swagger-ui.html)
2. Review service logs for error details
3. Verify environment variables are set correctly

## 🏷️ Version Information

- **Collection Version**: 1.0.0
- **API Version**: v1
- **Postman Version**: 10.20.0+
- **Service Version**: 1.0.0-SNAPSHOT

---

**Happy Testing! 🚀**