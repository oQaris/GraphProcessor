package algorithm.thesis

import graphs.Graph
import utils.Timestamps
import kotlin.properties.Delegates

interface Node {
    val graph: Graph
    val k: Int
    val rawEdges: MutableList<Pair<Int, Int>>
    var score: Int
    val strategy: Strategy
    var order: Int
    fun updateScore(removedEdge: Pair<Int, Int>? = null)

    operator fun component1() = graph

    operator fun component2() = rawEdges
}

/**
 * Множество всех остовных подграфов заданной k-связности.
 * Хранится исходный граф + список непройденных рёбер
 * (рёбра не входящие в список считаются фиксированными от удаления).
 */
class Subgraph(
    override val graph: Graph,
    override val k: Int,
    override val strategy: Strategy,
    unfixedEdges: List<Pair<Int, Int>>,
    lastRemEdge: Pair<Int, Int>? = null,
    override var order: Int = 0
) : Node {
    override val rawEdges: MutableList<Pair<Int, Int>>
    override var score by Delegates.notNull<Int>()

    init {
        rawEdges = unfixedEdges.toMutableList()
        // Сортируем при создании нового подграфа (удалении ребра в графе, а не только в rawEdges)
        if (strategy.reSort) strategy.sortEdges(rawEdges, graph)
        updateScore(lastRemEdge)
    }

    /**
     * Функция пересчёта оценки при изменении графа. Из списка непройденных рёбер убираем те,
     * удаление которых в исходном графе нарушит его k-связность.
     * @param removedEdge Удалённое ребро.
     * Если не null, то будут рассматриваться только рёбра, инцидентные его концам.
     */
    override fun updateScore(removedEdge: Pair<Int, Int>?) {
        // Некоторая оптимизация, чтоб не перебирать все рёбра в графе, когда известно какое удалено
        if (removedEdge != null) remUnsuitableRawEdges(removedEdge)
        else graph.getEdges().forEach { remUnsuitableRawEdges(it) }
        score = strategy.evaluate(this)
    }

    private fun remUnsuitableRawEdges(removedEdge: Pair<Int, Int>) {
        val (u, v) = removedEdge
        graph.com(u).map { it to u }
            .plus(graph.com(v).map { it to v })
            .forEach { (s, t) ->
                if (graph.deg(s) <= k || graph.deg(t) <= k) {
                    rawEdges.remove(s to t)
                    rawEdges.remove(t to s)
                }
            }
    }
}

/**
 * Возвращаемый результат в `findSpanningKConnectedSubgraph`
 */
data class Result(val answer: Graph, val rec: Int, val timestamps: Timestamps)
