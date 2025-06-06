FROM ubuntu:latest AS build

RUN apt-get update && apt-get install openjdk-17-jdk maven -y

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean install -DskipTests
RUN ls -l /app
RUN ls -l /app/target

# Stage 2: Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

EXPOSE 8080

COPY --from=build /app/target/agenda-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-Dserver.port=8080", "-Dserver.address=0.0.0.0", "-jar", "app.jar"]