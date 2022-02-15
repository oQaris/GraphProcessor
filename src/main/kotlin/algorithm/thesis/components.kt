package algorithm.thesis

import graphs.Graph
import kotlin.properties.Delegates

/**
 * Множество всех остовных подграфов заданной k-связности.
 * Хранится исходный граф + список непройденных рёбер
 * (рёбра не входящие в список считаются фиксированными от удаления).
 */
class Subgraph(
    val graph: Graph,
    val k: Int,
    private val strategy: Strategy,
    unfixedEdges: List<Pair<Int, Int>>,
    lastRemEdge: Pair<Int, Int>? = null,
    var order: Int = 0
) {
    val rawEdges: MutableList<Pair<Int, Int>>
    var score by Delegates.notNull<Int>()

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
    fun updateScore(removedEdge: Pair<Int, Int>? = null) {
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

    operator fun component1() = graph

    operator fun component2() = rawEdges
}

/**
 * Возвращаемый результат в `findSpanningKConnectedSubgraph`
 */
data class Result(val answer: Graph, val rec: Int, val timestamps: Timestamps)

class Timestamps {
    private val start = System.currentTimeMillis()
    private var times = mutableListOf<Long>()

    val get by lazy { times.map { it - start } }

    fun make() = times.add(System.currentTimeMillis())
}
