package storage

import graphs.AdjacencyMatrixGraph

private val noEdgeTokens = setOf("-", "0")
private val pattern = "(-?\\d+)|${noEdgeTokens.joinToString("|")}".toRegex()
private var id = 0

fun parseAdjacencyMatrix(name: String = "unnamed_$id", input: String) =
    AdjacencyMatrixGraph(name,
        input.split("\n").filter { it.isNotBlank() }.map { row ->
            pattern.findAll(row).map {
                if (noEdgeTokens.contains(it.value))
                    null
                else
                    it.value.toInt()
            }.toList()
        }).also { id++ }
