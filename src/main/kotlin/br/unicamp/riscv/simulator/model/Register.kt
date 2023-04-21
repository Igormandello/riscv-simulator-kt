package br.unicamp.riscv.simulator.model

import java.lang.RuntimeException

sealed class Register

object PC : Register()
data class XRegister(val id: Int) : Register() {
    val name: String get() = when (id) {
        in 0..4 -> arrayOf("zero", "ra", "sp", "gp", "tp")[id]
        in 5..7 -> "t${id - 5}"
        in 8..9 -> "s${id - 8}"
        in 10..17 -> "a${id - 10}"
        in 18..27 -> "s${id - 16}"
        in 28..31 -> "t${id - 25}"
        else -> throw UnmappedRegisterException(id)
    }
}

data class UnmappedRegisterException(val id: Int) : RuntimeException()
