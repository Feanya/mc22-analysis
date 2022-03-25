FROM openjdk:17-alpine

COPY ./mc22-analysis-assembly-0.1.0-SNAPSHOT.jar ./

CMD [ "java", "-jar", "./mc22-analysis-assembly-0.1.0-SNAPSHOT.jar" ]
