package console.algorithm.clustering

import graphs.Graph
import graphs.impl.AdjacencyMatrixGraph

fun greedy(
    base: Graph,
    maxSizeCluster: Int
): Graph {
    val result = AdjacencyMatrixGraph(base.name + "_res", base.numVer)
    val visited = mutableSetOf<Int>()

    fun findCluster(start: Int, size: Int): Set<Int> {
        val cluster = mutableSetOf(start)
        visited.add(start)
        if (size > 2) {
            val adjacency = base.com(start)
            adjacency.forEach { neighbor ->
                if (!visited.contains(neighbor)) {
                    val commonNeighbors = adjacency.intersect(base.com(neighbor).toSet())
                    if (commonNeighbors.size >= size - 2) {
                        cluster.addAll(findCluster(neighbor, size - 1))
                    }
                }
            }
        }
        return cluster
    }

    for (vertex in base.getVertices()) {
        if (!visited.contains(vertex)) {
            for (size in maxSizeCluster downTo 2) {
                val cluster = findCluster(vertex, size)
                if (cluster.size == size) {
                    cluster.forEach { result.addEdg(vertex, it) }
                    break
                }
            }
        }
    }

    return result
}
