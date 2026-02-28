# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -pl payment-service -am -DskipTests

# ---- Run Stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/payment-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","app.jar"]
