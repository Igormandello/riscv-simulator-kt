package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.BinaryOp
import br.unicamp.riscv.simulator.model.BinaryOpKind
import br.unicamp.riscv.simulator.model.Instruction
import br.unicamp.riscv.simulator.model.SetIfLessThan
import br.unicamp.riscv.simulator.model.Word

class RTypeInstructionWord(word: Word) : InstructionWord(word) {
    override val imm: Word = 0u

    override fun decode(): Instruction {
        when (funct3) {
            0b010u, 0b011u -> return SetIfLessThan(funct3 == 0b010u, rd, rs1, rs2)
        }

        val kind = when (funct3) {
            0b000u -> if (funct7 == 0u) BinaryOpKind.ADD else BinaryOpKind.SUB
            0b001u -> BinaryOpKind.SLL
            0b100u -> BinaryOpKind.XOR
            0b101u -> if (funct7 == 0u) BinaryOpKind.SRL else BinaryOpKind.SRA
            0b110u -> BinaryOpKind.OR
            0b111u -> BinaryOpKind.AND
            else -> throw IllegalArgumentException("Unknown R Type function")
        }

        return BinaryOp(kind, rd, rs1, rs2)
    }
}
