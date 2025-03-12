# Usa a imagem do OpenJDK 17
FROM openjdk:17-jdk-slim

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o arquivo JAR gerado pelo Maven/Gradle para dentro do contêiner
COPY target/*.jar app.jar

# Expõe a porta 8080 (ou a porta que sua API usa)
EXPOSE 8080

# Comando para rodar a API
CMD ["java", "-jar", "app.jar"]
