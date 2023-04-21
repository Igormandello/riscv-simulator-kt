package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.Instruction
import br.unicamp.riscv.simulator.model.JumpAndLink
import br.unicamp.riscv.simulator.model.Word
import br.unicamp.riscv.simulator.model.bitRange
import br.unicamp.riscv.simulator.model.get

class JTypeInstructionWord(word: Word) : InstructionWord(word) {
    override val imm: Word = (word.bitRange(21..30) shl 1)
        .plus(word[20].toUInt() shl 11)
        .plus(word.bitRange(12..19) shl 12)
        .plus(word[31].toUInt() shl 20)

    override fun decode(): Instruction = JumpAndLink(rd, imm)
}
