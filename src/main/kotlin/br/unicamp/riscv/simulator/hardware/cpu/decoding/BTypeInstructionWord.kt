package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.*

class BTypeInstructionWord(word: Word) : InstructionWord(word) {
    private val imm12: Word = word
        .patch(8..11, 25..30, 7..7, 31..31)
        .signExtend(12) shl 1

    override fun decode(): Instruction {
        val condition = when (funct3) {
            0b000u -> BranchCondition.EQ
            0b001u -> BranchCondition.NE
            0b100u -> BranchCondition.LT
            0b101u -> BranchCondition.GE
            0b110u -> BranchCondition.LTU
            0b111u -> BranchCondition.GEU
            else -> throw IllegalArgumentException("Unknown B Type function")
        }

        return ConditionalBranch(condition, rs1, rs2, imm12)
    }
}
