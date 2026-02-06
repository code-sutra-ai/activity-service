FROM eclipse-temurin:24.0.2_12-jre-ubi10-minimal
ARG JAR_FILE=build/libs/activity-service-app-build.jar
COPY ${JAR_FILE} /activity-service-app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/activity-service-app.jar"]
