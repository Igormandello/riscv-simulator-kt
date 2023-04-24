package br.unicamp.riscv.simulator

import br.unicamp.riscv.simulator.hardware.Memory
import br.unicamp.riscv.simulator.hardware.RegisterFile
import br.unicamp.riscv.simulator.hardware.cpu.Processor
import br.unicamp.riscv.simulator.log.Logger
import java.io.File
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
suspend fun main(args: Array<String>) {
    val context = newFixedThreadPoolContext(16, "main-dispatcher")
    val scope = CoroutineScope(context)

    File("./test/build/bin/")
        .listFiles()
        ?.sorted()
        ?.map { scope.launch { it.simulate() } }
        ?.joinAll()
}

private fun File.simulate() {
    println("Processing ${this.name}...")
    val registerFile = RegisterFile()
    val memory = Memory()
    val logger = Logger(registerFile, "./test/${this.nameWithoutExtension}.log")
    val processor = Processor(memory, registerFile, logger)

    this.readBytes().forEachIndexed { i, byte -> memory.storeByte(0x100u + i.toUInt(), byte.toUByte()) }
    processor.execute()
}
