FROM openjdk:8-jdk-alpine
RUN addgroup -S dgroup && adduser -S duser -G duser
USER dgroup:duser
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}a/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","worldmap.Application"]