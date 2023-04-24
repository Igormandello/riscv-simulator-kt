package br.unicamp.riscv.simulator

import br.unicamp.riscv.simulator.hardware.Memory
import br.unicamp.riscv.simulator.hardware.RegisterFile
import br.unicamp.riscv.simulator.hardware.cpu.Processor
import br.unicamp.riscv.simulator.log.Logger
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.nio.file.Path
import kotlin.io.path.*

suspend fun main(args: Array<String>) {
    val path = Path("./")
    val testFiles = args.flatMap { path.listDirectoryEntries(it) }.sorted()

    coroutineScope {
        testFiles.forEach {
            launch {
                it.simulate()
            }
        }
    }
}

private suspend fun Path.simulate() {
    println("Processing $name...")
    val registerFile = RegisterFile()
    val memory = Memory()
    val logFileName = parent.resolve("$nameWithoutExtension.log").pathString
    Logger(registerFile, logFileName).use { logger ->
        val processor = Processor(memory, registerFile, logger)
        memory.storeBytes(0x100u, readBytes().toUByteArray().toTypedArray())

        val cycles = processor.execute()
        println("$name ok, finished in $cycles cycles")
    }
}
