package br.unicamp.riscv.simulator.model

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.uInt
import io.kotest.property.checkAll

class WordTest : DescribeSpec({
    describe("Bit range") {
        withData(
            BitRangeTriple(0u, 0..31, 0u),
            BitRangeTriple(0xFFFFFFFFu, 0..31, 0xFFFFFFFFu),
            BitRangeTriple(0u, 0..7, 0u),
            BitRangeTriple(0xFFFFFFFFu, 0..7, 0xFFu),
            BitRangeTriple(0u, 8..31, 0u),
            BitRangeTriple(0xFFFFFFFFu, 8..31, 0xFFFFFFu),
            BitRangeTriple(0b10101010101010101010101010101010u, 0..3, 0b1010u),
            BitRangeTriple(0b10101010101010101010101010101010u, 1..4, 0b0101u),
            BitRangeTriple(0b10101010101010101010101010101010u, 0..0, 0b0u),
            BitRangeTriple(0b10101010101010101010101010101010u, 1..1, 0b1u)
        ) { (word: Word, range: IntRange, expected: Word) ->
            word.bitRange(range) shouldBe expected
        }
    }

    describe("Patch bits") {
        withData(
            PatchBitsTriple(0u, listOf(0..31), 0u),
            PatchBitsTriple(0xFFFFFFFFu, listOf(0..31), 0xFFFFFFFFu),
            PatchBitsTriple(0b111001u, listOf(4..5, 2..3, 0..1), 0b011011u),
            PatchBitsTriple(0b11110000u, listOf(4..7, 0..3), 0b00001111u)
        ) { (word: Word, ranges: List<IntRange>, expected: Word) ->
            word.patch(*ranges.toTypedArray()) shouldBe expected
        }
    }

    describe("Sign extension") {
        it("keeps positive numbers positive") {
            checkAll(Arb.uInt(), Arb.int(1..31)) { n, bitSize ->
                val mask = Word.MAX_VALUE shr (Word.SIZE_BITS - bitSize)
                val word = n and mask

                assert(word.countLeadingZeroBits() > 0)

                val sext = word.signExtend(bitSize + 1)

                sext.countLeadingZeroBits() shouldBeGreaterThan  0
                sext shouldBe word
                sext.toInt() shouldBeGreaterThanOrEqual 0
            }
        }

        it("keeps negative numbers negative") {
            checkAll(Arb.uInt(), Arb.int(1..31)) { n, bitSize ->
                val mask = Word.MAX_VALUE shr (Word.SIZE_BITS - bitSize)
                val signBit = 1u shl bitSize
                val word = (n and mask)
                val signed = word or signBit

                val sext = signed.signExtend(bitSize + 1)

                sext.countLeadingZeroBits() shouldBe 0
                sext shouldBe word - signBit
                sext.toInt() shouldBeLessThanOrEqual 0
            }
        }
    }
})

data class BitRangeTriple(val word: Word, val range: IntRange, val expected: Word): WithDataTestName {
    override fun dataTestName(): String {
        val wordBinary = word.toString(2).padStart(32, '0')
        val expectedBinary = expected.toString(2).padStart(range.last - range.first + 1, '0')
        return "$wordBinary [${range.last}:${range.first}] = $expectedBinary"
    }
}

data class PatchBitsTriple(val word: Word, val ranges: List<IntRange>, val expected: Word): WithDataTestName {
    override fun dataTestName(): String {
        val wordBinary = word.toString(2).padStart(32, '0')
        val expectedLength = ranges.sumOf { it.last - it.first + 1 }
        val expectedBinary = expected.toString(2).padStart(expectedLength, '0')
        val patches = ranges.asReversed().joinToString("|") { "${it.last}:${it.first}" }
        return "$wordBinary [$patches] = $expectedBinary"
    }
}
