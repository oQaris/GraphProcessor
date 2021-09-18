package storage

import algorithm.edgeConnectivity
import algorithm.vertexConnectivity
import graphs.AdjacencyMatrixGraph
import graphs.Graph

fun genUndirectedGraph(n: Int, p: Float) =
    AdjacencyMatrixGraph("rand", Array(n) { arrayOfNulls<Int?>(n) })
        .apply {
            oriented = false
            getPairVer().shuffled()
                .apply { takeLast((size * p).toInt()) }
                .forEach { addEdg(it) }
        }

fun genConnectedGraph(n: Int, p: Float, k: Int = 1): Graph {
    var graph: Graph
    do {
        graph = genUndirectedGraph(n, p)
    } while (edgeConnectivity(graph) < k ||
        vertexConnectivity(graph) < k
    )
    return graph
}


fun genGraphWithGC(n: Int, p: Float) =
    AdjacencyMatrixGraph("GC", Array(n) { arrayOfNulls<Int?>(n) })
        .apply {
            oriented = false
            val cycle = (0 until numVer).zipWithNext().plus(0 to numVer - 1)
            cycle.forEach { addEdg(it) }
            getPairVer().minus(cycle).shuffled()
                .let { it.takeLast((it.size * p).toInt()) }
                .forEach { addEdg(it) }
        }
