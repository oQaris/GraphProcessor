package algorithm.thesis

import algorithm.LocalConnectivity
import algorithm.connectivity
import algorithm.localEdgeConnectivity
import graphs.Graph
import graphs.requireG
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

enum class Event {
    ADD, ON, OFF
}

data class Result(val answer: Graph, val rec: Int)

/**
 * Нахождение k-связного остовного подграфа минимальной стоимости.
 *
 * @param g                 Исходный граф.
 * @param k                 Связность искомого подграфа.
 * @param localConnectivity Функция определения связности (по умолчанию - рёберная связность).
 * @param strategy          Стратегия управления методом ветвей и границ.
 * @param driver            Объект для обработки сообщений процесса работы.
 * @return Подграф заданной связности с минимальной суммой стоимостей рёбер.
 */
fun findSpanningKConnectedSubgraph(
    g: Graph,
    k: Int,
    localConnectivity: LocalConnectivity = ::localEdgeConnectivity,
    strategy: Strategy = WeightedStrategy(),
    driver: (Event) -> Unit = {}
): Result {
    requireG(k > 0)
    requireG(connectivity(g, localConnectivity) >= k)
    { "The original graph must have a connection not less than $k" }
    var rec = strategy.record(g)  // Текущий рекорд
    var minG = g                  // Текущий минимальный граф
    // Дерево ветвления
    val leaves = PriorityQueue(compareBy<Subgraph> { it.score }
        .reversed() // В начале графы с максимальной оценкой
        .thenBy { it.rawEdges.size })
    run { // Препроцессинг
        val edges = g.getEdges()
        if (!strategy.reSort)
            strategy.sortEdges(edges, g)
        leaves.add(Subgraph(g, k, strategy, edges))
    }
    driver.invoke(Event.ON)
    while (leaves.isNotEmpty()) {
        val curElem = leaves.poll()
        if (curElem.score >= rec)
            break
        val (curG, rawEdges) = curElem
        if (rawEdges.isEmpty())
            continue
        val edge = rawEdges.removeFirst() // изменяется curElem
        if (localConnectivity(curG, edge.first, edge.second) > k) {
            val newG = curG.clone().apply { remEdg(edge) }
            val newElem = Subgraph(
                newG, k, strategy,
                unfixedEdges = rawEdges.toMutableList(),
                lastRemEdge = edge
            )
            val newRec = strategy.record(newG)
            if (newRec < rec) {
                driver.invoke(Event.ADD)
                rec = newRec
                minG = newG
                leaves.removeIf { it.score >= rec }
            }
            if (newElem.score < rec)
                leaves.add(newElem)
        }
        curElem.updateScore()
        if (curElem.score < rec)
            leaves.add(curElem)
    }
    driver.invoke(Event.OFF)
    return Result(minG, rec)
}
