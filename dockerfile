# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk-jammy
COPY build/libs/mosaikapp-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]