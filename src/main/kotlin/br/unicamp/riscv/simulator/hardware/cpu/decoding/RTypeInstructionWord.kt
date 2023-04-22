package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.BinaryOp
import br.unicamp.riscv.simulator.model.BinaryOpKind
import br.unicamp.riscv.simulator.model.Instruction
import br.unicamp.riscv.simulator.model.SetIfLessThan
import br.unicamp.riscv.simulator.model.Word

class RTypeInstructionWord(word: Word) : InstructionWord(word) {
    override fun decode(): Instruction {
        if (funct7 == 0b1u) {
            return decodeRV32MExtensionInstruction()
        }

        when (funct10) {
            0b0000000010u, 0b0000000011u -> return SetIfLessThan(funct3 == 0b010u, rd, rs1, rs2)
        }

        val kind = when (funct10) {
            0b0000000000u -> BinaryOpKind.ADD
            0b0100000000u -> BinaryOpKind.SUB
            0b0000000001u -> BinaryOpKind.SLL
            0b0000000100u -> BinaryOpKind.XOR
            0b0000000101u -> BinaryOpKind.SRL
            0b0100000101u -> BinaryOpKind.SRA
            0b0000000110u -> BinaryOpKind.OR
            0b0000000111u -> BinaryOpKind.AND
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
