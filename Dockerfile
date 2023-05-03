FROM gradle:7.6.1-jdk17-alpine as builder
WORKDIR /build
ADD build.gradle.kts gradle.properties settings.gradle.kts ./
ENV JAVA_OPTS="-Xmx512m -XX:MaxMetaspaceSize=256m"
ENV TERM=dumb
RUN gradle --no-daemon dependencies
ADD src/ ./src/
RUN gradle --no-daemon installDist

FROM openjdk:17-bullseye

COPY --from=builder /build/build/install/riscv-simulator/ /app/
ENV PATH="$PATH:/app/bin/"

ENV LOG_LEVEL=INFO

WORKDIR /mnt/test
ENTRYPOINT ["riscv-simulator"]
