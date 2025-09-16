// MongoDB initialization script for Cartonization Service
// This script runs when the MongoDB container starts for the first time

// Switch to the cartonization database
db = db.getSiblingDB('cartonization');

// Create the application user with read/write permissions
db.createUser({
  user: 'cartonization_user',
  pwd: 'cartonization_pass',
  roles: [
    {
      role: 'readWrite',
      db: 'cartonization'
    }
  ]
});

// Create collections with validation schemas
db.createCollection('cartons', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['name', 'dimensions', 'maxWeight', 'status'],
      properties: {
        _id: {
          bsonType: 'string',
          description: 'Unique carton identifier'
        },
        name: {
          bsonType: 'string',
          minLength: 1,
          maxLength: 100,
          description: 'Carton name - required and must be a string'
        },
        dimensions: {
          bsonType: 'object',
          required: ['length', 'width', 'height', 'unit'],
          properties: {
            length: {
              bsonType: 'double',
              minimum: 0.1,
              description: 'Length must be a positive number'
            },
            width: {
              bsonType: 'double',
              minimum: 0.1,
              description: 'Width must be a positive number'
            },
            height: {
              bsonType: 'double',
              minimum: 0.1,
              description: 'Height must be a positive number'
            },
            unit: {
              enum: ['CENTIMETERS', 'INCHES'],
              description: 'Unit must be either CENTIMETERS or INCHES'
            }
          }
        },
        maxWeight: {
          bsonType: 'object',
          required: ['value', 'unit'],
          properties: {
            value: {
              bsonType: 'double',
              minimum: 0.01,
              description: 'Weight value must be positive'
            },
            unit: {
              enum: ['KILOGRAMS', 'POUNDS'],
              description: 'Weight unit must be either KILOGRAMS or POUNDS'
            }
          }
        },
        status: {
          enum: ['ACTIVE', 'INACTIVE'],
          description: 'Status must be either ACTIVE or INACTIVE'
        },
        createdAt: {
          bsonType: 'date',
          description: 'Creation timestamp'
        },
        updatedAt: {
          bsonType: 'date',
          description: 'Last update timestamp'
        }
      }
    }
  }
});

db.createCollection('packing_solutions', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['solutionId', 'packages'],
      properties: {
        solutionId: {
          bsonType: 'string',
          description: 'Unique solution identifier'
        },
        requestId: {
          bsonType: 'string',
          description: 'Request tracking identifier'
        },
        orderId: {
          bsonType: 'string',
          description: 'Order identifier'
        },
        packages: {
          bsonType: 'array',
          minItems: 0,
          description: 'Array of packages in the solution'
        },
        createdAt: {
          bsonType: 'date',
          description: 'Solution creation timestamp'
        }
      }
    }
  }
});

// Create indexes for better query performance
db.cartons.createIndex({ 'status': 1 });
db.cartons.createIndex({ 'name': 1 });
db.cartons.createIndex({ 'createdAt': -1 });
db.cartons.createIndex({ 'dimensions.length': 1, 'dimensions.width': 1, 'dimensions.height': 1 });

db.packing_solutions.createIndex({ 'requestId': 1 });
db.packing_solutions.createIndex({ 'orderId': 1 });
db.packing_solutions.createIndex({ 'createdAt': -1 });

// Insert sample carton data for development
db.cartons.insertMany([
  {
    _id: 'small-box-001',
    name: 'Small Box',
    dimensions: {
      length: 20.0,
      width: 15.0,
      height: 10.0,
      unit: 'CENTIMETERS'
    },
    maxWeight: {
      value: 5.0,
      unit: 'KILOGRAMS'
    },
    status: 'ACTIVE',
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    _id: 'medium-box-001',
    name: 'Medium Box',
    dimensions: {
      length: 35.0,
      width: 25.0,
      height: 20.0,
      unit: 'CENTIMETERS'
    },
    maxWeight: {
      value: 15.0,
      unit: 'KILOGRAMS'
    },
    status: 'ACTIVE',
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    _id: 'large-box-001',
    name: 'Large Box',
    dimensions: {
      length: 50.0,
      width: 40.0,
      height: 30.0,
      unit: 'CENTIMETERS'
    },
    maxWeight: {
      value: 25.0,
      unit: 'KILOGRAMS'
    },
    status: 'ACTIVE',
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);

print('✅ Cartonization database initialized successfully');
print('📦 Created collections: cartons, packing_solutions');
print('👤 Created user: cartonization_user');
print('📊 Created indexes for performance optimization');
print('🎯 Inserted sample carton data');