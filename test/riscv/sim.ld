/*
 * sim.ld - Linker script do simulador
 *
 * Baseado em https://github.com/ArchC/riscv/blob/9de4f8c3b928c77c3c3c550f8bb97c66d69b31cb/tests/rv_hal/test.ld
 */

OUTPUT_ARCH("riscv")

ENTRY(_start)

_RAM_SIZE = 2M;
_RAM_START = 0x0000;
_RAM_END = _RAM_START + _RAM_SIZE;

SECTIONS {
  . = 0x100;
  .text : {
    *(.text)
  }
  
  . = 0x20100;
  .data : { *(.data) }
  .sdata : { *(.sdata) }
  .rodata : { *(.rodata) }
  .sbss : { *(.sbss) }	
  .bss : { *(.bss) }

  _end = .;
}
