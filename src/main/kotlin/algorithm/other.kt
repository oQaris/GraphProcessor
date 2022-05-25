package algorithm

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
