package br.unicamp.riscv.simulator.loader

import br.unicamp.riscv.simulator.hardware.Memory
import br.unicamp.riscv.simulator.model.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.fornwall.jelf.ElfFile
import net.fornwall.jelf.ElfSegment
import java.nio.file.Path

class ProgramLoader {
    suspend fun loadProgram(path: Path, memory: Memory): Word {
        val file = path.toFile()
        val bytes = withContext(Dispatchers.IO) { file.readBytes() }
        val elf = ElfFile.from(bytes)

        require(elf.e_type.toInt() != ElfFile.ET_EXEC) { "Program must be executable" }
        require(elf.ei_class == ElfFile.CLASS_32) { "Program must be a 32-bit object" }
        require(elf.ei_data == ElfFile.DATA_LSB) { "Program file must be a little-endian" }
        require(elf.e_machine == MACHINE_RISCV) { "Program must target RISC-V machine" }
        require(elf.e_entry != 0L) { "Program must have an entrypoint" }

        for (i in 0 until elf.e_phnum) {
            val header = elf.getProgramHeader(i)

            val fileSize = header.p_filesz.toInt()
            val start = header.p_offset.toInt()
            val end = start + fileSize

            if (header.p_type != ElfSegment.PT_LOAD) {
                logger.debug {
                    "Ignoring ELF program header of type ${elfHeaderType(header.p_type)} at " +
                    "0x${start.toString(16)}..0x${end.toString(16)}"
                }
                continue
            }

            val targetVirtualAddress = header.p_vaddr.toUInt()

            val sectionBytes = bytes.sliceArray(start..end).toUByteArray().toTypedArray()

            val align = header.p_align.toUInt()
            val memAddress = (targetVirtualAddress / align) * align

            memory.storeBytes(memAddress, sectionBytes)
        }

        val entrypoint = elf.e_entry.toUInt()
        logger.debug { "Program entrypoint: 0x${entrypoint.toString(16)}" }

        return entrypoint
    }

    private fun elfHeaderType(type: Int) = ELF_HEADER_TYPES[type] ?: "0x${type.toString(16)}"

    companion object {
        private const val MACHINE_RISCV: Short = 0xF3

        private val ELF_HEADER_TYPES = mapOf(
            0x00000000 to "PT_NULL",
            0x00000001 to "PT_LOAD",
            0x00000002 to "PT_DYNAMIC",
            0x00000003 to "PT_INTERP",
            0x00000004 to "PT_NOTE",
            0x00000005 to "PT_SHLIB",
            0x00000006 to "PT_PHDR",
            0x00000007 to "PT_TLS",
            0x60000000 to "PT_LOOS",
            0x6FFFFFFF to "PT_HIOS",
            0x70000000 to "PT_LOPROC",
            0x7FFFFFFF to "PT_HIPROC"
        )

        private val logger = KotlinLogging.logger {}
    }
}
