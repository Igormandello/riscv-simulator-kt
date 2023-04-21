package br.unicamp.riscv.simulator.hardware.cpu

import br.unicamp.riscv.simulator.hardware.Memory
import br.unicamp.riscv.simulator.hardware.RegisterFile
import br.unicamp.riscv.simulator.hardware.cpu.decoding.Decoder
import br.unicamp.riscv.simulator.model.PC

class Processor(private val memory: Memory, private val registerFile: RegisterFile, private val decoder: Decoder) {
    fun execute() {
        while (registerFile[PC] > 0u) {
            val pcAddress = registerFile[PC]
            val fetchedInstruction = memory.loadWord(pcAddress)
            val instruction = decoder.decodeInstruction(fetchedInstruction)
            instruction.execute(registerFile, memory)
        }
    }
}
