package console.algorithm.clustering

import com.github.shiguruikai.combinatoricskt.combinations
import graphs.Edge
import graphs.Graph
import graphs.edg

fun greedy(
    base: Graph,
    maxSizeCluster: Int
): Graph {
    val result = base.clone()
    val notVisited = base.getVertices().toMutableSet()
    for (size in maxSizeCluster downTo 1) {
        notVisited.combinations(size).forEach { sub ->
            val dropEdges = mutableSetOf<Edge>()
            sub.all { v ->
                val toVertexes = result.com(v).toSet()
                dropEdges.addAll(toVertexes.minus(sub).map { v edg it })
                sub.minus(toVertexes.plus(v)).isEmpty()
            }.also { isClique ->
                if (isClique) {
                    dropEdges.forEach { result.remEdg(it) }
                    notVisited.removeAll(sub)
                }
            }
        }
    }
    return result
}
