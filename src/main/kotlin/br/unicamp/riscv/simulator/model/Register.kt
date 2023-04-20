package br.unicamp.riscv.simulator.model

sealed class Register

object PC : Register()
data class XRegister(val id: Int) : Register() {
    val name: String
        get() {
            // rv32IRegName (RV32IX 0) = "zero"
            //rv32IRegName (RV32IX 1) = "ra"
            //rv32IRegName (RV32IX 2) = "sp"
            //rv32IRegName (RV32IX 3) = "gp"
            //rv32IRegName (RV32IX 4) = "tp"
            //rv32IRegName (RV32IX n)
            //    | 5 <= n && n <= 7    = printf "t%d" (n - 5)
            //    | 8 <= n && n <= 9    = printf "s%d" (n - 8)
            //    | 10 <= n && n <= 17  = printf "a%d" (n - 10)
            //    | 18 <= n && n <= 27  = printf "s%d" (n - 16)
            //    | 28 <= n && n <= 31  = printf "t%d" (n - 25)
            //    | otherwise           = undefined
            return ""
        }
}