package storage

import algorithm.connectivity
import algorithm.localEdgeConnectivity
import graphs.AdjacencyMatrixGraph
import graphs.Graph
import mu.KotlinLogging
import kotlin.math.ceil
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger {}

class Generator(
    private val numVer: Int,
    numEdg: Int? = null,
    p: Float? = null,
    private val name: String? = null,
    private val weights: IntRange = 1..1,
    private val conn: Int? = null,
    private val localConn: ((Graph, Int, Int) -> Int) = ::localEdgeConnectivity,
    private val isDir: Boolean = false,
    private val withGC: Boolean = false,
    private val implementation: (String, Int) -> Graph = ::AdjacencyMatrixGraph
) {
    private val numEdge: Int

    init {
        require((numEdg != null) xor (p != null)) { "Выберите что то одно: либо точное число рёбер, либо вероятность появления" }
        val maxEdges = maxNumEdge(numVer)
        numEdge = numEdg ?: (maxEdges * p!!).roundToInt()
        require(numEdge in 0..maxEdges) { "Не может быть $numEdge рёбер в графе с $numVer вершинами" }
        require(conn == null || conn > 0) { "Связность графа не может быть $conn" }
        if (conn != null) {
            val reqEdgNum =
                if (conn == 1) numVer - 1
                else ceil(conn * numVer / 2.0).toInt()
            require(numEdge >= reqEdgNum) { "В $conn-связном графе не может быть $numEdg рёбер (min $reqEdgNum)" }
        }
    }

    fun build(): Graph {
        val namePattern =
            name ?: "${if (isDir) "Dir" else "Undir"}_${numVer}x${numEdge}_${weights}${if (withGC) "_GC" else ""}"
        var graph: Graph
        do {
            logger.debug { "Генерируем граф с именем $namePattern" }
            graph = implementation.invoke(namePattern, numVer)
                .apply { oriented = isDir }
                .apply { if (withGC) withGC() }
                .addEdge(numEdge, weights)
        } while (conn != null && connectivity(graph, localConn) < conn)
        logger.debug { "Граф сгенерирован:\n$graph" }
        return graph
    }

    private fun Graph.addEdge(count: Int, weightRange: IntRange) = apply {
        logger.debug { "Добавляем $count рёбер" }
        getPairVer().filterNot { isCom(it) }
            .shuffled()
            .takeLast(count)
            .forEach { addEdg(it, weightRange.random()) }
    }

    private fun Graph.withGC() = apply {
        logger.debug { "Создаём Гамильтонов цикл" }
        (0 until numVer).zipWithNext()
            .plus(numVer - 1 to 0)
            .forEach { addEdg(it) }
    }

    private fun maxNumEdge(n: Int) = n * (n - 1) / 2
}
