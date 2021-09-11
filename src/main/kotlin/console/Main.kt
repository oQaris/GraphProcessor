package console

import picocli.CommandLine
import picocli.jansi.graalvm.AnsiConsole
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val encoding = System.setProperty("console.encoding", "utf-8")
    println("Запускаемся!")
    println(encoding)
    val exitCode: Int
    AnsiConsole.windowsInstall().use {
        exitCode = CommandLine(BaseCommand())
            //.setOut(PrintWriter(PrintStream(System.out)))
            .setAbbreviatedOptionsAllowed(true)
            .setAbbreviatedSubcommandsAllowed(true)
            .setExecutionExceptionHandler(PrintExceptionMessageHandler())
            .execute(*args)
    }
    exitProcess(exitCode)
}
