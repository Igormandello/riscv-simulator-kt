package br.unicamp.riscv.simulator.model

import io.kotest.core.spec.style.DescribeSpec
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
            Triple(0u, 0..31, 0u),
            Triple(0xFFFFFFFFu, 0..31, 0xFFFFFFFFu),
            Triple(0u, 0..7, 0u),
            Triple(0xFFFFFFFFu, 0..7, 0xFFu),
            Triple(0u, 8..31, 0u),
            Triple(0xFFFFFFFFu, 8..31, 0xFFFFFFu),
            Triple(0b10101010101010101010101010101010u, 0..3, 0b1010u),
            Triple(0b10101010101010101010101010101010u, 1..4, 0b0101u),
            Triple(0b10101010101010101010101010101010u, 0..0, 0b0u),
            Triple(0b10101010101010101010101010101010u, 1..1, 0b1u)
        ) { (word: Word, range: IntRange, expected: Word) ->
            word.bitRange(range) shouldBe expected
        }
    }

    describe("Patch bits") {
        withData(
            Triple(0u, listOf(0..31), 0u),
            Triple(0xFFFFFFFFu, listOf(0..31), 0xFFFFFFFFu),
            Triple(0b111001u, listOf(4..5, 2..3, 0..1), 0b011011u),
            Triple(0b11110000u, listOf(4..7, 0..3), 0b00001111u)
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
