FROM public.ecr.aws/amazoncorretto/amazoncorretto:24-al2023-jdk
ARG JAR_FILE=build/libs/activity-service-app-build.jar
COPY ${JAR_FILE} /activity-service-app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/activity-service-app.jar"]
