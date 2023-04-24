package br.unicamp.riscv.simulator.hardware.cpu

import br.unicamp.riscv.simulator.hardware.Memory
import br.unicamp.riscv.simulator.hardware.RegisterFile
import br.unicamp.riscv.simulator.hardware.cpu.decoding.InstructionWord
import br.unicamp.riscv.simulator.log.Logger
import br.unicamp.riscv.simulator.model.PC

class Processor(private val memory: Memory, private val registerFile: RegisterFile, private val logger: Logger) {
    suspend fun execute(): Int {
        var cycleCount = 0

        registerFile[PC] = 0x100u
        while (registerFile[PC] != 0x20000000u) {
            val pcAddress = registerFile[PC]
            val fetchedInstruction = memory.loadWord(pcAddress)

            val instructionWord = InstructionWord.createInstructionWord(fetchedInstruction)
            val instruction = instructionWord.decode()

            val loggerState = logger.buildState(pcAddress, instructionWord.rs1, instructionWord.rs2)
            instruction.execute(registerFile, memory)

            loggerState.log(instructionWord.rd, fetchedInstruction, instruction.disassembly())
            cycleCount += instruction.cycleCount
        }

        return cycleCount
    }
}
