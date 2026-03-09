# Dockerfile para aplicação Spring Boot com H2
FROM openjdk:21-jdk-slim

# Definir diretório de trabalho
WORKDIR /app

# Copiar o JAR da aplicação
COPY target/*.jar app.jar

# Expor porta 8080
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
