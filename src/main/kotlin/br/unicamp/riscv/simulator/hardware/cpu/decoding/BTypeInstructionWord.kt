package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.BranchCondition
import br.unicamp.riscv.simulator.model.ConditionalBranch
import br.unicamp.riscv.simulator.model.Instruction
import br.unicamp.riscv.simulator.model.Word
import br.unicamp.riscv.simulator.model.bitRange
import br.unicamp.riscv.simulator.model.get

class BTypeInstructionWord(word: Word) : InstructionWord(word) {
    override val imm: Word = (word.bitRange(1..11) shl 1)
        .plus(word.bitRange(25..30) shl 5)
        .plus(word[7].toUInt() shl 11)
        .plus(word[31].toUInt() shl 12)

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

        return ConditionalBranch(condition, rs1, rs2, imm)
    }
}
