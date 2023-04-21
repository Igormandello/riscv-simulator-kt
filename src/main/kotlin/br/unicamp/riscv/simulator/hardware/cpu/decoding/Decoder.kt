package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.Instruction
import br.unicamp.riscv.simulator.model.Word

class Decoder {
    fun decodeInstruction(word: Word): Instruction {
        val instructionWord = InstructionWord.createInstructionWord(word)
        return instructionWord.decode()
    }
}
