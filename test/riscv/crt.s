/*
 * crt.s - C Runtime do simulador
 *
 * Baseado em: https://github.com/ArchC/riscv/blob/9de4f8c3b928c77c3c3c550f8bb97c66d69b31cb/tests/rv_hal/crt.S
 */

.text
.globl _start
.equ memory_size, 0x20000000

_start:
  lui sp, 0x500
  jal main
  lui t0, 0x20000
  jalr t0, 0x0
  ebreak

.bss
.align 8
.skip 4096
kstacktop:
