FROM amazoncorretto:11
LABEL authors="bighaeil"
EXPOSE 8080
ARG JAR_FILE
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
