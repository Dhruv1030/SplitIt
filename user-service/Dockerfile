# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY user-service/pom.xml user-service/
RUN mvn dependency:go-offline -pl user-service -am -q || true
COPY . .
RUN mvn clean package -pl user-service -am -DskipTests

# ---- Run Stage ----
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl \
    && addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /app/user-service/target/*.jar app.jar
RUN chown app:app app.jar
USER app
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=10s --retries=5 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["java","-jar","app.jar"]
