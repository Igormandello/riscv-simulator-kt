package br.unicamp.riscv.simulator.log

import br.unicamp.riscv.simulator.hardware.RegisterFile
import br.unicamp.riscv.simulator.model.Disassembly
import br.unicamp.riscv.simulator.model.Word
import br.unicamp.riscv.simulator.model.XRegister

class LogFormatter() {
    fun build(
        registerFile: RegisterFile,
        pcAddress: Word,
        rs1: XRegister,
        rs2: XRegister,
        instruction: Word,
        disassembly: Disassembly
    ): LogBuilder = LogBuilder(registerFile, pcAddress, rs1, rs2, instruction, disassembly)

    inner class LogBuilder(
        private val registerFile: RegisterFile,
        private val pcAddress: Word,
        private val rs1: XRegister,
        private val rs2: XRegister,
        private val instruction: Word,
        private val disassembly: Disassembly
    ) {
        private val rs1BeforeInstruction: Word = registerFile[rs1]
        private val rs2BeforeInstruction: Word = registerFile[rs2]

        fun format(rd: XRegister): String {
            val rdAfterInstruction = registerFile[rd]
            return "PC=${pcAddress.toHex()} [${instruction.toHex()}]" +
                    " ${rd.xId}=${rdAfterInstruction.toHex()}" +
                    " ${rs1.xId}=${rs1BeforeInstruction.toHex()}" +
                    " ${rs2.xId}=${rs2BeforeInstruction.toHex()}" +
                    " ${disassembly.mnemonic.padEnd(8, ' ')} ${disassembly.args.joinToString()}"
                    .trimEnd()
        }

        private fun Word.toHex() = this.toString(16).padStart(8, '0')
        private val XRegister.xId: String get() = "x${this.id.toString().padStart(2, '0')}"
    }
}
