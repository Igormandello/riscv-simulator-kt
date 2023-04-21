package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.BinaryOp
import br.unicamp.riscv.simulator.model.BinaryOpKind
import br.unicamp.riscv.simulator.model.Instruction
import br.unicamp.riscv.simulator.model.SetIfLessThan
import br.unicamp.riscv.simulator.model.Word

class RTypeInstructionWord(word: Word) : InstructionWord(word) {
    override val imm: Word = 0u

    override fun decode(): Instruction {
        if (funct7 == 0b1u) {
            return decodeRV32MExtensionInstruction()
        }

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

    private fun decodeRV32MExtensionInstruction(): Instruction {
        val kind = when (funct3) {
            0b000u -> BinaryOpKind.MUL
            0b001u -> BinaryOpKind.MULH
            0b010u -> BinaryOpKind.MULHSU
            0b011u -> BinaryOpKind.MULHU
            0b100u -> BinaryOpKind.DIV
            0b101u -> BinaryOpKind.DIVU
            0b110u -> BinaryOpKind.REM
            0b111u -> BinaryOpKind.REMU
            else -> throw IllegalArgumentException("Unknown RV32M Extension function")
        }

        return BinaryOp(kind, rd, rs1, rs2)
    }
}
