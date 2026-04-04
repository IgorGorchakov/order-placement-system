FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY common-events/pom.xml common-events/
COPY user-service/pom.xml user-service/
COPY search-service/pom.xml search-service/
COPY booking-manager/pom.xml booking-manager/
COPY payment-service/pom.xml payment-service/
COPY fulfillment-service/pom.xml fulfillment-service/
COPY integration-tests/pom.xml integration-tests/
RUN mvn dependency:go-offline -B || true

COPY common-events/ common-events/
COPY user-service/ user-service/
COPY search-service/ search-service/
COPY booking-manager/ booking-manager/
COPY payment-service/ payment-service/
COPY fulfillment-service/ fulfillment-service/
COPY integration-tests/ integration-tests/
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre AS user-service
WORKDIR /app
COPY --from=build /app/user-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:17-jre AS search-service
WORKDIR /app
COPY --from=build /app/search-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:17-jre AS booking-manager
WORKDIR /app
COPY --from=build /app/booking-manager/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:17-jre AS payment-service
WORKDIR /app
COPY --from=build /app/payment-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:17-jre AS fulfillment-service
WORKDIR /app
COPY --from=build /app/fulfillment-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
