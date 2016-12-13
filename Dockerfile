FROM java:8

RUN mkdir -p /opt/lite-country-service

ENV JAR_FILE lite-country-service-1.1.jar
ENV SERVICE_DIR /opt/lite-country-service
ENV CONFIG_FILE /conf/country-service-config.yaml

COPY build/libs/$JAR_FILE $SERVICE_DIR

WORKDIR $SERVICE_DIR

CMD java "-jar" $JAR_FILE "server" $CONFIG_FILE
