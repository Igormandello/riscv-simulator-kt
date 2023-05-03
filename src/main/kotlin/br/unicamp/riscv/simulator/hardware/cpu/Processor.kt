package br.unicamp.riscv.simulator.hardware.cpu

import br.unicamp.riscv.simulator.hardware.Memory
import br.unicamp.riscv.simulator.hardware.RegisterFile
import br.unicamp.riscv.simulator.hardware.cpu.decoding.InstructionWord
import br.unicamp.riscv.simulator.log.LogFormatter
import br.unicamp.riscv.simulator.model.EBreakException
import br.unicamp.riscv.simulator.model.PC
import br.unicamp.riscv.simulator.model.Word
import mu.KotlinLogging

class Processor(private val memory: Memory, private val registerFile: RegisterFile, private val logFormatter: LogFormatter) {
    fun execute(entrypoint: Word): Int {
        var cycleCount = 0

        registerFile[PC] = entrypoint

        while (true) {
            val pcAddress = registerFile[PC]
            val fetchedInstruction = memory.loadWord(pcAddress)

            val instructionWord = InstructionWord.createInstructionWord(fetchedInstruction)
            val instruction = instructionWord.decode()

            val logBuilder = logFormatter.build(
                registerFile,
                pcAddress,
                instructionWord.rs1,
                instructionWord.rs2,
                fetchedInstruction,
                instruction.disassembly(pcAddress)
            )

            try {
                instruction.execute(registerFile, memory)
            } catch (ex: EBreakException) {
                logger.debug { "EBREAK instruction reached, process finished" }
                break
            } finally {
                cycleCount += instruction.cycleCount
                simulationLogger.trace { logBuilder.format(instructionWord.rd) }
            }
        }

        return cycleCount
    }

    companion object {
        private val simulationLogger = KotlinLogging.logger("simulationLogger")
        private val logger = KotlinLogging.logger {}
    }
}
