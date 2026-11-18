# La receta de la imagen de la API. Tiene dos partes:

# Parte 1: compilar. Trae Maven y el JDK, y solo sirve para fabricar el .jar.
FROM maven:3.9-eclipse-temurin-21-alpine AS compilar
WORKDIR /app
# Primero el pom.xml solo: si el codigo cambia y el pom no, Docker reutiliza las dependencias ya bajadas.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
# Las pruebas no se corren aqui: necesitarian una base, y dentro de esta parte no hay ninguna.
RUN mvn -B -q package -Dmaven.test.skip=true

# Parte 2: correr. Solo Java y el .jar. Maven y el codigo fuente se quedan afuera.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=compilar /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]