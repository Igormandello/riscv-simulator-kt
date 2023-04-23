package br.unicamp.riscv.simulator.log

import br.unicamp.riscv.simulator.hardware.RegisterFile
import br.unicamp.riscv.simulator.model.Disassembly
import br.unicamp.riscv.simulator.model.Word
import br.unicamp.riscv.simulator.model.XRegister
import java.io.File

class Logger(private val registerFile: RegisterFile, logFileName: String) {
    private val logFile = File(logFileName)
    init {
        logFile.delete()
        logFile.createNewFile()
    }

    fun buildState(pcAddress: Word, rs1: XRegister, rs2: XRegister): StatefulLogger {
        return StatefulLogger(pcAddress, rs1, rs2)
    }

    inner class StatefulLogger(private val pcAddress: Word, private val rs1: XRegister, private val rs2: XRegister) {
        private val rs1BeforeInstruction: Word = registerFile[rs1]
        private val rs2BeforeInstruction: Word = registerFile[rs2]

        fun log(rd: XRegister, instruction: Word, disassembly: Disassembly) {
            val rdAfterInstruction = registerFile[rd]
            val logString = "PC=${pcAddress.toHex()} [${instruction.toHex()}]" +
                    " ${rd.xId}=${rdAfterInstruction.toHex()}" +
                    " ${rs1.xId}=${rs1BeforeInstruction.toHex()}" +
                    " ${rs2.xId}=${rs2BeforeInstruction.toHex()}" +
                    " ${disassembly.mnemonic.padEnd(8, ' ')} ${disassembly.args.joinToString()}"

            logFile.appendText(logString + "\n")
        }

        private fun Word.toHex() = this.toString(16).padStart(8, '0')
        private val XRegister.xId: String get() = "x${this.id.toString().padStart(2, '0')}"
    }
}
