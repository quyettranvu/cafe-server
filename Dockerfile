FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar server.jar
ENTRYPOINT ["java","-jar","/server.jar"]
EXPOSE 8080