package br.unicamp.riscv.simulator.model

import br.unicamp.riscv.simulator.hardware.Memory
import br.unicamp.riscv.simulator.hardware.RegisterFile
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.assume
import io.kotest.property.checkAll

fun Arb.Companion.xRegister() = Arb.int(0..31).map(::XRegister)
fun Arb.Companion.imm20() = Arb.uInt(0u..0xFFFFFu)
fun Arb.Companion.imm12() = Arb.uImm12().map { it.signExtend(12) }
fun Arb.Companion.uImm12() = Arb.uInt(0u..0xFFFu)

class InstructionTest : DescribeSpec({
    describe("LUI") {
        val arbLUI = arbitrary {
            val rd = Arb.xRegister().bind()
            val imm = Arb.uInt(0u..0x7FFFFu).bind()
            LoadUpperImmediate(rd, imm)
        }

        it("affects destination register") {
            checkSetDestRegister(arbLUI, { it.rd }) { instruction, _, _ ->
                instruction.imm shl 12
            }
        }

        it("preserves other registers") {
            checkPreservesOtherRegisters(arbLUI) { it.rd }
        }

        it("advances PC") {
            checkAdvancePC(arbLUI)
        }
    }

    describe("AUIPC") {
        val arbAUIPC = arbitrary {
            val rd = Arb.xRegister().bind()
            val imm = Arb.uInt(0u..0xFFFFFu).bind()
            AddUpperImmediateToPC(rd, imm)
        }

        it("affects destination register") {
            checkSetDestRegister(arbAUIPC, { it.rd }) { instruction, registerFile, _ ->
                registerFile[PC] + (instruction.imm shl 12)
            }
        }

        it("preserves other registers") {
            checkPreservesOtherRegisters(arbAUIPC) { it.rd }
        }

        it("advances PC") {
            checkAdvancePC(arbAUIPC)
        }
    }

    describe("JAL") {
        val arbJAL = arbitrary {
            val rd = Arb.xRegister().bind()
            val imm = Arb.imm20().bind()
            JumpAndLink(rd, imm)
        }

        it("affects destination register") {
            checkSetDestRegister(arbJAL, { it.rd }) { _, registerFile, _ ->
                registerFile[PC] + IALIGN
            }
        }

        it("preserves other registers") {
            checkPreservesOtherRegisters(arbJAL) { it.rd }
        }

        it("jumps") {
            checkAll(arbJAL) { instruction ->
                val registerFile = RegisterFile()
                val memory = Memory()

                val addr = registerFile[PC] + instruction.imm

                instruction.execute(registerFile, memory)

                registerFile[PC] shouldBe addr
            }
        }
    }

    describe("JALR") {
        val arbJALR = arbitrary {
            val rd = Arb.xRegister().bind()
            val rs1 = Arb.xRegister().bind()
            val imm = Arb.imm12().bind()
            JumpAndLinkRegister(rd, rs1, imm)
        }

        it("affects destination register") {
            checkSetDestRegister(arbJALR, { it.rd }) { instruction, registerFile, _ ->
                registerFile[instruction.rs1] + IALIGN
            }
        }

        it("preserves other registers") {
            checkPreservesOtherRegisters(arbJALR) { it.rd }
        }

        it("jumps") {
            checkAll(arbJALR) { instruction ->
                val registerFile = RegisterFile()
                val memory = Memory()

                val addr = registerFile[instruction.rs1] + instruction.imm

                instruction.execute(registerFile, memory)

                registerFile[PC] shouldBe addr
            }
        }
    }

    describe("Conditional branch") {
        for (cond in BranchCondition.values()) {
            describe(cond.name) {
                val arbCond = arbitrary {
                    val rs1 = Arb.xRegister().bind()
                    val rs2 = Arb.xRegister().bind()
                    val imm = Arb.imm12().bind()
                    ConditionalBranch(cond, rs1, rs2, imm)
                }

                it("preserves registers") {
                    checkPreserveAllRegisters(arbCond)
                }
            }
        }

        val arbCond = arbitrary {
            val rs1 = Arb.xRegister().bind()
            val rs2 = Arb.xRegister().bind()
            val imm = Arb.imm12().bind()
            ConditionalBranch(BranchCondition.EQ, rs1, rs2, imm)
        }

        it("jumps if condition passes") {
            checkAll(arbCond) { instruction ->
                val registerFile = RegisterFile()
                val memory = Memory()

                assume(registerFile[instruction.rs1] == 0u)
                assume(registerFile[instruction.rs2] == 0u)

                val addr = registerFile[PC] + instruction.imm

                instruction.execute(registerFile, memory)

                registerFile[PC] shouldBe addr
            }
        }

        it("jumps if condition fails") {
            checkAll(arbCond) { instruction ->
                assume(instruction.rs1 != instruction.rs2)
                assume(instruction.rs1 != XRegister(0))
                assume(instruction.rs2 != XRegister(0))

                val registerFile = RegisterFile()
                val memory = Memory()

                registerFile[instruction.rs1] = 0u
                registerFile[instruction.rs2] = 1u

                val pc = registerFile[PC]

                instruction.execute(registerFile, memory)

                registerFile[PC] shouldBe pc + IALIGN
            }
        }
    }

    describe("Load") {
        for (kind in LoadKind.values()) {
            describe("L" + kind.name) {
                val arbLoad = arbitrary {
                    val rd = Arb.xRegister().bind()
                    val rs1 = Arb.xRegister().bind()
                    val imm = Arb.imm12().bind()
                    Load(kind, rd, rs1, imm)
                }

                it("advances PC") {
                    checkAdvancePC(arbLoad)
                }

                it("reads from memory") {
                    checkAll(arbLoad) { instruction ->
                        assume(instruction.rd != XRegister(0))

                        val registerFile = RegisterFile()
                        val memory = Memory()

                        val address = registerFile[instruction.rs1] + instruction.imm
                        assume(address <= UInt.MAX_VALUE - 4u)

                        when (kind) {
                            LoadKind.B -> {
                                val value = Arb.byte().sample(randomSource()).value
                                memory[address] = value.toUByte()

                                instruction.execute(registerFile, memory)

                                registerFile[instruction.rd] shouldBeEqual value.toInt().toUInt()
                            }

                            LoadKind.BU -> {
                                val value = Arb.uByte().sample(randomSource()).value
                                memory[address] = value

                                instruction.execute(registerFile, memory)

                                registerFile[instruction.rd] shouldBeEqual value.toUInt()
                            }

                            LoadKind.H -> {
                                val value = Arb.short().sample(randomSource()).value
                                memory.storeShort(address, value.toUShort())

                                instruction.execute(registerFile, memory)

                                registerFile[instruction.rd] shouldBeEqual value.toUInt()
                            }

                            LoadKind.HU -> {
                                val value = Arb.uShort().sample(randomSource()).value
                                memory.storeShort(address, value)

                                instruction.execute(registerFile, memory)

                                registerFile[instruction.rd] shouldBeEqual value.toUInt()
                            }

                            LoadKind.W -> {
                                val value = Arb.uInt().sample(randomSource()).value
                                memory.storeWord(address, value)

                                instruction.execute(registerFile, memory)

                                registerFile[instruction.rd] shouldBeEqual value
                            }
                        }
                    }
                }
            }
        }
    }

    describe("Store") {
        for (kind in StoreKind.values()) {
            describe("S" + kind.name) {
                val arbStore = arbitrary {
                    val rs1 = Arb.xRegister().bind()
                    val rs2 = Arb.xRegister().bind()
                    val imm = Arb.imm12().bind()
                    Store(kind, rs1, rs2, imm)
                }

                it("advances PC") {
                    checkAdvancePC(arbStore)
                }

                it("affects memory") {
                    checkAll(arbStore) { instruction ->
                        val registerFile = RegisterFile()
                        val memory = Memory()

                        instruction.execute(registerFile, memory)

                        val address = registerFile[instruction.rs2] + instruction.imm

                        assume(address <= UInt.MAX_VALUE - 4u)

                        when (kind) {
                            StoreKind.B -> memory[address] shouldBe registerFile[instruction.rs1].toUByte()
                            StoreKind.H -> memory.loadShort(address) shouldBe registerFile[instruction.rs1].toUShort()
                            StoreKind.W -> memory.loadWord(address) shouldBe registerFile[instruction.rs1]
                        }
                    }
                }

                it("preserves registers") {
                    checkPreserveAllRegisters(arbStore)
                }
            }
        }
    }

    describe("Binary operation") {
        for (kind in BinaryOpKind.values()) {
            describe(kind.name) {
                val arbBinOp = arbitrary {
                    val rd = Arb.xRegister().bind()
                    val rs1 = Arb.xRegister().bind()
                    val rs2 = Arb.xRegister().bind()
                    BinaryOp(kind, rd, rs1, rs2)
                }

                it("affects destination register") {
                    checkSetDestRegister(arbBinOp, { it.rd }) { instruction, registerFile, _ ->
                        kind.op(registerFile[instruction.rs1], registerFile[instruction.rs2])
                    }
                }

                it("preserves other registers") {
                    checkPreservesOtherRegisters(arbBinOp) { it.rd }
                }

                it("advances PC") {
                    checkAdvancePC(arbBinOp)
                }
            }

            describe(kind.name + "I") {
                val arbBinOp = arbitrary {
                    val rd = Arb.xRegister().bind()
                    val rs1 = Arb.xRegister().bind()
                    val imm = Arb.uImm12().bind()
                    BinaryOpImmediate(kind, rd, rs1, imm)
                }

                it("affects destination register") {
                    checkSetDestRegister(arbBinOp, { it.rd }) { instruction, registerFile, _ ->
                        kind.op(registerFile[instruction.rs1], instruction.imm)
                    }
                }

                it("preserves other registers") {
                    checkPreservesOtherRegisters(arbBinOp) { it.rd }
                }

                it("advances PC") {
                    checkAdvancePC(arbBinOp)
                }
            }
        }
    }

    describe("SLT") {
        for (signed in arrayOf(true, false)) {
            describe("Signed = $signed") {
                val arbSLT = arbitrary {
                    val rd = Arb.xRegister().bind()
                    val rs1 = Arb.xRegister().bind()
                    val rs2 = Arb.xRegister().bind()
                    SetIfLessThan(signed, rd, rs1, rs2)
                }

                val cmp: (UInt, UInt) -> Boolean = if (signed) { x, y -> x.toInt() < y.toInt() }
                else { x, y -> x < y }

                it("affects destination register") {
                    checkSetDestRegister(arbSLT, { it.rd }) { instruction, registerFile, _ ->
                        val x = registerFile[instruction.rs1]
                        val y = registerFile[instruction.rs2]
                        if (cmp(x, y)) 1u else 0u
                    }
                }

                it("preserves other registers") {
                    checkPreservesOtherRegisters(arbSLT) { it.rd }
                }

                it("advances PC") {
                    checkAdvancePC(arbSLT)
                }
            }
        }
    }

    describe("SLTI") {
        for (signed in arrayOf(true, false)) {
            describe("Signed = $signed") {
                val arbSLTI = arbitrary {
                    val rd = Arb.xRegister().bind()
                    val rs1 = Arb.xRegister().bind()
                    val imm = Arb.uImm12().bind()
                    SetIfLessThanImmediate(signed, rd, rs1, imm)
                }

                val cmp: (UInt, UInt) -> Boolean = if (signed) { x, y -> x.toInt() < y.toInt() }
                else { x, y -> x < y }

                it("affects destination register") {
                    checkSetDestRegister(arbSLTI, { it.rd }) { instruction, registerFile, _ ->
                        val x = registerFile[instruction.rs1]
                        val y = instruction.imm
                        if (cmp(x, y)) 1u else 0u
                    }
                }

                it("preserves other registers") {
                    checkPreservesOtherRegisters(arbSLTI) { it.rd }
                }

                it("advances PC") {
                    checkAdvancePC(arbSLTI)
                }
            }
        }
    }
})

