#!/bin/bash

echo "🧪 Testing Splitwise Services..."

# Set JAVA_HOME to Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Test User Service
echo "📝 Testing User Service..."
cd user-service
mvn test
cd ..

# Add more tests as services are completed
echo "✅ Tests completed!"
