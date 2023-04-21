package br.unicamp.riscv.simulator.model

import br.unicamp.riscv.simulator.hardware.Memory
import br.unicamp.riscv.simulator.hardware.RegisterFile

sealed interface Instruction {
    fun execute(registerFile: RegisterFile, memory: Memory)
    fun disassembly(): Disassembly
}

const val IALIGN = 4u

data class LoadUpperImmediate(val rd: XRegister, val imm: UInt) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        registerFile[rd] = imm shl IMM_OFFSET
        registerFile[PC] += IALIGN
    }

    override fun disassembly() = Disassembly("LUI", rd.name, imm)

    companion object {
        private const val IMM_OFFSET = 12
    }
}

data class AddUpperImmediateToPC(val rd: XRegister, val imm: UInt) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        registerFile[rd] = registerFile[PC] + (imm shl IMM_OFFSET)
        registerFile[PC] += IALIGN
    }

    override fun disassembly() = Disassembly("AUIPC", rd.name, imm)

    companion object {
        private const val IMM_OFFSET = 12
    }
}

data class JumpAndLink(val rd: XRegister, val imm: Int) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        registerFile[rd] = registerFile[PC] + IALIGN
        registerFile[PC] = (registerFile[PC].toInt() + imm).toUInt()
    }

    override fun disassembly() = Disassembly("JAL", rd.name, imm)
}

data class JumpAndLinkRegister(val rd: XRegister, val rs1: XRegister, val imm: Int) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        val address = registerFile[rs1].toInt() + imm
        registerFile[rd] = registerFile[PC] + IALIGN
        registerFile[PC] = address.toUInt()
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
        val x = registerFile[rs1]
        val y = registerFile[rs2]
        if (condition.test(x, y)) {
            registerFile[PC] = (registerFile[PC].toInt() + imm).toUInt()
        } else {
            registerFile[PC] += IALIGN
        }
    }

    override fun disassembly() = Disassembly("B${condition.name}", rs1.name, rs2.name, imm)
}

enum class LoadKind {
    B,
    BU,
    H,
    HU,
    W
}

data class Load(val kind: LoadKind, val rd: XRegister, val rs1: XRegister, val imm: Int) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        val address = (registerFile[rs1].toInt() + imm).toUInt()
        registerFile[rd] = when (kind) {
            LoadKind.B  -> memory.loadByte(address).toByte().toInt().toUInt()
            LoadKind.BU -> memory.loadByte(address).toUInt()
            LoadKind.H  -> memory.loadShort(address).toShort().toInt().toUInt()
            LoadKind.HU -> memory.loadShort(address).toUInt()
            LoadKind.W  -> memory.loadWord(address)
        }
        registerFile[PC] += IALIGN
    }

    override fun disassembly() = Disassembly("L${kind}", rd, rs1, imm)
}

enum class StoreKind {
    B,
    H,
    W
}

data class Store(val kind: StoreKind, val rs1: XRegister, val rs2: XRegister, val imm: Int) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        val address = (registerFile[rs2].toInt() + imm).toUInt()
        val word = registerFile[rs1]
        when (kind) {
            StoreKind.B -> memory.storeByte(address, word.toUByte())
            StoreKind.H -> memory.storeShort(address, word.toUShort())
            StoreKind.W -> memory.storeWord(address, word)
        }
        registerFile[PC] += IALIGN
    }

    override fun disassembly() = Disassembly("S${kind}", rs1, rs2, imm)
}

enum class BinaryOpKind(val op: (UInt, UInt) -> UInt) {
    ADD(UInt::plus),
    AND(UInt::and),
    OR(UInt::or),
    XOR(UInt::xor),
    SLL({ x, y -> x shl (y and 0x1Fu).toInt() }),
    SRL({ x, y -> x shr (y and 0x1Fu).toInt() }),
    SRA({ x, y -> (x.toInt() shr (y and 0x1Fu).toInt()).toUInt() }),
    MUL({ x, y -> x * y }),
    MULH({ x, y -> (x.toUShort().toShort() * y.toUShort().toShort()).toUInt() }),
    MULHU({ x, y -> x.toUShort() * y.toUShort() }),
    MULHSU({ x, y -> (x.toUShort().toShort() * y.toUShort().toInt()).toUInt() }),
    DIV({ x, y -> if (y == 0u) UInt.MAX_VALUE else (x.toInt() / y.toInt()).toUInt() }),
    DIVU({ x, y -> if (y == 0u) UInt.MAX_VALUE else x / y }),
    REM({ x, y -> if (y == 0u) x else (x.toInt() % y.toInt()).toUInt() }),
    REMU({ x, y -> if (y == 0u) x else x % y })
}

data class BinaryOp(
    val kind: BinaryOpKind,
    val rd: XRegister,
    val rs1: XRegister,
    val rs2: XRegister
) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        val x = registerFile[rs1]
        val y = registerFile[rs2]
        registerFile[rd] = kind.op(x, y)
        registerFile[PC] += IALIGN
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
        val x = registerFile[rs1]
        registerFile[rd] = kind.op(x, imm)
        registerFile[PC] += IALIGN
    }

    override fun disassembly() = Disassembly(kind.name + "I", rd.name, rs1.name, imm.toInt())
}

data class Subtract(val rd: XRegister, val rs1: XRegister, val rs2: XRegister) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        val x = registerFile[rs1]
        val y = registerFile[rs2]
        registerFile[rd] = x - y
        registerFile[PC] += IALIGN
    }

    override fun disassembly() = Disassembly("SUB", rd.name, rs1.name, rs2.name)
}

private fun slt(signed: Boolean, x: UInt, y: UInt): UInt =
    if (signed && x.toInt() < y.toInt() || !signed && x < y) 1u else 0u

data class SetIfLessThan(
    val signed: Boolean,
    val rd: XRegister,
    val rs1: XRegister,
    val rs2: XRegister
) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        val x = registerFile[rs1]
        val y = registerFile[rs2]
        registerFile[rd] = slt(signed, x, y)
        registerFile[PC] += IALIGN
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
        val x = registerFile[rs1]
        registerFile[rd] = slt(signed, x, imm)
        registerFile[PC] += IALIGN
    }

    override fun disassembly() = Disassembly("SLTI${if (signed) "" else "U"}", rd.name, rs1.name, imm)
}
