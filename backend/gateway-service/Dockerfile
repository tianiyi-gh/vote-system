FROM openjdk:17-jre-slim

WORKDIR /app

COPY target/gateway-service-*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]