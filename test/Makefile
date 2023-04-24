CC		= riscv32-unknown-elf-gcc
OBJCOPY	= riscv32-unknown-elf-objcopy

ELF_SUFFIX = .riscv
BIN_SUFFIX = .bin

ECHO 	= /bin/echo

CFLAGS 		= -g -march=rv32im -std=gnu99 -mabi=ilp32
LINK_OPTS 	= -nostartfiles

SIMULATOR =

BIN_FILES = $(patsubst %.c,%$(BIN_SUFFIX),$(wildcard *.c))

# Compile programs
.PRECIOUS: %$(ELF_SUFFIX)
%$(ELF_SUFFIX): %.c
	$(CC) $(CFLAGS) riscv/crt.s $< -o $@ $(LIB_DIR) $(LIBS) $(CFLAGS) -T riscv/sim.ld $(LINK_OPTS)

%$(BIN_SUFFIX): %$(ELF_SUFFIX)
	$(OBJCOPY) -O binary $< $@

build: $(BIN_FILES)

run: build
	$(SIMULATOR) "*.bin"

clean:
	rm -f *~
	rm -f *.cmd
	rm -f *.out
	rm -f *.bin
	rm -f *.log
	rm -f *.riscv

.PHONY: build clean
