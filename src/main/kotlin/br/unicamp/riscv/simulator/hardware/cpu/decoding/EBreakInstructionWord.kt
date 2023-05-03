package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.EBreak
import br.unicamp.riscv.simulator.model.Instruction

object EBreakInstructionWord : InstructionWord(0b00000000000100000000000001110011u) {
    override fun decode(): Instruction = EBreak
}
