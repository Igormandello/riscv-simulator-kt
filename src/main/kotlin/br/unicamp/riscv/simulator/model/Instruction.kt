package br.unicamp.riscv.simulator.model

import br.unicamp.riscv.simulator.hardware.Memory
import br.unicamp.riscv.simulator.hardware.RegisterFile

sealed interface Instruction {
    fun execute(registerFile: RegisterFile, memory: Memory)
    fun disassembly(): Disassembly
}

data class LoadUpperImmediate(val rd: XRegister, val imm: UInt) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        TODO("Not yet implemented")
    }

    override fun disassembly() = Disassembly("LUI", rd.name, imm)
}

data class AddUpperImmediateToPC(val rd: XRegister, val imm: UInt) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        TODO("Not yet implemented")
    }

    override fun disassembly() = Disassembly("AUIPC", rd.name, imm)
}

data class JumpAndLink(val rd: XRegister, val imm: Int) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        TODO("Not yet implemented")
    }

    override fun disassembly() = Disassembly("JAL", rd.name, imm)
}

data class JumpAndLinkRegister(val rd: XRegister, val rs1: XRegister, val imm: Int) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        TODO("Not yet implemented")
    }

    override fun disassembly() = Disassembly("JALR", rd.name, rs1.name, imm)
}

enum class BranchCondition(val test: (UInt, UInt) -> Boolean) {
    EQ(UInt::equals),
    NE({ x, y -> x != y }),
    LT({ x, y -> x.toInt() < y.toInt() }),
    LTU({ x, y -> x < y }),
    GE({ x, y -> x.toInt() >= y.toInt() }),
    GEU({ x, y -> x >= y })
}

data class ConditionalBranch(
    val condition: BranchCondition,
    val rs1: XRegister,
    val rs2: XRegister,
    val imm: Int
) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        TODO("Not yet implemented")
    }

    override fun disassembly() = Disassembly("B${condition.name}", rs1.name, rs2.name, imm)
}

enum class LoadKind(val sizeBytes: Int, val signExtend: Boolean) {
    B(1, true),
    BU(1, false),
    H(2, true),
    HU(2, false),
    W(4, false)
}

data class Load(val kind: LoadKind, val rs1: XRegister, val rs2: XRegister, val imm: Int) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        TODO("Not yet implemented")
    }

    override fun disassembly() = Disassembly("L${kind}", rs1, rs2, imm)
}

enum class StoreKind(val sizeBytes: Int) {
    B(1),
    H(2),
    W(4)
}

data class Store(val kind: StoreKind, val rs1: XRegister, val rs2: XRegister, val imm: Int) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        TODO("Not yet implemented")
    }

    override fun disassembly() = Disassembly("S${kind}", rs1, rs2, imm)
}

enum class BinaryOpKind(val op: (UInt, UInt) -> UInt) {
    ADD(UInt::plus),
    AND(UInt::and),
    OR(UInt::or),
    XOR(UInt::xor),
    SLL({ x, y -> x shl y.toInt() }),
    SRL({ x, y -> x shr y.toInt() }),
    SRA({ x, y -> (x.toInt() shr y.toInt()).toUInt() })
}

data class BinaryOp(
    val kind: BinaryOpKind,
    val rd: XRegister,
    val rs1: XRegister,
    val rs2: XRegister
) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        TODO("Not yet implemented")
    }

    override fun disassembly() = Disassembly(kind.name, rd.name, rs1.name, rs2.name)
}

data class BinaryOpImmediate(
    val kind: BinaryOpKind,
    val rd: XRegister,
    val rs1: XRegister,
    val imm: UInt
) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        TODO("Not yet implemented")
    }

    override fun disassembly() = Disassembly(kind.name + "I", rd.name, rs1.name, imm.toInt())
}

data class Subtract(val rd: XRegister, val rs1: XRegister, val rs2: XRegister) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        TODO("Not yet implemented")
    }

    override fun disassembly() = Disassembly("SUB", rd.name, rs1.name, rs2.name)
}

data class SetIfLessThan(
    val signed: Boolean,
    val rd: XRegister,
    val rs1: XRegister,
    val rs2: XRegister
) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        TODO("Not yet implemented")
    }

    override fun disassembly() = Disassembly("SLT${if (signed) "" else "U"}", rd.name, rs1.name, rs2.name)
}

data class SetIfLessThanImmediate(
    val signed: Boolean,
    val rd: XRegister,
    val rs1: XRegister,
    val imm: UInt
) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        TODO("Not yet implemented")
    }

    override fun disassembly() = Disassembly("SLTI${if (signed) "" else "U"}", rd.name, rs1.name, imm)
}
