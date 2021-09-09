package algorithms

import graphs.Graph

fun redo(g: Graph, lambda: (u: Int, v: Int, w: Int) -> Int) {
    for (edge in g.getEdges())
        if (g.isCom(edge))
            g.setWeightEdg(
                edge,
                lambda(edge.first, edge.second, g.getWeightEdg(edge) ?: 0)
            )
}
