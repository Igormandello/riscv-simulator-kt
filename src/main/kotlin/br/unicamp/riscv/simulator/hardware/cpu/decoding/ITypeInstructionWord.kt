package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.BinaryOpImmediate
import br.unicamp.riscv.simulator.model.BinaryOpKind
import br.unicamp.riscv.simulator.model.Instruction
import br.unicamp.riscv.simulator.model.JumpAndLinkRegister
import br.unicamp.riscv.simulator.model.Load
import br.unicamp.riscv.simulator.model.LoadKind
import br.unicamp.riscv.simulator.model.SetIfLessThanImmediate
import br.unicamp.riscv.simulator.model.Word
import br.unicamp.riscv.simulator.model.bitRange

class ITypeInstructionWord(private val word: Word) : InstructionWord(word) {
    override val imm: Word = word.bitRange(20..31)

    override fun decode(): Instruction = when (opcode) {
        JALR_OPCODE -> JumpAndLinkRegister(rd, rs1, imm)
        LOAD_OPCODE -> Load(word.loadKind, rd, rs1, imm)
        ARITHMETIC_OP_IMMEDIATE_OPCODE -> decodeArithmeticOpWithImmediate()
        else -> throw IllegalArgumentException("Unknown I Type opcode")
    }

    private fun decodeArithmeticOpWithImmediate(): Instruction {
        when (funct3) {
            0b010u, 0b011u -> return SetIfLessThanImmediate(funct3 == 0b010u, rd, rs1, imm)
            0b001u -> return BinaryOpImmediate(BinaryOpKind.SLL, rd, rs1, shamt)
            0b101u -> {
                val kind = if (funct7 == 0u) BinaryOpKind.SRL else BinaryOpKind.SRA
                return BinaryOpImmediate(kind, rd, rs1, shamt)
            }
        }

        val kind = when (funct3) {
            0b000u -> BinaryOpKind.ADD
            0b100u -> BinaryOpKind.XOR
            0b110u -> BinaryOpKind.OR
            0b111u -> BinaryOpKind.AND
            else -> throw IllegalArgumentException("Unknown arithmetic op function")
        }

        return BinaryOpImmediate(kind, rd, rs1, imm)
    }

    private val Word.loadKind: LoadKind
        get() = when (funct3) {
            0b000u -> LoadKind.B
            0b001u -> LoadKind.H
            0b010u -> LoadKind.W
            0b100u -> LoadKind.BU
            0b101u -> LoadKind.HU
            else -> throw IllegalArgumentException("Unknown function type")
        }
}