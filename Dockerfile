FROM ubuntu:latest AS build

RUN apt-get update && apt-get install openjdk-17-jdk maven -y

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests
RUN find /app/target -name "*.jar" -not -name "*sources.jar" -not -name "*javadoc.jar" -not -name "*tests.jar" -exec cp {} /app/app.jar \;

# Stage 2: Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

EXPOSE 8081

# Copia o JAR renomeado
COPY --from=build /app/app.jar app.jar

# Definir variáveis de ambiente para a conexão com o banco de dados
ENV JDBC_DATABASE_URL=jdbc:postgresql://dpg-d1132295pdvs73ei4pqg-a.oregon-postgres.render.com/maestriabd
ENV JDBC_DATABASE_USERNAME=maestriabd_user
ENV JDBC_DATABASE_PASSWORD=7FqwA1lUdLXrsdQLUGS7y3hO80aAsuvn

ENTRYPOINT ["java", "-Dserver.port=8081", "-jar", "app.jar"]