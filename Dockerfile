FROM openjdk:24
ARG JAR_FILE=build/libs/activity-service-app-build.jar
COPY ${JAR_FILE} /activity-service-app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/activity-service-app.jar"]