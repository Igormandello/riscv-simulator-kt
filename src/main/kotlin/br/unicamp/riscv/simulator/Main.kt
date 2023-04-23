package br.unicamp.riscv.simulator

import br.unicamp.riscv.simulator.hardware.Memory
import br.unicamp.riscv.simulator.hardware.RegisterFile
import br.unicamp.riscv.simulator.hardware.cpu.Processor
import br.unicamp.riscv.simulator.log.Logger
import java.io.File

fun main(args: Array<String>) {
    File("./test/build/elf/")
        .listFiles()
        ?.sorted()
        ?.forEach {
            println("Processing ${it.name}...")
            val registerFile = RegisterFile()
            val memory = Memory()
            val logger = Logger(registerFile, "./test/${it.nameWithoutExtension}.log")
            val processor = Processor(memory, registerFile, logger)

            it.readBytes().forEachIndexed { i, byte -> memory.storeByte(i.toUInt(), byte.toUByte()) }
            processor.execute()
        }
}
