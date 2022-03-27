package storage

import algorithm.LocalConnectivity
import algorithm.connectivity
import algorithm.localEdgeConnectivity
import graphs.Graph
import graphs.GraphException
import graphs.impl.AdjacencyMatrixGraph
import graphs.toEdge
import mu.KotlinLogging
import kotlin.math.ceil
import kotlin.math.roundToInt

class Generator(
    private val numVer: Int,
    _numEdge: Int? = null,
    p: Float? = null,
    private val name: String? = null,
    private val weights: IntRange = 1..1,
    private val conn: Int? = null,
    private val localConn: LocalConnectivity = ::localEdgeConnectivity,
    private val isDir: Boolean = false,
    private val withGC: Boolean = (conn ?: 0) > 1,
    private val implementation: (String, Int) -> Graph = ::AdjacencyMatrixGraph,
    private val except: Collection<Graph> = mutableListOf(),
    private val genLim: Int = Short.MAX_VALUE.toInt()
) {
    private val numEdge: Int
    private val logger = KotlinLogging.logger {}

    init {
        requireG((_numEdge != null) xor (p != null)) { "Выберите что то одно: либо точное число рёбер, либо вероятность появления" }
        val maxEdges = maxNumEdge(numVer)
        numEdge = _numEdge ?: (maxEdges * p!!).roundToInt()
        requireG(numEdge in 0..maxEdges) { "Не может быть $numEdge рёбер в графе с $numVer вершинами (max $maxEdges)" }
        requireG(conn == null || conn > 0) { "Связность графа не может быть $conn" }
        if (conn != null) {
            val reqEdgNum = minNumEdge(numVer, conn)
            requireG(numEdge >= reqEdgNum) { "($numVer,$numEdge)-граф не может быть $conn-связным (min $reqEdgNum рёбер)" }
        }
        requireG(genLim > 0) { "Предел генерации должен быть больше 0" }
    }

    fun build(): Graph {
        val namePattern =
            name ?: "${if (isDir) "Dir" else "Undir"}_${numVer}x${numEdge}_${weights}${if (withGC) "_GC" else ""}"

        var graph: Graph
        var mutableLim = genLim

        logger.debug { "Генерируем граф с именем $namePattern" }
        do {
            requireG(mutableLim != 0) { "Превышен предел генерации ($genLim). Скорее всего граф с данными параметрами не существует" }

            graph = implementation.invoke(namePattern, numVer)
                .apply { oriented = isDir }
                .apply { if (withGC) withGC(weights) }
                .addEdge(numEdge - (if (withGC) numVer else 0), weights)

            mutableLim--
        } while (except.contains(graph) ||
            (conn != null && connectivity(graph, localConn) < conn)
        )
        logger.debug { "Граф сгенерирован:\n$graph" }
        return graph
    }

    private fun Graph.addEdge(count: Int, weightRange: IntRange) = apply {
        logger.debug { "Добавляем $count рёбер" }
        getPairVer().filterNot { isCom(it) }
            .shuffled()
            .take(count)
            .apply { require(size == count) { "Слишком много рёбер добавляете (max $size)" } }
            .forEach { addEdg(it.toEdge(weightRange.random())) }
    }

    private fun Graph.withGC(weightRange: IntRange) = apply {
        logger.debug { "Создаём Гамильтонов цикл" }
        (0 until numVer).shuffled()
            .run { plus(first()) }
            .zipWithNext()
            .forEach { addEdg(it.toEdge(weightRange.random())) }
    }

    companion object {
        fun maxNumEdge(n: Int) = n * (n - 1) / 2
        fun minNumEdge(n: Int, k: Int) =
            if (k == 1) n - 1
            else ceil(k * n / 2.0).toInt()
    }
}


inline fun requireG(value: Boolean, lazyMessage: () -> Any) {
    if (!value) {
        val message = lazyMessage()
        throw GraphException(message.toString())
    }
}
