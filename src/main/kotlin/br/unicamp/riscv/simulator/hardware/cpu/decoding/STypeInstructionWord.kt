package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.Instruction
import br.unicamp.riscv.simulator.model.Store
import br.unicamp.riscv.simulator.model.StoreKind
import br.unicamp.riscv.simulator.model.Word

class STypeInstructionWord(word: Word) : InstructionWord(word) {
    override val imm: Word = this.rdId + (funct7 shl 5)

    override fun decode(): Instruction {
        val kind = when (funct3) {
            0b000u -> StoreKind.B
            0b001u -> StoreKind.H
            0b010u -> StoreKind.W
            else -> throw IllegalArgumentException("Unknown S Type function")
        }

        return Store(kind, rs1, rs1, imm)
    }
}