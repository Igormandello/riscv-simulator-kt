package br.unicamp.riscv.simulator.model

data class Disassembly(val mnemonic: String, val args: List<String>) {
    constructor(mnemonic: String, vararg args: String) : this(mnemonic, args.toList())
    constructor(mnemonic: String, vararg args: Any) : this(mnemonic, args.map(Any::toString))
}
