@file:Suppress("EXPERIMENTAL_API_USAGE")

package eu.jrie.jetbrains.kotlinshell.shell

import eu.jrie.jetbrains.kotlinshell.processes.execution.ExecutionContext
import eu.jrie.jetbrains.kotlinshell.processes.execution.ProcessExecutionContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.io.core.buildPacket
import kotlinx.io.core.writeText
import java.io.File

typealias ShellCommand = suspend (ExecutionContext) -> Unit

@Suppress("PropertyName")
@ExperimentalCoroutinesApi
interface ShellBase : ProcessExecutionContext {

    /**
     * [CoroutineScope] used by this shell, its members and sub shells
     */
    val scope: CoroutineScope

    /**
     * Environment of this shell.
     * These variables are being inherited to sub shells.
     *
     * @see [variables]
     */
    val environment: Map<String, String>
    /**
     * Variables of this shell.
     * These variables are not being inherited to sub shells.
     *
     * @see [environment]
     */
    val variables: Map<String, String>
    /**
     * Current directory of this shell
     */
    val directory: File

    /**
     * Creates command, that can be piped or executed inside shell
     */
    fun command(block: suspend ShellBase.() -> String): ShellCommand = {
        it.stdout.send(
            buildPacket { writeText(this@ShellBase.block()) }
        )
    }

    suspend operator fun ShellCommand.invoke() = this.invoke(this@ShellBase)

    suspend fun finalize()

    fun closeOut() {
        stdout.close()
        stderr.close()
    }

    companion object {
        const val SYSTEM_PROCESS_INPUT_STREAM_BUFFER_SIZE = "SYSTEM_PROCESS_INPUT_STREAM_BUFFER_SIZE"
        const val PIPELINE_RW_PACKET_SIZE = "PIPELINE_RW_PACKET_SIZE"
        const val PIPELINE_CHANNEL_BUFFER_SIZE = "PIPELINE_CHANNEL_BUFFER_SIZE"

        const val DEFAULT_SYSTEM_PROCESS_INPUT_STREAM_BUFFER_SIZE: Int = 512
        const val DEFAULT_PIPELINE_RW_PACKET_SIZE: Long = 256
        const val DEFAULT_PIPELINE_CHANNEL_BUFFER_SIZE: Int = 16
    }

}
