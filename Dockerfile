# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY bank-front-ui/pom.xml bank-front-ui/
COPY bank-accounts/pom.xml bank-accounts/
COPY bank-cash/pom.xml bank-cash/
COPY bank-transfer/pom.xml bank-transfer/
COPY bank-notifications/pom.xml bank-notifications/
COPY bank-gateway/pom.xml bank-gateway/
COPY bank-front-ui/src bank-front-ui/src/
COPY bank-accounts/src bank-accounts/src/
COPY bank-cash/src bank-cash/src/
COPY bank-transfer/src bank-transfer/src/
COPY bank-notifications/src bank-notifications/src/
COPY bank-gateway/src bank-gateway/src/
RUN mvn clean package -DskipTests

# --- Runtime stages ---

FROM eclipse-temurin:21-jre-alpine AS bank-front-ui
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /app/bank-front-ui/target/bank-front-ui-*.jar app.jar
EXPOSE 8080
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine AS bank-accounts
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /app/bank-accounts/target/bank-accounts-*.jar app.jar
EXPOSE 8082
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine AS bank-cash
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /app/bank-cash/target/bank-cash-*.jar app.jar
EXPOSE 8083
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine AS bank-transfer
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /app/bank-transfer/target/bank-transfer-*.jar app.jar
EXPOSE 8084
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine AS bank-notifications
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /app/bank-notifications/target/bank-notifications-*.jar app.jar
EXPOSE 8085
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine AS bank-gateway
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /app/bank-gateway/target/bank-gateway-*.jar app.jar
EXPOSE 8081
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
