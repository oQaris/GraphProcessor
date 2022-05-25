package console

import picocli.CommandLine
import picocli.jansi.graalvm.AnsiConsole
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val exitCode: Int
    AnsiConsole.windowsInstall().use {
        exitCode = CommandLine(BaseCommand())
            .setUsageHelpAutoWidth(true)
            .setAbbreviatedOptionsAllowed(true)
            .setAbbreviatedSubcommandsAllowed(true)
            .setExecutionExceptionHandler(PrintExceptionMessageHandler())
            .execute(*args)
    }
    exitProcess(exitCode)
}
