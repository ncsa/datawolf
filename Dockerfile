FROM maven:3-jdk-8 AS build

RUN apt-get update && apt-get -y install net-tools

WORKDIR /src

COPY . /src/

RUN mvn clean package -Dmaven.test.skip=true

FROM adoptopenjdk/openjdk8:alpine-jre

ENV DATAWOLF_ADMINS=admin@example.com \
    DB_CLASS_NAME="org.postgresql.ds.PGSimpleDataSource" \
    DB_DIALECT="org.hibernate.dialect.PostgreSQL9Dialect" \
    DB_SOURCE_URL="jdbc:postgresql://postgres/datawolf" \
    DB_MAX_POOLSIZE=100 \
    DB_IDLE_TIMEOUT=30000 \
    DB_USER=datawolf \
    DB_PASSWORD=datawolf \
    KUBERNETES_NAMESPACE="datawolf" \
    KUBERNETES_PVC="datawolf" \
    KUBERNETES_DATA="/data" \
    KUBERNETES_CPU=2 \
    KUBERNETES_MEMORY=4 \
    KUBERNETES_WORKER_NODE_AFFINITY_REQUIRED=false \
    DATASET_PERMISSIONS=private

EXPOSE 8888
VOLUME /home/datawolf/data
WORKDIR /home/datawolf

COPY --from=build /src/datawolf-editor/target/datawolf-editor-*.war /home/datawolf/lib/
COPY --from=build /src/datawolf-webapp-all/target/datawolf-webapp-all-*.war /home/datawolf/lib/
COPY --from=build /src/datawolf-webapp-all/target/dependency/jetty-runner.jar /home/datawolf/lib/
COPY --from=build /src/datawolf-webapp-all/src/assembly/bin/datawolf-service /home/datawolf/bin/
COPY --from=build /src/datawolf-webapp-all/src/assembly/conf/* /home/datawolf/conf/
COPY docker/custom.properties /home/datawolf/conf

CMD /home/datawolf/bin/datawolf-service
