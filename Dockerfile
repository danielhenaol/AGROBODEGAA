FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY config-server/pom.xml ./pom.xml
COPY config-server/src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/agrobodega-config-server-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]