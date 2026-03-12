# Dockerfile para aplicação Spring Boot com H2
FROM eclipse-temurin:21-jdk-jammy

# Definir diretório de trabalho
WORKDIR /app

# Criar diretório para dados do banco de dados
RUN mkdir -p /app/data

# Copiar o JAR da aplicação
COPY target/*.jar app.jar

# Expor porta 8080
EXPOSE 8080

# Variáveis de ambiente
ENV SPRING_PROFILES_ACTIVE=docker
ENV SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/shortenerdb;MODE=MySQL;AUTO_SERVER=true

# Volume para persistência de dados
VOLUME ["/app/data"]

# Comando para executar a aplicação com timeout maior
ENTRYPOINT ["java", "-jar", "app.jar"]
