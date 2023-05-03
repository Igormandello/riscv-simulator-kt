package br.unicamp.riscv.simulator

import br.unicamp.riscv.simulator.hardware.Memory
import br.unicamp.riscv.simulator.hardware.RegisterFile
import br.unicamp.riscv.simulator.hardware.cpu.Processor
import br.unicamp.riscv.simulator.loader.ProgramLoader
import br.unicamp.riscv.simulator.log.LogFormatter
import br.unicamp.riscv.simulator.log.withLogContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.lang.Exception
import java.nio.file.Path
import kotlin.io.path.*

suspend fun main(args: Array<String>) {
    val workingDir = Path("./")

    val simulationFiles = args
        .asSequence()
        .flatMap { workingDir.listDirectoryEntries(it).asSequence().map(Path::absolute).map(Path::normalize) }
        .sorted()
        .toList()

    logger.debug { "Received command line arguments: ${args.toList()}" }
    logger.debug { "Found simulations: $simulationFiles" }

    coroutineScope {
        simulationFiles.forEach {
            launch {
                it.simulate()
            }
        }
    }

    logger.info("Done")
}

private suspend fun Path.simulate() {
    withLogContext(this) {
        logger.info("Loading program $name")

        val memory = Memory()

        val loader = ProgramLoader()
        val entrypoint = loader.loadProgram(this@simulate, memory)

        val registerFile = RegisterFile()
        val logFormatter = LogFormatter()

        val processor = Processor(memory, registerFile, logFormatter)

        logger.info("Running program $name")
        try {
            val cycles = processor.execute(entrypoint)
            logger.info("Program $name finished in $cycles cycles")
        } catch (ex: Exception) {
            logger.error("Failed to run program $name due to an unexpected exception", ex)
        }
    }
}

private val logger = KotlinLogging.logger {}
