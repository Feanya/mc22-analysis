FROM hseeberger/scala-sbt:17.0.2_1.6.2_2.12.15

COPY . /app
WORKDIR /app

ENTRYPOINT ["bash", "run.sh"]
