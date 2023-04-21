package br.unicamp.riscv.simulator.hardware

import br.unicamp.riscv.simulator.model.Register
import br.unicamp.riscv.simulator.model.PC
import br.unicamp.riscv.simulator.model.XRegister

class RegisterFile {
    private val _registers: Array<UInt> = Array(32) { 0u }

    operator fun get(reg: Register): UInt =
        when (reg) {
            XRegister(0) -> 0u
            else -> _registers[index(reg)]
        }

    operator fun set(reg: Register, value: UInt) {
        if (reg is XRegister && reg.id == 0) {
            return
        }

        _registers[index(reg)] = value
    }

    private fun index(reg: Register) =
        when (reg) {
            is PC -> 0
            is XRegister -> reg.id
        }
}
