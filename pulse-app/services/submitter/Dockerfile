FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY common ./common
COPY services ./services

RUN mvn -pl services/submitter -am clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* \
    && addgroup --system --gid 1001 appgroup && adduser --system --uid 1001 --ingroup appgroup appuser
WORKDIR /app
COPY --from=build /app/services/submitter/target/submitter-*.jar app.jar
RUN chown appuser:appgroup app.jar
USER 1001

EXPOSE 8080
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
