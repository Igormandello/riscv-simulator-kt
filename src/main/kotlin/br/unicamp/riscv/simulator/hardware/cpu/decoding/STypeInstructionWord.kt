package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.*

class STypeInstructionWord(word: Word) : InstructionWord(word) {
    private val imm12: Word = word.patch(7..11, 25..31).signExtend(12)

    override fun decode(): Instruction {
        val kind = when (funct3) {
            0b000u -> StoreKind.B
            0b001u -> StoreKind.H
            0b010u -> StoreKind.W
            else -> throw IllegalArgumentException("Unknown S Type function")
        }

        return Store(kind, rs1, rs2, imm12)
    }
}
