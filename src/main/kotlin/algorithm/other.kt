package algorithm

import com.github.shiguruikai.combinatoricskt.combinations
import graphs.Edge
import graphs.Graph

fun redo(g: Graph, lambda: (u: Int, v: Int, w: Int) -> Int): Int {
    var count = 0
    g.getEdges().forEach { edge ->
        g.setWeightEdg(
            edge.copy(weight = lambda(edge.first, edge.second, edge.weight))
        )
        ++count
    }
    return count
}

fun isClustering(g: Graph): Boolean {
    return findComponents(g).withIndex()
        .groupBy { it.value }.all { (_, values) ->
            val vertexInCmp = values.map { it.index }
            vertexInCmp.combinations(2).all {
                g.isCom(it[0], it[1])
            }
        }
}

fun isFull(g: Graph): Boolean {
    val vertexes = g.getVertices()
    return vertexes.combinations(2).all {
        g.isCom(it[0], it[1])
    }
}

fun isClusteringMaxSize(g: Graph, maxSizeCluster: Int): Boolean {
    return findComponents(g).withIndex()
        .groupBy { it.value }.all { (_, values) ->
            if (values.size > maxSizeCluster)
                return false
            val vertexInCmp = values.map { it.index }
            vertexInCmp.combinations(2).all {
                g.isCom(it[0], it[1])
            }
        }
}

fun findComponents(g: Graph): IntArray {
    val components = IntArray(g.numVer)
    var newCmp = 0
    g.getVertices().forEach { v ->
        if (components[v] == 0)
            dfs(g, v, ++newCmp, components)
    }
    return components
}

private fun dfs(g: Graph, v: Int, component: Int, components: IntArray) {
    components[v] = component
    g.com(v).forEach { u ->
        if (components[u] == 0)
            dfs(g, u, component, components)
    }
}

inline fun distance(g1: Graph, g2: Graph): Int {
    return diffEdges(g1, g2).size
}

inline fun diffEdges(g1: Graph, g2: Graph): Set<Edge> {
    val e1 = g1.getEdges()
    val e2 = g2.getEdges()
    return e1.union(e2).minus(e1.intersect(e2.toSet()))
}