FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY common ./common
COPY services ./services

RUN mvn -pl services/processor -am clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/services/processor/target/processor-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
