# Use uma imagem base do Java
FROM openjdk:17-jdk-slim

# Defina o diretório de trabalho no contêiner
WORKDIR /app

# Copie o JAR da sua aplicação para o contêiner
COPY target/agenda-0.0.1-SNAPSHOT.jar /app/agenda.jar

# Exponha a porta onde a aplicação vai rodar
EXPOSE 8080

# Comando para rodar sua aplicação Spring Boot
CMD ["java", "-jar", "agenda.jar"]
