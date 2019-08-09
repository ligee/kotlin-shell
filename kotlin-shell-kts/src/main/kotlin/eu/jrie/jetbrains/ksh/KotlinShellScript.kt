package eu.jrie.jetbrains.ksh

import eu.jrie.jetbrains.kotlinshell.shell.DEFAULT_PIPELINE_CHANNEL_BUFFER_SIZE
import eu.jrie.jetbrains.kotlinshell.shell.DEFAULT_PIPELINE_RW_PACKET_SIZE
import eu.jrie.jetbrains.kotlinshell.shell.DEFAULT_SYSTEM_PROCESS_INPUT_STREAM_BUFFER_SIZE
import eu.jrie.jetbrains.kotlinshell.shell.ScriptingShell
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.kotlin.mainKts.MainKtsConfigurator
import org.jetbrains.kotlin.script.util.DependsOn
import org.jetbrains.kotlin.script.util.Import
import org.jetbrains.kotlin.script.util.Repository
import java.io.File
import java.util.Collections.emptyMap
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptAcceptedLocation
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.api.refineConfigurationBeforeEvaluate
import kotlin.script.experimental.api.scriptsInstancesSharing
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.jsr223.configureProvidedPropertiesFromJsr223Context
import kotlin.script.experimental.jvmhost.jsr223.importAllBindings
import kotlin.script.experimental.jvmhost.jsr223.jsr223

@Suppress("unused")
@ExperimentalCoroutinesApi
@KotlinScript(
    fileExtension = "sh.kts",
    compilationConfiguration = KotlinShellScriptConfiguration::class,
    evaluationConfiguration = KotlinShellScriptEvaluationConfiguration::class
)
open class KotlinShellScript (
    val args: Array<String>
) : ScriptingShell(
    emptyMap(),
    File(System.getProperty("user.dir")),
    envOrDefault(SYSTEM_PROCESS_INPUT_STREAM_BUFFER_SIZE_ENV, DEFAULT_SYSTEM_PROCESS_INPUT_STREAM_BUFFER_SIZE),
    envOrDefault(PIPELINE_RW_PACKET_SIZE_ENV, DEFAULT_PIPELINE_RW_PACKET_SIZE.toInt()).toLong(),
    envOrDefault(PIPELINE_CHANNEL_BUFFER_SIZE_ENV, DEFAULT_PIPELINE_CHANNEL_BUFFER_SIZE)
) {
    companion object {
        const val SYSTEM_PROCESS_INPUT_STREAM_BUFFER_SIZE_ENV = "SH_KTS_SYSTEM_PROCESS_INPUT_STREAM_BUFFER_SIZE"
        const val PIPELINE_RW_PACKET_SIZE_ENV = "SH_KTS_PIPELINE_RW_PACKET_SIZE"
        const val PIPELINE_CHANNEL_BUFFER_SIZE_ENV = "SH_KTS_PIPELINE_CHANNEL_BUFFER_SIZE"

        private fun envOrDefault(env: String, default: Int): Int = envOfNull(env) ?: default
        private fun envOfNull(env: String): Int? = runCatching { System.getenv(env).toInt() } .getOrNull()
    }
}

class KotlinShellScriptConfiguration : ScriptCompilationConfiguration (
    {
        defaultImports(DependsOn::class, Repository::class, Import::class)
        jvm {
            dependenciesFromClassContext(KotlinShellScriptConfiguration::class, "kotlin-main-kts", "kotlin-stdlib", "kotlin-reflect")
        }
        refineConfiguration {
            onAnnotations(DependsOn::class, Repository::class, Import::class, handler = MainKtsConfigurator())
        }
        ide {
            acceptedLocations(ScriptAcceptedLocation.Everywhere)
        }
        jsr223 {
            importAllBindings(true)
        }
    }
)

class KotlinShellScriptEvaluationConfiguration : ScriptEvaluationConfiguration (
    {
        scriptsInstancesSharing(true)
        refineConfigurationBeforeEvaluate(::configureProvidedPropertiesFromJsr223Context)
    }
)