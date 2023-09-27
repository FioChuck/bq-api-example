mvn package -f "/Users/chasf/bq-api-example/pom.xml";

docker buildx build --platform linux/amd64 -t bq-interface-image .;

docker tag bq-interface-image us-central1-docker.pkg.dev/cf-data-analytics/bq-interface/demo-image;

docker image push us-central1-docker.pkg.dev/cf-data-analytics/bq-interface/demo-image
