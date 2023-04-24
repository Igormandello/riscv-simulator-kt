FROM ghcr.io/guibrandt/riscv-gnu-toolchain:rv32im-ilp32 as toolchain

FROM gradle:7.6.1-jdk17-alpine as builder
WORKDIR /build
ADD build.gradle.kts gradle.properties settings.gradle.kts ./
ENV JAVA_OPTS="-Xmx512m -XX:MaxMetaspaceSize=256m"
ENV TERM=dumb
RUN gradle --no-daemon dependencies
ADD src/ ./src/
RUN gradle --no-daemon installDist

FROM openjdk:17-bullseye

COPY --from=toolchain /opt/riscv /opt/riscv
ENV PATH="$PATH:/opt/riscv/bin"
RUN apt-get update && apt-get install -y make libmpc3 && rm -rf /var/lib/apt/lists/*

COPY --from=builder /build/build/install/riscv-simulator/ /app/
ENV PATH="$PATH:/app/bin/"

WORKDIR /mnt/test
ENTRYPOINT ["make", "-j", "32", "run", \
    "CC=riscv64-unknown-elf-gcc", \
    "OBJCOPY=riscv64-unknown-elf-objcopy", \
    "SIMULATOR=riscv-simulator"]
