package br.unicamp.riscv.simulator.hardware

import br.unicamp.riscv.simulator.model.PC
import br.unicamp.riscv.simulator.model.XRegister
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.uInt
import io.kotest.property.assume
import io.kotest.property.forAll

class RegisterFileTest : DescribeSpec({
    val arbitraryRegister =
        Arb.int(-1..31).map {
            if (it == -1) {
                PC
            } else {
                XRegister(it)
            }
        }

    describe("Register file is persistent") {
        it("persists values") {
            forAll(arbitraryRegister, Arb.uInt()) { reg, value ->
                assume(reg != XRegister(0))

                val file = RegisterFile()
                file[reg] = value
                file[reg] == value
            }
        }

        it("does not mix registers") {
            forAll(arbitraryRegister, Arb.uInt(), arbitraryRegister, Arb.uInt()) { reg1, value1, reg2, value2 ->
                assume(reg1 != XRegister(0))
                assume(reg1 != reg2)

                val file = RegisterFile()
                file[reg1] = value1
                file[reg2] = value2
                file[reg1] == value1
            }
        }
    }

    describe("Register zero is constant") {
        it("is zero") {
            val file = RegisterFile()
            file[XRegister(0)] shouldBe 0u
        }

        it("ignores assignments") {
            forAll<UInt> { value ->
                val file = RegisterFile()
                file[XRegister(0)] = value
                file[XRegister(0)] == 0u
            }
        }
    }
})
