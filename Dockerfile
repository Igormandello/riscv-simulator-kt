FROM ghcr.io/guibrandt/riscv-gnu-toolchain:rv32im-ilp32
WORKDIR /mnt/test

RUN apt-get update && apt-get install -y make libmpc3 && rm -rf /var/lib/apt/lists/*

ENTRYPOINT [ "make", "clean", "build", "CC=riscv64-unknown-elf-gcc", "OBJCOPY=riscv64-unknown-elf-objcopy" ]
