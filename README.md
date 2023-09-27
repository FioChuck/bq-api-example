# Overview

A simple Java Cloud Run app that implements an HTTP server using [com.sun.net.httpserver.HttpServer
](https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html) Class. The HTTP handler acts as webhook interface for executing BigQuery jobs and analyzing performance.

The BigQuery Client Library _(Java)_ is used to simplify interfacing with the underlying APIs. Example client library code be found [here](https://cloud.google.com/bigquery/docs/reference/libraries#client-libraries-install-java).

# Build

## Maven

This project relies on Maven for dependency management and distribution.

The Java application is packaged as a far jar distribution using the [Apache Maven Assembly Plugin](https://maven.apache.org/plugins/maven-assembly-plugin/). You can find this plugin definition in the POM as as shown below.

```xml
<plugin>
    <artifactId>maven-assembly-plugin</artifactId>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <archive>
            <manifest>
                <addClasspath>true</addClasspath>
                <mainClass>chasf.Main</mainClass>
            </manifest>
        </archive>
        <descriptorRefs>
            <descriptorRef>jar-with-dependencies</descriptorRef>
        </descriptorRefs>
    </configuration>
</plugin>
```

Additional examples of this plugin/execution can be found [here](https://maven.apache.org/plugins/maven-assembly-plugin/usage.html#execution-building-an-assembly). Make sure to configure the main class attribute of the JAR manifest.

## Docker Image

The uber jar _(packaged using the Maven Assembly Plugin)_ is then containerized into a Docker image using Dockerfile definition. Notice the Maven package phase is orchestrated in the Dockerfile as shown below.

```Docker
RUN mvn package -DskipTests
```

# Deploy

## Cloud Run / Cloud Build

Cloud Build is used to automate the build and deployment of the Docker image to Cloud Run using a Cloud Build Trigger. More info on this process can be found [here](https://cloud.google.com/run/docs/continuous-deployment-with-cloud-build). Essentially Cloud Build authenticates with Github and executes the `Dockerfile`. This is the simplest method for building and deploying docker images to Cloud Run. You can also use Git Actions to accomplish the as shown [here](https://github.com/google-github-actions/deploy-cloudrun).

# BigQuery Interface

The main purpose of this project was to test the Java Client Library for BigQuery. I was interested in returning Query stats such as Slot MS consumed and Query execution time.

The [JobStatistics.QueryStatistics](https://cloud.google.com/java/docs/reference/google-cloud-bigquery/latest/com.google.cloud.bigquery.JobStatistics.QueryStatistics) Class has several interesting methods for analyzing query performance like:

[getTotalSlotMS](https://cloud.google.com/java/docs/reference/google-cloud-bigquery/latest/com.google.cloud.bigquery.JobStatistics.QueryStatistics#com_google_cloud_bigquery_JobStatistics_QueryStatistics_getTotalSlotMs__): Returns the slot-milliseconds consumed by the query.

[getTotalBytesProcessed](https://cloud.google.com/java/docs/reference/google-cloud-bigquery/latest/com.google.cloud.bigquery.JobStatistics.QueryStatistics#com_google_cloud_bigquery_JobStatistics_QueryStatistics_getTotalBytesProcessed__): Returns the total number of bytes processed by the job.

The JobStatistics.QueryStatistics is an inherited class.
