package algorithm

import com.github.shiguruikai.combinatoricskt.combinations
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

fun isCluster(g: Graph): Boolean {
    return findComponents(g).withIndex()
        .groupBy { it.value }.all { (_, values) ->
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
