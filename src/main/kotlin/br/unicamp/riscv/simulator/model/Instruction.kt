package br.unicamp.riscv.simulator.model

import br.unicamp.riscv.simulator.hardware.Memory
import br.unicamp.riscv.simulator.hardware.RegisterFile

sealed interface Instruction {
    fun execute(registerFile: RegisterFile, memory: Memory)
    fun disassembly(pc: Word): Disassembly

    val cycleCount: Int
        get() = 1
}

const val IALIGN = 4u

data class LoadUpperImmediate(val rd: XRegister, val imm: UInt) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        registerFile[rd] = imm shl IMM_OFFSET
        registerFile[PC] += IALIGN
    }

    override fun disassembly(pc: Word) = Disassembly("LUI", rd.name, imm)

    companion object {
        private const val IMM_OFFSET = 12
    }
}

data class AddUpperImmediateToPC(val rd: XRegister, val imm: UInt) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        registerFile[rd] = registerFile[PC] + (imm shl IMM_OFFSET)
        registerFile[PC] += IALIGN
    }

    override fun disassembly(pc: Word) = Disassembly("AUIPC", rd.name, imm)

    companion object {
        private const val IMM_OFFSET = 12
    }
}

data class JumpAndLink(val rd: XRegister, val imm: Word) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        registerFile[rd] = registerFile[PC] + IALIGN
        registerFile[PC] = registerFile[PC] + imm
    }

    override fun disassembly(pc: Word): Disassembly {
        return Disassembly("JAL", rd.name, "0x${(pc + imm).toString(16)}")
    }
}

data class JumpAndLinkRegister(val rd: XRegister, val rs1: XRegister, val imm: Word) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        val address = registerFile[rs1] + imm
        registerFile[rd] = registerFile[PC] + IALIGN
        registerFile[PC] = address
    }

    override fun disassembly(pc: Word) = Disassembly("JALR", rd.name, "${imm.toInt()}($rs1)")
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
    val imm: Word
) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        val x = registerFile[rs1]
        val y = registerFile[rs2]
        if (condition.test(x, y)) {
            registerFile[PC] = registerFile[PC] + imm
        } else {
            registerFile[PC] += IALIGN
        }
    }

    override fun disassembly(pc: Word): Disassembly {
        return Disassembly("B${condition.name}", rs1.name, rs2.name, "0x${(pc + imm).toString(16)}")
    }
}

enum class LoadKind {
    B,
    BU,
    H,
    HU,
    W
}

data class Load(val kind: LoadKind, val rd: XRegister, val rs1: XRegister, val imm: Word) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        val address = registerFile[rs1] + imm
        registerFile[rd] = when (kind) {
            LoadKind.B  -> memory.loadByte(address).toUInt().signExtend(UByte.SIZE_BITS)
            LoadKind.BU -> memory.loadByte(address).toUInt()
            LoadKind.H  -> memory.loadShort(address).toUInt().signExtend(UShort.SIZE_BITS)
            LoadKind.HU -> memory.loadShort(address).toUInt()
            LoadKind.W  -> memory.loadWord(address)
        }
        registerFile[PC] += IALIGN
    }

    override fun disassembly(pc: Word) = Disassembly("L${kind}", rd, "${imm.toInt()}($rs1)")
}

enum class StoreKind {
    B,
    H,
    W
}

data class Store(val kind: StoreKind, val rs1: XRegister, val rs2: XRegister, val imm: Word) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        val address = registerFile[rs1] + imm
        val word = registerFile[rs2]
        when (kind) {
            StoreKind.B -> memory.storeByte(address, word.toUByte())
            StoreKind.H -> memory.storeShort(address, word.toUShort())
            StoreKind.W -> memory.storeWord(address, word)
        }
        registerFile[PC] += IALIGN
    }

    override fun disassembly(pc: Word) = Disassembly("S${kind}", rs2, "${imm.toInt()}($rs1)")
}

enum class BinaryOpKind(val op: (UInt, UInt) -> UInt) {
    ADD(UInt::plus),
    SUB(UInt::minus),
    AND(UInt::and),
    OR(UInt::or),
    XOR(UInt::xor),
    SLL({ x, y -> x shl (y and 0x1Fu).toInt() }),
    SRL({ x, y -> x shr (y and 0x1Fu).toInt() }),
    SRA({ x, y -> (x.toInt() shr (y and 0x1Fu).toInt()).toUInt() }),
    MUL({ x, y -> x * y }),
    MULH({ x, y -> (x.toInt().toLong() * y.toInt().toLong()).shr(32).toUInt() }),
    MULHU({ x, y -> (x.toULong() * y.toULong()).shr(32).toUInt() }),
    MULHSU({ x, y -> (x.toInt().toLong() * y.toLong()).toULong().shr(32).toUInt() }),
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

    override fun disassembly(pc: Word) = Disassembly(kind.name, rd.name, rs1.name, rs2.name)
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

    override fun disassembly(pc: Word) = Disassembly(kind.name + "I", rd.name, rs1.name, imm.toInt())
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

    override fun disassembly(pc: Word) = Disassembly("SLT${if (signed) "" else "U"}", rd.name, rs1.name, rs2.name)
}

data class SetIfLessThanImmediate(
    val signed: Boolean,
    val rd: XRegister,
    val rs1: XRegister,
    val imm: UInt
) : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) {
        val x = registerFile[rs1]
        registerFile[rd] = slt(signed, x, if (signed) imm else imm.signExtend(12))
        registerFile[PC] += IALIGN
    }

    override fun disassembly(pc: Word) = Disassembly("SLTI${if (signed) "" else "U"}", rd.name, rs1.name, imm.toInt())
}

object EBreak : Instruction {
    override fun execute(registerFile: RegisterFile, memory: Memory) = throw EBreakException

    override fun disassembly(pc: Word) = Disassembly("EBREAK", emptyList())
}
