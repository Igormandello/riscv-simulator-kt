package br.unicamp.riscv.simulator.hardware

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.PropTestConfig
import io.kotest.property.assume
import io.kotest.property.checkAll
import io.kotest.property.forAll

class MemoryTest : DescribeSpec({
    describe("Memory is persistent") {
        it("persists bytes") {
            forAll<UInt, UByte> { address, byte ->
                val mem = Memory()
                mem.storeByte(address, byte)

                mem.loadByte(address) == byte
            }
        }

        it("persists shorts") {
            forAll<UInt, UShort>(PropTestConfig(maxDiscardPercentage = 55)) { address, short ->
                assume(address % 2u == 0u)
                assume(address <= UInt.MAX_VALUE - 2u)

                val mem = Memory()
                mem.storeShort(address, short)
                mem.loadShort(address) == short
            }
        }

        it("persists words") {
            forAll<UInt, UInt>(PropTestConfig(maxDiscardPercentage = 80)) { address, word ->
                assume(address % 4u == 0u)
                assume(address <= UInt.MAX_VALUE - 4u)

                val mem = Memory()
                mem.storeWord(address, word)

                mem.loadWord(address) == word
            }
        }

        it("persists byte arrays") {
            forAll<UInt, List<UByte>> { address, bytes ->
                assume(address <= UInt.MAX_VALUE - bytes.size.toUInt())

                val mem = Memory()
                mem.storeBytes(address, bytes.toTypedArray())

                mem.loadBytes(address, bytes.size).toList() == bytes
            }
        }

        it("does not mix addresses (byte)") {
            forAll<UInt, UByte, UInt, UByte> { address1, byte1, address2, byte2 ->
                assume(address1 != address2)

                val mem = Memory()
                mem.storeByte(address1, byte1)
                mem.storeByte(address2, byte2)

                mem.loadByte(address1) == byte1
            }
        }

        it("does not mix addresses (short)") {
            forAll<UInt, UShort, UInt, UShort>(PropTestConfig(maxDiscardPercentage = 80)) { address1, short1, address2, short2 ->
                assume(address1 != address2)
                assume(address1 % 2u == 0u)
                assume(address2 % 2u == 0u)
                assume(address1 <= UInt.MAX_VALUE - 2u)
                assume(address2 <= UInt.MAX_VALUE - 2u)

                val mem = Memory()
                mem.storeShort(address1, short1)
                mem.storeShort(address2, short2)

                mem.loadShort(address1) == short1
            }
        }

        it("does not mix addresses (word)") {
            forAll<UInt, UInt, UInt, UInt>(PropTestConfig(maxDiscardPercentage = 95)) { address1, word1, address2, word2 ->
                assume(address1 != address2)
                assume(address1 % 4u == 0u)
                assume(address2 % 4u == 0u)
                assume(address1 <= UInt.MAX_VALUE - 4u)
                assume(address2 <= UInt.MAX_VALUE - 4u)

                val mem = Memory()
                mem.storeWord(address1, word1)
                mem.storeWord(address2, word2)

                mem.loadWord(address1) == word1
            }
        }
    }

    describe("Memory endianness") {
        it("is little endian") {
            checkAll<UInt, UInt>(PropTestConfig(maxDiscardPercentage = 80)) { address, word ->
                assume(address % 4u == 0u)
                assume(address <= UInt.MAX_VALUE - 4u)

                val mem = Memory()
                mem.storeWord(address, word)

                mem.loadShort(address) shouldBeEqual word.toUShort()
                mem.loadShort(address + 2u) shouldBeEqual (word shr 16).toUShort()

                mem.loadByte(address) shouldBeEqual word.toUByte()
                mem.loadByte(address + 1u) shouldBeEqual (word shr 8).toUByte()
                mem.loadByte(address + 2u) shouldBeEqual (word shr 16).toUByte()
                mem.loadByte(address + 3u) shouldBeEqual (word shr 24).toUByte()
            }
        }
    }
})
