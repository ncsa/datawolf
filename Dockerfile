FROM java:8

EXPOSE 8888
WORKDIR /home/datawolf

COPY datawolf-editor/target/datawolf-editor-4.4.0-SNAPSHOT.war /home/datawolf/lib/
COPY datawolf-webapp-all/target/datawolf-webapp-all-4.4.0-SNAPSHOT.war /home/datawolf/lib/
COPY datawolf-webapp-all/target/dependency/jetty-runner.jar /home/datawolf/lib/
COPY datawolf-webapp-all/src/assembly/bin/datawolf-service datawolf-service /home/datawolf/bin/
COPY datawolf-webapp-all/src/assembly/conf/* /home/datawolf/conf/

CMD /home/datawolf/bin/datawolf-service



