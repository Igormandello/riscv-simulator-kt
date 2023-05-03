package br.unicamp.riscv.simulator.log

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.MDC
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString

suspend fun <T> withLogContext(path: Path, fn: suspend CoroutineScope.() -> T) {
    MDC.put("simulationName", path.resolveSibling(path.nameWithoutExtension).pathString)
    withContext(MDCContext(), fn)
    MDC.clear()
}
