package console.algorithm.clustering

import algorithm.thesis.Event
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import graphs.Graph
import graphs.impl.AdjacencyMatrixGraph
import storage.SetFileGraph

class GamsModel {
    private val template = """
        Set i "num vertex" / 1*%d /;
        Alias (i, j, k)

        Table y(i,j) "input graph clustering"
        %s ;

        Binary Variable x(i,j) "result";
        Variable distance "objective";

        Equations
           allow(i,j,k) "triangle inequality"
           sim(i,j) "symmetric matrix"
           diag(i,i) "diagonal matrix"
           size(i) "cluster size"
           obj "total distance";

        allow(i,j,k)${'$'}(ord(i)<>ord(j) and ord(j)<>ord(k) and ord(k)<>ord(i)).. x(i,k) + x(i,j) =l= x(j,k) + 1;

        sim(i,j)${'$'}(ord(i)<ord(j)).. x(i,j) =e= x(j,i);

        diag(i,i).. x(i,i) =e= 0;

        size(i).. sum(j, x(i,j)) =l= %d;

        obj.. distance =e= sum((i,j), abs(x(i,j) - y(i,j)));

        Model clustering / all /;
        Solve clustering minimizing distance using MINLP;

        Option x:0;
        Display "Result graph", x.l, "Objective value", clustering.objval;
    """.trimIndent()

    private val workdir = "remote"
    private val tmpFile = "tmp.gms"
    private lateinit var session: Session

    fun connect() {
        val ipAddress = "217.79.52.72"
        val port = 6005
        val username = "oqaris"
        val privateKeyPath = "C:\\Users\\oQaris\\oqaris.rsa"
        val password = System.getenv("PASS_SSH")

        val jsch = JSch()
        jsch.addIdentity(privateKeyPath, password)

        session = jsch.getSession(username, ipAddress, port)
        session.setConfig("StrictHostKeyChecking", "no")
        session.connect()
    }

    fun clustering(
        base: Graph,
        maxSizeCluster: Int,
        driver: (Event) -> Unit = {}
    ): Graph {

        exec(session, "rm $tmpFile")
        driver.invoke(Event.ON)

        createTmpGamsInput(base, maxSizeCluster)

        val out = startGamsCalculating()
        driver.invoke(Event.OFF)

        return parseResultGraph(out, base.numVer)
    }

    private fun createTmpGamsInput(graph: Graph, k: Int) {
        val codeGms = String.format(template, graph.numVer, graphToTableGms(graph), k - 1)
        exec(session, "echo \'$codeGms\' > $tmpFile")
    }

    private fun startGamsCalculating(): String {
        val reportFile = tmpFile.dropLast(3) + "lst"
        exec(session, "rm $reportFile")

        exec(session, "faketime '2021-01-01 00:00:00' /opt/gams/gams $tmpFile")

        return exec(session, "cat $reportFile")
    }

    private fun exec(session: Session, command: String): String {
        val channel: ChannelExec = session.openChannel("exec") as ChannelExec
        channel.setCommand("cd $workdir && $command")
        channel.connect()
        return channel.inputStream.bufferedReader().lineSequence().joinToString("\n")
    }

    fun graphToTableGms(graph: Graph): String {
        val pad = graph.numVer.toString().length + 1
        fun Int.pad() = toString().padEnd(pad)

        val buf = StringBuilder()
        buf.append(" ".repeat(pad + 4) + (1..graph.numVer)
            .joinToString(" ") { it.pad() } + " \n")

        for (i in 0 until graph.numVer) {
            buf.append("   ${(i + 1).pad()} ")
            for (j in 0 until graph.numVer) {
                val item = if (graph.isCom(i, j)) 1 else 0
                buf.append(item.pad()).append(" ")
            }
            buf.append("\n")
        }
        return buf.toString()
    }

    fun parseResultGraph(out: String, n: Int): Graph {
        val digRegExp = "\\d+".toRegex()
        val prefix = "----\\s+\\d+\\s+VARIABLE\\s+x.L\\s+result".toRegex()
        val suffix = "----\\s+\\d+\\s+Objective value".toRegex()

        val start = prefix.findAll(out).lastOrNull()?.range?.last ?: throw IllegalStateException("no solve!")
        val end = suffix.findAll(out).lastOrNull()?.range?.first ?: throw IllegalStateException("no solve!")

        val rows = out.substring(start + 1, end)
            .split("\n")
            .filter { it.isNotBlank() }

        val idxMap = digRegExp.findAll(rows[0])
            .map { it.range.last to it.value.toInt() }
            .toMap()

        val graph = AdjacencyMatrixGraph("result", n)
        rows.drop(1).forEach { row ->

            val from = digRegExp.find(row)!!.value.toInt()
            val tos = idxMap.filter { it.key < row.length && row[it.key] == '1' }
                .map { it.value }

            tos.forEach {
                graph.addEdg(from - 1, it - 1)
            }
        }
        return graph
    }

    fun disconnect() {
        session.disconnect()
    }
}

fun main() {
    val model = GamsModel()
    val graph = SetFileGraph()["12_6"]
    println(graph)
    try {
        model.connect()
        println(model.clustering(graph, 3))
    } finally {
        model.disconnect()
    }
}
