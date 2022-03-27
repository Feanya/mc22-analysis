FROM openjdk:17-alpine

COPY ./mc22-analysis-assembly-0.1.0-SNAPSHOT.jar ./
COPY ./run.sh ./

CMD [ "sh", "./run.sh" ]
