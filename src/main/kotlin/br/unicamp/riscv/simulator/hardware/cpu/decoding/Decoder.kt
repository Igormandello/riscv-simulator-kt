package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.AddUpperImmediateToPC
import br.unicamp.riscv.simulator.model.BinaryOp
import br.unicamp.riscv.simulator.model.BinaryOpImmediate
import br.unicamp.riscv.simulator.model.BinaryOpKind
import br.unicamp.riscv.simulator.model.BranchCondition
import br.unicamp.riscv.simulator.model.ConditionalBranch
import br.unicamp.riscv.simulator.model.Instruction
import br.unicamp.riscv.simulator.model.JumpAndLink
import br.unicamp.riscv.simulator.model.JumpAndLinkRegister
import br.unicamp.riscv.simulator.model.Load
import br.unicamp.riscv.simulator.model.LoadKind
import br.unicamp.riscv.simulator.model.LoadUpperImmediate
import br.unicamp.riscv.simulator.model.SetIfLessThan
import br.unicamp.riscv.simulator.model.SetIfLessThanImmediate
import br.unicamp.riscv.simulator.model.Store
import br.unicamp.riscv.simulator.model.StoreKind
import br.unicamp.riscv.simulator.model.Word
import br.unicamp.riscv.simulator.model.XRegister
import br.unicamp.riscv.simulator.model.bitRange
import br.unicamp.riscv.simulator.model.get

class Decoder {
    fun decodeInstruction(word: Word): Instruction {
        return when {
            word.isRType -> word.decodeRType()
            word.isIType -> word.decodeIType()
            word.isSType -> word.decodeSType()
            word.isBType -> word.decodeBType()
            word.isUType -> word.decodeUType()
            word.isJType -> word.decodeJType()
            else -> throw IllegalArgumentException("Unknown instruction type")
        }
    }

    private fun Word.decodeRType(): Instruction {
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

    private fun Word.decodeIType(): Instruction = when (opcode) {
        JALR_OPCODE -> JumpAndLinkRegister(rd, rs1, imm)
        LOAD_OPCODE -> Load(loadKind, rd, rs1, imm)
        ARITHMETIC_OP_IMMEDIATE_OPCODE -> decodeArithmeticOpWithImmediate()
        else -> throw IllegalArgumentException("Unknown I Type opcode")
    }

    private fun Word.decodeSType(): Instruction {
        val kind = when (funct3) {
            0b000u -> StoreKind.B
            0b001u -> StoreKind.H
            0b010u -> StoreKind.W
            else -> throw IllegalArgumentException("Unknown S Type function")
        }

        return Store(kind, rs1, rs1, imm)
    }

    private fun Word.decodeBType(): Instruction {
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

    private fun Word.decodeUType(): Instruction = when (opcode) {
        LOAD_UPPER_IMMEDIATE_OPCODE -> LoadUpperImmediate(rd, imm)
        ADD_UPPER_IMMEDIATE_TO_PC_OPCODE -> AddUpperImmediateToPC(rd, imm)
        else -> throw IllegalArgumentException("Unknown U Type opcode")
    }

    private fun Word.decodeJType(): Instruction = JumpAndLink(rd, imm)

    private fun Word.decodeArithmeticOpWithImmediate(): Instruction {
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

    private val Word.isRType: Boolean get() = opcode in R_TYPE_OPCODES
    private val Word.isIType: Boolean get() = opcode in I_TYPE_OPCODES
    private val Word.isSType: Boolean get() = opcode in S_TYPE_OPCODES
    private val Word.isBType: Boolean get() = opcode in B_TYPE_OPCODES
    private val Word.isUType: Boolean get() = opcode in U_TYPE_OPCODES
    private val Word.isJType: Boolean get() = opcode in J_TYPE_OPCODES

    private val Word.opcode: Word get() = this.bitRange(0..6)
    private val Word.rdId: Word get() = this.bitRange(7..11)
    private val Word.rd: XRegister get() = XRegister(this.rdId.toInt())
    private val Word.rs1Id: Word get() = this.bitRange(15..19)
    private val Word.rs1: XRegister get() = XRegister(rs1Id.toInt())
    private val Word.rs2Id: Word get() = this.bitRange(20..24)
    private val Word.rs2: XRegister get() = XRegister(rs2Id.toInt())

    private val Word.funct3: Word get() = this.bitRange(12..14)
    private val Word.funct7: Word get() = this.bitRange(25..31)

    private val Word.shamt: Word get() = rs2Id
    private val Word.imm: Word
        get() = when {
            isIType -> this.bitRange(20..31)
            isSType -> this.rdId + (funct7 shl 5)
            isBType -> {
                (this.bitRange(1..11) shl 1)
                    .plus(this.bitRange(25..30) shl 5)
                    .plus(this[7].toUInt() shl 11)
                    .plus(this[31].toUInt() shl 12)
            }
            isUType -> this.bitRange(12..31) shl 12
            isJType -> {
                (this.bitRange(21..30) shl 1)
                    .plus(this[20].toUInt() shl 11)
                    .plus(this.bitRange(12..19) shl 12)
                    .plus(this[31].toUInt() shl 20)
            }
            else -> throw IllegalArgumentException("Unknown instruction type")
        }


    companion object {
        private val BINARY_OP_OPCODE = 0b0110011u

        private val JALR_OPCODE = 0b1100111u
        private val LOAD_OPCODE = 0b0010011u
        private val ARITHMETIC_OP_IMMEDIATE_OPCODE = 0b0000011u

        private val STORE_OPCODE = 0b0100011u

        private val BRANCH_OPCODE = 0b1100011u

        private val LOAD_UPPER_IMMEDIATE_OPCODE = 0b0110111u
        private val ADD_UPPER_IMMEDIATE_TO_PC_OPCODE = 0b0010111u

        private val JAL_OPCODE = 0b1101111u

        private val R_TYPE_OPCODES = setOf(BINARY_OP_OPCODE)
        private val I_TYPE_OPCODES = setOf(JALR_OPCODE, LOAD_OPCODE, ARITHMETIC_OP_IMMEDIATE_OPCODE)
        private val S_TYPE_OPCODES = setOf(STORE_OPCODE)
        private val B_TYPE_OPCODES = setOf(BRANCH_OPCODE)
        private val U_TYPE_OPCODES = setOf(LOAD_UPPER_IMMEDIATE_OPCODE, ADD_UPPER_IMMEDIATE_TO_PC_OPCODE)
        private val J_TYPE_OPCODES = setOf(JAL_OPCODE)
    }
}