package algorithm.thesis

import algorithm.LocalConnectivity
import algorithm.connectivity
import algorithm.localEdgeConnectivity
import graphs.AdjacencyMatrixGraph
import graphs.Graph
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Нахождение k-связного остовного подграфа с наименьшим числом ребер.
 *
 * @param g                 Исходный граф.
 * @param k                 Связность искомого подграфа.
 * @param localConnectivity Функция определения связности (по умолчанию - рёберная связность).
 * @return Подграф заданной связности с минимальным числом рёбер.
 */
fun findSpanningKConnectedSubgraph(
    g: Graph,
    k: Int,
    localConnectivity: LocalConnectivity = ::localEdgeConnectivity,
    strategy: Strategy = UnweightedStrategy()
): Result {

    require(k > 0)
    require(connectivity(g, localConnectivity) >= k) { "The graph must have connectivity >= $k" }
    var rec = strategy.record(g)
    var minG = g
    var order = 0
    val timestamps = Timestamps()

    val leaves = TreeSet(compareBy<Subgraph> { it.score }
        .reversed() // в начале графы с максимальной оценкой
        .thenBy { it.rawEdges.size }
        .thenBy { it.order }) // этот костыль нужен чтоб в TreeSet не удалялись графы, которые равны по компаратору

    val edges = g.getEdges()
    if (!strategy.reSort)
        strategy.sortEdges(edges, g)

    leaves.add(
        Subgraph(g, k, strategy, edges, order = ++order)
            .apply { logger.debug { "Оценка исходного графа $score" } })

    try {
        while (leaves.isNotEmpty()) {
            val curElem = leaves.pollFirst()!!

            if (curElem.score >= rec)
                break

            val (curG, rawEdges) = curElem
            if (rawEdges.isEmpty())
                continue

            val edge = rawEdges.removeFirst()

            if (localConnectivity(curG, edge.first, edge.second) > k) {

                val newG = AdjacencyMatrixGraph(curG).apply { remEdg(edge); ++order }

                logger.debug { "Удалили ребро $edge у графа ${order}. Нефиксированные рёбра: ${curElem.rawEdges}" }
                val newElem = Subgraph(
                    newG, k, strategy,
                    unfixedEdges = rawEdges.toMutableList(),
                    lastRemEdge = edge,
                    order = order
                )
                logger.debug { "Оценка получившегося графа ${newElem.score}" }

                val newRec = strategy.record(newG)
                if (newRec < rec) {
                    rec = newRec
                    logger.info { "Теперь рекорд $rec" }
                    minG = newG
                    timestamps.make()
                    leaves.removeIf { it.score >= rec }
                }
                if (newElem.score < rec && !leaves.add(newElem))
                    throw IllegalArgumentException("Внутренняя ошибка №1")
            }
            curElem.updateScore()
            curElem.order = ++order
            if (curElem.score < rec && !leaves.add(curElem))
                throw IllegalArgumentException("Внутренняя ошибка №2")
        }
    } catch (e: OutOfMemoryError) {
        logger.error(e) { "Всего графов: ${leaves.size}" }
        throw e
    }
    logger.info { "Рекорд: $rec" }
    logger.debug { "Исходный граф: $g" }
    logger.debug { "Получившийся граф: $minG" }
    timestamps.make()
    return Result(minG, rec, timestamps)
}
