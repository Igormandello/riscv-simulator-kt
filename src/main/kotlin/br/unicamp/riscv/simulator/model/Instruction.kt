package br.unicamp.riscv.simulator.model

import br.unicamp.riscv.simulator.hardware.Memory
import br.unicamp.riscv.simulator.hardware.RegisterFile

sealed interface Instruction {
    fun execute(registerFile: RegisterFile, memory: Memory)
    fun disassembly(): Disassembly
}

data class Add(val rd: XRegister, val rs1: XRegister, val rs2: XRegister) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        TODO("Not yet implemented")
    }

    override fun disassembly() = Disassembly("ADD", rd.name, rs1.name, rs2.name)
}
