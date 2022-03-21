package storage

import graphs.EdgeListGraph
import graphs.Graph

private val noEdgeTokens = setOf("-" /*"0"*/)
private val pattern = "(-?\\d+)|${noEdgeTokens.joinToString("|")}".toRegex()
private var id = 0

fun parse(
    name: String = "unnamed_$id",
    input: String,
    implementation: (String, List<List<Int?>>) -> Graph = ::EdgeListGraph,
) = implementation(name,
    input.split("\n")
        .filter { it.isNotBlank() }
        .map { row ->
            pattern.findAll(row).map {
                if (noEdgeTokens.contains(it.value)) null
                else it.value.toInt()
            }.toList()
        }).also { id++ }

fun standardToString(graph: Graph): String {
    val sb = StringBuilder()
    sb.append(":").append(graph.name).append(":")
    sb.append(System.lineSeparator())
    for (i in 0 until graph.numVer) {
        for (j in 0 until graph.numVer)
            sb.append(
                if (graph.isCom(i, j))
                    graph.getWeightEdg(i, j).toString()
                else "-"
            ).append(" ")
        sb.append(System.lineSeparator())
    }
    return sb.toString()
}