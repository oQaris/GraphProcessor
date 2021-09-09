package algorithms

import graphs.Graph


fun isPlanarity(G: Graph): Boolean {
    throw UnsupportedOperationException("Эта функция ещё в разработке...")
}

/*
private fun isK5(G: Graph): Boolean {
    val k5: Graph = AdjacencyMatrixGraph("k5", arrayOf(arrayOf(0, 1, 1, 1, 1), arrayOf(1, 0, 1, 1, 1), arrayOf(1, 1, 0, 1, 1), arrayOf(1, 1, 1, 0, 1), arrayOf(1, 1, 1, 1, 0)))
    return GPLogic.isomorphism(G, k5)
}


private fun isK33(G: Graph): Boolean {
    val k33: Graph = AdjacencyMatrixGraph("k33", arrayOf(arrayOf(0, 1, 0, 1, 0, 1), arrayOf(1, 0, 1, 0, 1, 0), arrayOf(0, 1, 0, 1, 0, 1), arrayOf(1, 0, 1, 0, 1, 0), arrayOf(0, 1, 0, 1, 0, 1), arrayOf(1, 0, 1, 0, 1, 0)))
    AdjacencyMatrixGraph(G).matrix.forEach { row ->

    }
}*/
