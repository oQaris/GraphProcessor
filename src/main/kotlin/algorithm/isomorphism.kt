package algorithm

import graphs.AdjacencyMatrixGraph
import graphs.Graph


fun planarity(g: Graph): Boolean {
    throw UnsupportedOperationException("Эта функция ещё в разработке...")
    //todo найти подграфы изоморфные k33 или k5
}

val k33: Graph = AdjacencyMatrixGraph(
    "k33",
    arrayOf(
        arrayOf(false, true, false, true, false, true),
        arrayOf(true, false, true, false, true, false),
        arrayOf(false, true, false, true, false, true),
        arrayOf(true, false, true, false, true, false),
        arrayOf(false, true, false, true, false, true),
        arrayOf(true, false, true, false, true, false)
    )
)

val k5: Graph = AdjacencyMatrixGraph(
    "k5",
    arrayOf(
        arrayOf(false, true, true, true, true),
        arrayOf(true, false, true, true, true),
        arrayOf(true, true, false, true, true),
        arrayOf(true, true, true, false, true),
        arrayOf(true, true, true, true, false)
    )
)

fun isomorphism(g1: Graph, g2: Graph): Boolean {
    // Если разное количество вершин или рёбер, то точно не изоморфны
    if (g1.numVer != g2.numVer ||
        g1.numEdg != g2.numEdg
    ) return false

    @OptIn(ExperimentalStdlibApi::class)
    fun degreeSequence(g: Graph) =
        buildList { (0 until g.numVer).forEach { add(it to g.deg(it)) } }.sortedBy { it.second }

    // Если разные степенные последовательности, то тоже не изоморфны
    val degSeq1 = degreeSequence(g1)
    val degSeq2 = degreeSequence(g2)
    if (degSeq1.map { it.second } != degSeq2.map { it.second }) return false

    // Если всё прерыдущее не помогло, то - полный перебор
    val mtx1 = AdjacencyMatrixGraph(g1).matrix
    val mtx2 = AdjacencyMatrixGraph(g2).matrix

    //todo доделать
    /*for (int i = 0; i < mtx1.length; i++)
        for (int j = i + 1; j < mtx1.length; j++) {
            changeRowCol(mtx2, i, j);
            if (Arrays.deepEquals(mtx1, mtx2))
                return true;
        }*/return false
}

fun isSymmetric(g: Graph): Boolean {
    val data = AdjacencyMatrixGraph(g).matrix
    for (i in 0 until g.numVer) for (j in 0 until g.numVer) if (data[i][j] != data[j][i]) return false
    return true
}

fun isTree(g: Graph): Boolean {
    return vertexConnectivity(g) >= 1 && g.numEdg == g.numVer - 1
}
