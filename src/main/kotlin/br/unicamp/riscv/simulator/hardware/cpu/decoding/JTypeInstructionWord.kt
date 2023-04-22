package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.*

class JTypeInstructionWord(word: Word) : InstructionWord(word) {
    private val imm20: Word = word
        .patch(21..30, 20..20, 12..19, 31..31)
        .signExtend(20) shl 1

    override fun decode(): Instruction = JumpAndLink(rd, imm20)
}
