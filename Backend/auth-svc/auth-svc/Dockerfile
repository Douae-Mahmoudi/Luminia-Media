FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

# Copie le JAR généré par Maven
COPY target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]