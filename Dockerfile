FROM maven:3-jdk-11-slim AS build-env

WORKDIR /app
# Copy the pom.xml file to download dependencies
COPY pom.xml ./
# Copy local code to the container image.
COPY src ./src

# Download dependencies and build a release artifact.
RUN mvn package -DskipTests

FROM openjdk:20

COPY target/bq-api-example-1.0-SNAPSHOT-jar-with-dependencies.jar /

ENV PORT 8080

CMD java -jar bq-api-example-1.0-SNAPSHOT-jar-with-dependencies.jar