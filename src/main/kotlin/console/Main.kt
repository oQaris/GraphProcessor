package console

import picocli.CommandLine
import picocli.jansi.graalvm.AnsiConsole
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    System.setProperty("console.encoding", "utf-8")
    println("Запускаемся!")
    val exitCode: Int
    AnsiConsole.windowsInstall().use {
        exitCode = CommandLine(BaseCommand())
            .setExecutionExceptionHandler(PrintExceptionMessageHandler())
            .execute(*args)
    }
    exitProcess(exitCode)
}
