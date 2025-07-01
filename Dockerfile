# Multi-stage build para otimizar o tamanho da imagem
FROM maven:3.9.5-openjdk-17 AS build

WORKDIR /app

# Copiar apenas os arquivos de dependências primeiro para aproveitar o cache do Docker
COPY pom.xml .
COPY src ./src

# Baixar dependências e compilar
RUN mvn clean package -DskipTests

# Imagem de produção
FROM openjdk:17-jre-slim

WORKDIR /app

# Criar usuário não-root para segurança
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copiar o JAR compilado
COPY --from=build /app/target/order-service-1.0.0.jar app.jar

# Configurar permissões
RUN chown -R appuser:appuser /app
USER appuser

# Configurar JVM para alta performance
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+OptimizeStringConcat"

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando para executar a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 