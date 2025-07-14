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

# Instalar ferramentas de rede para diagnóstico
RUN apt-get update && apt-get install -y \
    iputils-ping \
    dnsutils \
    curl \
    netcat \
    && rm -rf /var/lib/apt/lists/*

# Configurar variáveis de ambiente para o Render
ENV SPRING_PROFILES_ACTIVE=render
ENV IS_RENDER=true

# Copia o JAR renomeado
COPY --from=build /app/app.jar app.jar

# Script para resolver problemas de DNS no Render antes de iniciar a aplicação
RUN echo '#!/bin/sh' > /app/start.sh && \
    echo 'echo "Verificando resolução DNS para o host do banco de dados..."' >> /app/start.sh && \
    echo 'host db.kgcajiuuvcgkggbhtudi.supabase.co || echo "Falha na resolução DNS"' >> /app/start.sh && \
    echo 'echo "Configurando nameservers alternativos..."' >> /app/start.sh && \
    echo 'echo "nameserver 8.8.8.8" > /etc/resolv.conf' >> /app/start.sh && \
    echo 'echo "nameserver 1.1.1.1" >> /etc/resolv.conf' >> /app/start.sh && \
    echo 'echo "Verificando novamente resolução DNS..."' >> /app/start.sh && \
    echo 'host db.kgcajiuuvcgkggbhtudi.supabase.co || echo "Ainda com falha na resolução DNS"' >> /app/start.sh && \
    echo 'echo "Iniciando a aplicação..."' >> /app/start.sh && \
    echo 'java -Dserver.port=8081 -Dspring.profiles.active=render -jar app.jar' >> /app/start.sh && \
    chmod +x /app/start.sh

# Usar o script de inicialização
CMD ["/app/start.sh"]