package br.unicamp.riscv.simulator.hardware.cpu.decoding

import br.unicamp.riscv.simulator.model.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.IsStableType
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class DecoderTest : DescribeSpec({
    val decoder = Decoder()

    describe("LUI") {
        withData(
            InstructionDecoding(0b0000000000000000000_00000_0110111u, LoadUpperImmediate(XRegister(0), 0u)),
            InstructionDecoding(0b0000000000000000000_00101_0110111u, LoadUpperImmediate(XRegister(5), 0u)),
            InstructionDecoding(0b0000000000000101010_00101_0110111u, LoadUpperImmediate(XRegister(5), 42u))
        ) { (word: Word, expected: Instruction) ->
            decoder.decodeInstruction(word) shouldBe expected
        }
    }

    describe("AUIPC") {
        withData(
            InstructionDecoding(0b0000000000000000000_00000_0010111u, AddUpperImmediateToPC(XRegister(0), 0u)),
            InstructionDecoding(0b0000000000000000000_00101_0010111u, AddUpperImmediateToPC(XRegister(5), 0u)),
            InstructionDecoding(0b0000000000000101010_00101_0010111u, AddUpperImmediateToPC(XRegister(5), 42u))
        ) { (word: Word, expected: Instruction) ->
            decoder.decodeInstruction(word) shouldBe expected
        }
    }

    describe("JAL") {
        withData(
            InstructionDecoding(0b0_0000000000_0_00000000_00000_1101111u, JumpAndLink(XRegister(0), 0u)),
            InstructionDecoding(0b0_0000000000_0_00000000_00101_1101111u, JumpAndLink(XRegister(5), 0u)),
            InstructionDecoding(0b0_0000010101_0_00000000_00101_1101111u, JumpAndLink(XRegister(5), 42u)),
            InstructionDecoding(0b1_1111101011_1_11111111_00101_1101111u, JumpAndLink(XRegister(5), (-42).toUInt())),
        ) { (word: Word, expected: Instruction) ->
            decoder.decodeInstruction(word) shouldBe expected
        }
    }

    describe("JALR") {
        withData(
            InstructionDecoding(
                0b000000000000_00000_000_00000_1100111u,
                JumpAndLinkRegister(XRegister(0), XRegister(0), 0u)
            ),
            InstructionDecoding(
                0b000000000000_10100_000_00101_1100111u,
                JumpAndLinkRegister(XRegister(5), XRegister(20), 0u)
            ),
            InstructionDecoding(
                0b000000101010_10100_000_00101_1100111u,
                JumpAndLinkRegister(XRegister(5), XRegister(20), 42u)
            ),
            InstructionDecoding(
                0b111111010110_10100_000_00101_1100111u,
                JumpAndLinkRegister(XRegister(5), XRegister(20), (-42).toUInt())
            ),
        ) { (word: Word, expected: Instruction) ->
            decoder.decodeInstruction(word) shouldBe expected
        }
    }

    describe("Conditional branches") {
        withData(BranchCondition.values().toList()) { kind ->
            val fnBits = when (kind) {
                BranchCondition.EQ -> 0b000_000000000000u
                BranchCondition.NE -> 0b001_000000000000u
                BranchCondition.LT -> 0b100_000000000000u
                BranchCondition.LTU -> 0b110_000000000000u
                BranchCondition.GE -> 0b101_000000000000u
                BranchCondition.GEU -> 0b111_000000000000u
            }

            withData(
                InstructionDecoding(
                    0b0000000_00000_00000_000_00000_1100011u or fnBits,
                    ConditionalBranch(kind, XRegister(0), XRegister(0), 0u)
                ),
                InstructionDecoding(
                    0b0000000_10100_00101_000_00000_1100011u or fnBits,
                    ConditionalBranch(kind, XRegister(5), XRegister(20), 0u)
                ),
                InstructionDecoding(
                    0b0000001_10100_00101_000_01010_1100011u or fnBits,
                    ConditionalBranch(kind, XRegister(5), XRegister(20), 42u)
                ),
                InstructionDecoding(
                    0b1111110_10100_00101_000_10111_1100011u or fnBits,
                    ConditionalBranch(kind, XRegister(5), XRegister(20), (-42).toUInt())
                ),
            ) { (word: Word, expected: Instruction) ->
                decoder.decodeInstruction(word) shouldBe expected
            }
        }
    }

    describe("Load") {
        withData(LoadKind.values().toList()) { kind ->
            val fnBits = when (kind) {
                LoadKind.B -> 0b000_000000000000u
                LoadKind.BU -> 0b100_000000000000u
                LoadKind.H -> 0b001_000000000000u
                LoadKind.HU -> 0b101_000000000000u
                LoadKind.W -> 0b010_000000000000u
            }

            withData(
                InstructionDecoding(
                    0b000000000000_00000_000_00000_0000011u or fnBits,
                    Load(kind, XRegister(0), XRegister(0), 0u)
                ),
                InstructionDecoding(
                    0b000000000000_10100_000_00101_0000011u or fnBits,
                    Load(kind, XRegister(5), XRegister(20), 0u)
                ),
                InstructionDecoding(
                    0b000000101010_10100_000_00101_0000011u or fnBits,
                    Load(kind, XRegister(5), XRegister(20), 42u)
                ),
                InstructionDecoding(
                    0b111111010110_10100_000_00101_0000011u or fnBits,
                    Load(kind, XRegister(5), XRegister(20), (-42).toUInt())
                ),
            ) { (word: Word, expected: Instruction) ->
                decoder.decodeInstruction(word) shouldBe expected
            }
        }
    }

    describe("Store") {
        withData(StoreKind.values().toList()) { kind ->
            val fnBits = when (kind) {
                StoreKind.B -> 0b000_000000000000u
                StoreKind.H -> 0b001_000000000000u
                StoreKind.W -> 0b010_000000000000u
            }

            withData(
                InstructionDecoding(
                    0b0000000_00000_00000_000_00000_0100011u or fnBits,
                    Store(kind, XRegister(0), XRegister(0), 0u)
                ),
                InstructionDecoding(
                    0b0000000_10100_00101_000_00000_0100011u or fnBits,
                    Store(kind, XRegister(5), XRegister(20), 0u)
                ),
                InstructionDecoding(
                    0b0000001_10100_00101_000_01010_0100011u or fnBits,
                    Store(kind, XRegister(5), XRegister(20), 42u)
                ),
                InstructionDecoding(
                    0b1111110_10100_00101_000_10110_0100011u or fnBits,
                    Store(kind, XRegister(5), XRegister(20), (-42).toUInt())
                ),
            ) { (word: Word, expected: Instruction) ->
                decoder.decodeInstruction(word) shouldBe expected
            }
        }
    }

    describe("Binary operation") {
        withData(BinaryOpKind.values().toList()) { kind ->
            val fnBits = when (kind) {
                BinaryOpKind.ADD -> 0b0000000_0000000000_000_000000000000u
                BinaryOpKind.SUB -> 0b0100000_0000000000_000_000000000000u
                BinaryOpKind.AND -> 0b0000000_0000000000_111_000000000000u
                BinaryOpKind.OR -> 0b0000000_0000000000_110_000000000000u
                BinaryOpKind.XOR -> 0b0000000_0000000000_100_000000000000u
                BinaryOpKind.SLL -> 0b0000000_0000000000_001_000000000000u
                BinaryOpKind.SRL -> 0b0000000_0000000000_101_000000000000u
                BinaryOpKind.SRA -> 0b0100000_0000000000_101_000000000000u
                BinaryOpKind.MUL -> 0b0000001_0000000000_000_000000000000u
                BinaryOpKind.MULH -> 0b0000001_0000000000_001_000000000000u
                BinaryOpKind.MULHU -> 0b0000001_0000000000_011_000000000000u
                BinaryOpKind.MULHSU -> 0b0000001_0000000000_010_000000000000u
                BinaryOpKind.DIV -> 0b0000001_0000000000_100_000000000000u
                BinaryOpKind.DIVU -> 0b0000001_0000000000_101_000000000000u
                BinaryOpKind.REM -> 0b0000001_0000000000_110_000000000000u
                BinaryOpKind.REMU -> 0b0000001_0000000000_111_000000000000u
            }

            withData(
                InstructionDecoding(
                    0b0000000_00000_00000_000_00000_0110011u or fnBits,
                    BinaryOp(kind, XRegister(0), XRegister(0), XRegister(0))
                ),
                InstructionDecoding(
                    0b0000000_10100_00101_000_00100_0110011u or fnBits,
                    BinaryOp(kind, XRegister(4), XRegister(5), XRegister(20))
                ),
            ) { (word: Word, expected: Instruction) ->
                decoder.decodeInstruction(word) shouldBe expected
            }

            if (kind in setOf(BinaryOpKind.ADD, BinaryOpKind.AND, BinaryOpKind.OR, BinaryOpKind.XOR)) {
                withData(
                    InstructionDecoding(
                        0b000000000000_00000_000_00000_0010011u or fnBits,
                        BinaryOpImmediate(kind, XRegister(0), XRegister(0), 0u)
                    ),
                    InstructionDecoding(
                        0b000000000000_10100_000_00101_0010011u or fnBits,
                        BinaryOpImmediate(kind, XRegister(5), XRegister(20), 0u)
                    ),
                    InstructionDecoding(
                        0b000000101010_10100_000_00101_0010011u or fnBits,
                        BinaryOpImmediate(kind, XRegister(5), XRegister(20), 42u)
                    ),
                    InstructionDecoding(
                        0b111111010110_10100_000_00101_0010011u or fnBits,
                        BinaryOpImmediate(kind, XRegister(5), XRegister(20), (-42).toUInt())
                    ),
                ) { (word: Word, expected: Instruction) ->
                    decoder.decodeInstruction(word) shouldBe expected
                }
            }

            if (kind in setOf(BinaryOpKind.SLL, BinaryOpKind.SRL, BinaryOpKind.SRA)) {
                withData(
                    InstructionDecoding(
                        0b0000000_00000_00000_000_00000_0010011u or fnBits,
                        BinaryOpImmediate(kind, XRegister(0), XRegister(0), 0u)
                    ),
                    InstructionDecoding(
                        0b0000000_00000_10100_000_00101_0010011u or fnBits,
                        BinaryOpImmediate(kind, XRegister(5), XRegister(20), 0u)
                    ),
                    InstructionDecoding(
                        0b0000000_01010_10100_000_00101_0010011u or fnBits,
                        BinaryOpImmediate(kind, XRegister(5), XRegister(20), 10u)
                    ),
                    InstructionDecoding(
                        0b0000000_11111_10100_000_00101_0010011u or fnBits,
                        BinaryOpImmediate(kind, XRegister(5), XRegister(20), 31u)
                    ),
                ) { (word: Word, expected: Instruction) ->
                    decoder.decodeInstruction(word) shouldBe expected
                }
            }
        }
    }

    describe("SLT") {
        withData(
            InstructionDecoding(
                0b0000000_00000_00000_010_00000_0110011u,
                SetIfLessThan(true, XRegister(0), XRegister(0), XRegister(0))
            ),
            InstructionDecoding(
                0b0000000_10100_00101_010_00100_0110011u,
                SetIfLessThan(true, XRegister(4), XRegister(5), XRegister(20))
            ),
        ) { (word: Word, expected: Instruction) ->
            decoder.decodeInstruction(word) shouldBe expected
        }
    }

    describe("SLTU") {
        withData(
            InstructionDecoding(
                0b0000000_00000_00000_011_00000_0110011u,
                SetIfLessThan(false, XRegister(0), XRegister(0), XRegister(0))
            ),
            InstructionDecoding(
                0b0000000_10100_00101_011_00100_0110011u,
                SetIfLessThan(false, XRegister(4), XRegister(5), XRegister(20))
            ),
        ) { (word: Word, expected: Instruction) ->
            decoder.decodeInstruction(word) shouldBe expected
        }
    }

    describe("SLTI") {
        withData(
            InstructionDecoding(
                0b0000000_00000_00000_010_00000_0110011u,
                SetIfLessThan(true, XRegister(0), XRegister(0), XRegister(0))
            ),
            InstructionDecoding(
                0b0000000_10100_00101_010_00100_0110011u,
                SetIfLessThan(true, XRegister(4), XRegister(5), XRegister(20))
            ),
            InstructionDecoding(
                0b000000101010_10100_010_00101_0010011u,
                SetIfLessThanImmediate(true, XRegister(5), XRegister(20), 42u)
            ),
            InstructionDecoding(
                0b111111010110_10100_010_00101_0010011u,
                SetIfLessThanImmediate(true, XRegister(5), XRegister(20), (-42).toUInt())
            ),
        ) { (word: Word, expected: Instruction) ->
            decoder.decodeInstruction(word) shouldBe expected
        }
    }

    describe("SLTIU") {
        withData(
            InstructionDecoding(
                0b0000000_00000_00000_011_00000_0110011u,
                SetIfLessThan(false, XRegister(0), XRegister(0), XRegister(0))
            ),
            InstructionDecoding(
                0b0000000_10100_00101_011_00100_0110011u,
                SetIfLessThan(false, XRegister(4), XRegister(5), XRegister(20))
            ),
            InstructionDecoding(
                0b000000101010_10100_011_00101_0010011u,
                SetIfLessThanImmediate(false, XRegister(5), XRegister(20), 42u)
            ),
            InstructionDecoding(
                0b111111010110_10100_011_00101_0010011u,
                SetIfLessThanImmediate(false, XRegister(5), XRegister(20), 0b111111010110u)
            ),
        ) { (word: Word, expected: Instruction) ->
            decoder.decodeInstruction(word) shouldBe expected
        }
    }
})

data class InstructionDecoding(val word: Word, val instruction: Instruction): WithDataTestName {
    override fun dataTestName(): String {
        val disassembly = instruction.disassembly()
        return word.toString(16).padStart(8, '0') + " = " +
                disassembly.mnemonic + " " +
                disassembly.args.joinToString(", ")
    }
}
