#!/bin/bash

echo "🚀 Building Splitwise Microservices..."

# Set JAVA_HOME to Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo "✓ Using Java 17: $JAVA_HOME"

# Build all services
echo "📦 Building all services with Maven..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Build completed successfully!"
    echo ""
    echo "Next steps:"
    echo "  1. Start infrastructure: docker compose up -d mongodb postgres kafka zookeeper zipkin"
    echo "  2. Run all services: docker compose up -d"
    echo "  3. Or run locally: cd <service-name> && mvn spring-boot:run"
else
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi
