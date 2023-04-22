package br.unicamp.riscv.simulator.model

typealias Word = UInt

operator fun Word.get(i: Int): UByte = (this shr (i * 8)).toUByte()

fun Word.bitRange(range: IntRange): Word {
    val leadingBits = 31 - range.last
    return (this shl leadingBits) shr (leadingBits + range.first)
}

fun Word.patch(vararg ranges: IntRange): Word {
    var ans = 0u
    var shift = 0
    for (range in ranges) {
        ans = ans or (bitRange(range) shl shift)
        shift += range.last - range.first + 1
    }
    return ans
}

fun Word.signExtend(bitSize: Int): Word {
    val d = Word.SIZE_BITS - bitSize
    return ((this shl d).toInt() shr d).toUInt()
}