private suspend fun checkAdvancePC(arb: Arb<Instruction>) {
    checkAll(arb) { instruction ->
        val registerFile = RegisterFile()
        val memory = Memory()

        val pc = registerFile[PC]

        instruction.execute(registerFile, memory)

        registerFile[PC] shouldBe pc + IALIGN
    }
}

private suspend fun <I : Instruction> checkSetDestRegister(
    arb: Arb<I>, rd: (I) -> XRegister, fn: (I, RegisterFile, Memory) -> UInt
) {
    checkAll(arb) { instruction ->
        val dest = rd(instruction)
        assume(dest != XRegister(0))

        val registerFile = RegisterFile()
        val memory = Memory()

        val expected = fn(instruction, registerFile, memory)

        instruction.execute(registerFile, memory)

        registerFile[dest] shouldBe expected
    }
}

private suspend fun <I : Instruction> checkPreservesOtherRegisters(
    arb: Arb<I>, rd: (I) -> XRegister
) {
    checkAll(arb, Arb.xRegister(), Arb.uInt()) { instruction, reg, value ->
        assume(rd(instruction) != reg)
        assume(reg != XRegister(0))

        val registerFile = RegisterFile()
        val memory = Memory()

        registerFile[reg] = value
        instruction.execute(registerFile, memory)

        registerFile[reg] shouldBe value
    }
}

private suspend fun <I : Instruction> checkPreserveAllRegisters(arb: Arb<I>) {
    checkAll(arb, Arb.xRegister()) { instruction, reg ->
        val registerFile = RegisterFile()
        val memory = Memory()

        val before = registerFile[reg]
        instruction.execute(registerFile, memory)

        registerFile[reg] shouldBeEqual before
    }
}
