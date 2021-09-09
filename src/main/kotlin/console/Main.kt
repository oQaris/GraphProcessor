package console

import picocli.CommandLine
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val exitCode = CommandLine(BaseCommand())
        .setExecutionExceptionHandler(PrintExceptionMessageHandler())
        .execute(*args)
    exitProcess(exitCode)
}
