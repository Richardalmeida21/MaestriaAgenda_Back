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

# Configurar variáveis de ambiente para o Render
ENV SPRING_PROFILES_ACTIVE=render
ENV JWT_SECRET=Y3i49Jx8nQw3sP@74LkF9dC4mJ1N2PZz
ENV COMISSAO_PERCENTUAL=70.0

# Copia o JAR renomeado
COPY --from=build /app/app.jar app.jar

# Iniciar a aplicação
ENTRYPOINT ["java", "-Dserver.port=8081", "-jar", "app.jar"]