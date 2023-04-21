package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.AddUpperImmediateToPC
import br.unicamp.riscv.simulator.model.Instruction
import br.unicamp.riscv.simulator.model.LoadUpperImmediate
import br.unicamp.riscv.simulator.model.Word
import br.unicamp.riscv.simulator.model.bitRange

class UTypeInstructionWord(word: Word) : InstructionWord(word) {
    override val imm: Word = word.bitRange(12..31) shl 12

    override fun decode(): Instruction = when (opcode) {
        LOAD_UPPER_IMMEDIATE_OPCODE -> LoadUpperImmediate(rd, imm)
        ADD_UPPER_IMMEDIATE_TO_PC_OPCODE -> AddUpperImmediateToPC(rd, imm)
        else -> throw IllegalArgumentException("Unknown U Type opcode")
    }
}
