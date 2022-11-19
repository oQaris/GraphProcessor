package storage

import graphs.Graph
import graphs.impl.AdjacencyMatrixGraph

private val noEdgeTokens = setOf("-" /*"0"*/)
private val pattern = "(-?\\d+)|${noEdgeTokens.joinToString("|")}".toRegex()
private var id = 0

fun parse(
    name: String = "unnamed_$id",
    input: String,
    implementation: (String, List<List<Int?>>) -> Graph = ::AdjacencyMatrixGraph,
) = implementation(name,
    input.split("\n")
        .filter { it.isNotBlank() }
        .map { row ->
            pattern.findAll(row).map {
                if (noEdgeTokens.contains(it.value)) null
                else it.value.toInt()
            }.toList()
        }).also { id++ }

fun standardToString(graph: Graph) =
    StringBuilder().apply {
        append(":").append(graph.name).append(":")
        append(System.lineSeparator())
        for (i in 0 until graph.numVer) {
            for (j in 0 until graph.numVer)
                append(
                    if (graph.isCom(i, j))
                        graph.getWeightEdg(i, j).toString()
                    else noEdgeTokens.first()
                ).append(" ")
            append(System.lineSeparator())
        }
    }.toString()
