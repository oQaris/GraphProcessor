package console

import picocli.CommandLine

class PrintExceptionMessageHandler : CommandLine.IExecutionExceptionHandler {
    override fun handleExecutionException(
        ex: Exception,
        cmd: CommandLine,
        parseResult: CommandLine.ParseResult
    ): Int {
        cmd.err.println(cmd.colorScheme.errorText(ex.message))
        return if (cmd.exitCodeExceptionMapper != null)
            cmd.exitCodeExceptionMapper.getExitCode(ex)
        else cmd.commandSpec.exitCodeOnExecutionException()
    }
}
