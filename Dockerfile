# Multi-stage Dockerfile for Activity Service
# Build stage (requires JDK 24)
FROM eclipse-temurin:24-jdk AS build
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} activity-service-app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/activity-service-app.jar"]