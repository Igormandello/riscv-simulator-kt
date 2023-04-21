package br.unicamp.riscv.simulator.hardware

import java.util.SortedMap
import java.util.TreeMap

class Memory {
    private val _data: SortedMap<UInt, UByte> = sortedMapOf()

    operator fun get(address: UInt): UByte = _data[address] ?: 0u

    operator fun set(address: UInt, value: UByte) {
        _data[address] = value
    }

    fun loadWord(address: UInt): UInt = loadBytes(address, 4).pack()
    fun loadShort(address: UInt): UShort = loadBytes(address, 2).pack().toUShort()
    fun loadByte(address: UInt): UByte = get(address)

    fun loadBytes(address: UInt, n: Int): Array<UByte> {
        val bytes = Array<UByte>(n) { 0u }
        val last = address + n.toUInt()
        val found = _data.tailMap(address).asSequence().take(n).takeWhile { (k, _) -> k <= last }
        for ((k, byte) in found) {
            bytes[(k - address).toInt()] = byte
        }
        return bytes
    }

    fun storeWord(address: UInt, word: UInt) = storeBytes(address, word.unpack())
    fun storeShort(address: UInt, short: UShort) = storeBytes(address, short.unpack())
    fun storeByte(address: UInt, byte: UByte) = set(address, byte)

    fun storeBytes(baseAddress: UInt, bytes: Array<UByte>) {
        val pairs = bytes.asSequence().mapIndexed { i, byte -> baseAddress + i.toUInt() to byte }
        val toAdd = TreeMap<UInt, UByte>()
        toAdd.putAll(pairs)
        _data.putAll(toAdd)
    }

    private fun Array<UByte>.pack(): UInt = foldRight(0u) { byte, acc -> (acc shl 8) or byte.toUInt() }

    private fun UInt.unpack(): Array<UByte> =
        arrayOf(
            toUByte(),
            shr(8).toUByte(),
            shr(16).toUByte(),
            shr(24).toUByte()
        )

    private fun UShort.unpack(): Array<UByte> =
        arrayOf(
            toUByte(),
            (toUInt() shr 8).toUByte()
        )
}
