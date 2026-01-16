FROM registry.access.redhat.com/ubi9/openjdk-25:latest
ENV JAVA_APP_DIR=/deployments
COPY --chown=185 build/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 build/quarkus-app/*.jar /deployments/
COPY --chown=185 build/quarkus-app/app/ /deployments/app/
COPY --chown=185 build/quarkus-app/quarkus/ /deployments/quarkus/
EXPOSE 8080
USER 185
ENTRYPOINT [ "java", "-jar", "/deployments/quarkus-run.jar" ]