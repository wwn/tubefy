ARG JAVA_VERSION=26.0.2_10

FROM eclipse-temurin:${JAVA_VERSION}-jre

ENV JAVA_APP_DIR=/deployments
ENV JAVA_OPTS=""

RUN groupadd -g 185 quarkus \
 && useradd -u 185 -g 185 -m -d /home/quarkus -s /usr/sbin/nologin quarkus

COPY --chown=185:185 build/quarkus-app/lib/ /deployments/lib/
COPY --chown=185:185 build/quarkus-app/*.jar /deployments/
COPY --chown=185:185 build/quarkus-app/app/ /deployments/app/
COPY --chown=185:185 build/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185

ENTRYPOINT [ "sh", "-c", "exec java $JAVA_OPTS -jar /deployments/quarkus-run.jar" ]
