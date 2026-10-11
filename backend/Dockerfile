FROM eclipse-temurin:26-jdk-noble AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw -q dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:26-jre-noble
RUN apt-get update && apt-get install -y --no-install-recommends \
        libreoffice-impress \
        fonts-liberation fonts-dejavu \
        fonts-crosextra-carlito fonts-crosextra-caladea \
    && rm -rf /var/lib/apt/lists/*

ENV LIBREOFFICE_PATH=soffice
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]