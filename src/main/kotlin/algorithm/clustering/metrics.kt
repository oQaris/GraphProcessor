package console.algorithm.clustering

import graphs.Graph

fun distance(g1: Graph, g2: Graph): Int {
    val e1 = g1.getEdges()
    val e2 = g2.getEdges()
    return e1.union(e2).minus(e1.intersect(e2.toSet())).size
}
