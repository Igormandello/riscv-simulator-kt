package br.unicamp.riscv.simulator.model

typealias Word = UInt

operator fun Word.get(i: Int): UByte = (this shr (i * 8)).toUByte()
fun Word.bitRange(range: IntRange): Word {
    val leadingBits = 31 - range.last
    return (this shl leadingBits) shr (leadingBits + range.first)
}
