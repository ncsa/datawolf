# syntax=docker/dockerfile:1.0-experimental
FROM maven:3-jdk-8 AS build

RUN apt-get update && apt-get -y install net-tools

WORKDIR /src

COPY pom.xml datawolf-* gondola* file-* ncsa-* /src/

RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=cache,target=/src/target \
    mvn dependency:resolve
RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=cache,target=/src/target \
    mvn compile
RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=cache,target=/src/target \
    mvn package -Dmaven.test.skip=true
RUN cat /usr/share/maven/ref/settings-docker.xml

FROM openjdk:8-jre-alpine

EXPOSE 8888
VOLUME /home/datawolf/data
WORKDIR /home/datawolf

COPY --from=build /src/datawolf-editor/target/datawolf-editor-4.4.0-SNAPSHOT.war /home/datawolf/lib/
COPY --from=build /src/datawolf-webapp-all/target/datawolf-webapp-all-4.4.0-SNAPSHOT.war /home/datawolf/lib/
COPY --from=build /src/datawolf-webapp-all/target/dependency/jetty-runner.jar /home/datawolf/lib/
COPY --from=build /src/datawolf-webapp-all/src/assembly/bin/datawolf-service /home/datawolf/bin/
COPY --from=build /src/datawolf-webapp-all/src/assembly/conf/* /home/datawolf/conf/

CMD /home/datawolf/bin/datawolf-service
