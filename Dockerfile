# Multi-stage Dockerfile for Activity Service
# Build stage (requires JDK 24)
FROM amazoncorretto:24-alpine AS build
ARG JAR_FILE=build/libs/activity-service-app-build.jar
COPY ${JAR_FILE} /activity-service-app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/activity-service-app.jar"]