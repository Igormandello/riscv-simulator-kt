package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.Instruction
import br.unicamp.riscv.simulator.model.Word
import br.unicamp.riscv.simulator.model.XRegister
import br.unicamp.riscv.simulator.model.bitRange

abstract class InstructionWord(word: Word) {
    protected val opcode: Word = word.bitRange(0..6)

    private val rdId: Word = word.bitRange(7..11)
    val rd: XRegister = XRegister(this.rdId.toInt())

    private val rs1Id: Word = word.bitRange(15..19)
    val rs1: XRegister = XRegister(rs1Id.toInt())

    private val rs2Id: Word = word.bitRange(20..24)
    val rs2: XRegister = XRegister(rs2Id.toInt())

    protected val funct3: Word = word.bitRange(12..14)
    protected val funct7: Word = word.bitRange(25..31)
    protected val funct10: Word = funct3 or (funct7 shl 3)

    protected val shamt: Word = rs2Id

    abstract fun decode(): Instruction

    companion object {
        fun createInstructionWord(word: Word): InstructionWord = when (val opcode = word.bitRange(0..6)) {
            in R_TYPE_OPCODES -> RTypeInstructionWord(word)
            in I_TYPE_OPCODES -> ITypeInstructionWord(word)
            in S_TYPE_OPCODES -> STypeInstructionWord(word)
            in B_TYPE_OPCODES -> BTypeInstructionWord(word)
            in U_TYPE_OPCODES -> UTypeInstructionWord(word)
            in J_TYPE_OPCODES -> JTypeInstructionWord(word)
            else -> throw IllegalArgumentException("Unknown instruction type (0b${opcode.toString(2)})")
        }

        private val BINARY_OP_OPCODE = 0b0110011u
        private val STORE_OPCODE = 0b0100011u
        private val BRANCH_OPCODE = 0b1100011u
        private val JAL_OPCODE = 0b1101111u

        @JvmStatic
        protected val JALR_OPCODE = 0b1100111u

        @JvmStatic
        protected val LOAD_OPCODE = 0b0000011u

        @JvmStatic
        protected val ARITHMETIC_OP_IMMEDIATE_OPCODE = 0b0010011u

        @JvmStatic
        protected val LOAD_UPPER_IMMEDIATE_OPCODE = 0b0110111u

        @JvmStatic
        protected val ADD_UPPER_IMMEDIATE_TO_PC_OPCODE = 0b0010111u

        private val R_TYPE_OPCODES = setOf(BINARY_OP_OPCODE)
        private val I_TYPE_OPCODES = setOf(JALR_OPCODE, LOAD_OPCODE, ARITHMETIC_OP_IMMEDIATE_OPCODE)
        private val S_TYPE_OPCODES = setOf(STORE_OPCODE)
        private val B_TYPE_OPCODES = setOf(BRANCH_OPCODE)
        private val U_TYPE_OPCODES = setOf(LOAD_UPPER_IMMEDIATE_OPCODE, ADD_UPPER_IMMEDIATE_TO_PC_OPCODE)
        private val J_TYPE_OPCODES = setOf(JAL_OPCODE)
    }
}
