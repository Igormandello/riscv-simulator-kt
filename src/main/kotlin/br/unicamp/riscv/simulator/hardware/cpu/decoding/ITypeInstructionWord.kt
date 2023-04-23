package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.*

class ITypeInstructionWord(word: Word) : InstructionWord(word) {
    private val imm12: Word = word.bitRange(20..31)
    private val imm12S: Word get() = imm12.signExtend(12)

    override fun decode(): Instruction =
        when (opcode) {
            JALR_OPCODE -> JumpAndLinkRegister(rd, rs1, imm12S)
            LOAD_OPCODE -> {
                val kind = when (funct3) {
                    0b000u -> LoadKind.B
                    0b001u -> LoadKind.H
                    0b010u -> LoadKind.W
                    0b100u -> LoadKind.BU
                    0b101u -> LoadKind.HU
                    else -> throw IllegalArgumentException("Unknown function type")
                }
                Load(kind, rd, rs1, imm12S)
            }
            ARITHMETIC_OP_IMMEDIATE_OPCODE -> decodeArithmeticOpWithImmediate()
            else -> throw IllegalArgumentException("Unknown I Type opcode")
        }

    private fun decodeArithmeticOpWithImmediate(): Instruction =
        when (funct3) {
            0b010u -> SetIfLessThanImmediate(true, rd, rs1, imm12S)
            0b011u -> SetIfLessThanImmediate(false, rd, rs1, imm12)
            0b001u ->
                if (funct7 == 0u) {
                    BinaryOpImmediate(BinaryOpKind.SLL, rd, rs1, shamt)
                } else {
                    throw IllegalArgumentException("Unknown binary shift op function")
                }
            0b101u -> {
                val kind = when (funct7) {
                    0u -> BinaryOpKind.SRL
                    32u -> BinaryOpKind.SRA
                    else -> throw IllegalArgumentException("Unknown binary shift op function")
                }
                BinaryOpImmediate(kind, rd, rs1, shamt)
            }
            else -> {
                val kind = when (funct3) {
                    0b000u -> BinaryOpKind.ADD
                    0b100u -> BinaryOpKind.XOR
                    0b110u -> BinaryOpKind.OR
                    0b111u -> BinaryOpKind.AND
                    else -> throw IllegalArgumentException("Unknown arithmetic op function")
                }
                BinaryOpImmediate(kind, rd, rs1, imm12S)
            }
        }
}
