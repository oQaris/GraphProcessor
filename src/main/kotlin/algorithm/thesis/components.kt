package algorithm.thesis

import graphs.Edge
import graphs.Graph
import graphs.edg
import utils.Timestamps
import kotlin.properties.Delegates

interface Node {
    val graph: Graph
    val k: Int
    val rawEdges: MutableList<Edge> // todo просто List<Pair<Int, Int>>
    var score: Int
    val strategy: Strategy
    var order: Int

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
    unfixedEdges: List<Edge>,
    lastRemEdge: Edge? = null,
    override var order: Int = 0
) : Node {
    override val rawEdges: MutableList<Edge>
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
    fun updateScore(removedEdge: Edge? = null) {
        // Некоторая оптимизация, чтоб не перебирать все рёбра в графе, когда известно какое удалено
        if (removedEdge != null) remUnsuitableRawEdges(removedEdge)
        else graph.getEdges().forEach { remUnsuitableRawEdges(it) }
        score = strategy.evaluate(this)
    }

    private fun remUnsuitableRawEdges(removedEdge: Edge) {
        val (u, v) = removedEdge
        graph.com(u).map { it to u }
            .plus(graph.com(v).map { it to v })
            .forEach { (s, t) ->
                if (graph.deg(s) <= k || graph.deg(t) <= k) {
                    rawEdges.remove(s edg t)
                    rawEdges.remove(t edg s)
                }
            }
    }
}

class EconomicalSubgraph(
    private val originalGraph: Graph,
    override val k: Int,
    override val strategy: Strategy,
    val remEdges: List<Edge> = listOf(),
    val fixEdges: MutableList<Edge> = mutableListOf(),
    lastRemEdge: Edge? = null,
    override var order: Int = 0
) : Node {
    override var score by Delegates.notNull<Int>()

    override val rawEdges: MutableList<Edge> by lazy {
        val edges = graph.getEdges()
            .minus((fixEdges + remEdges).toSet()).toMutableList()
        strategy.sortEdges(edges, graph)
        edges
    }

    override val graph: Graph by lazy {
        genGraph()
    }

    init {
        updateScore(lastRemEdge)
    }

    /**
     * Функция пересчёта оценки при изменении графа. Из списка непройденных рёбер убираем те,
     * удаление которых в исходном графе нарушит его k-связность.
     * @param removedEdge Удалённое ребро.
     * Если не null, то будут рассматриваться только рёбра, инцидентные его концам.
     */
    private fun updateScore(removedEdge: Edge?) {
        val g = genGraph()
        if (removedEdge != null) fixedEdges(g, removedEdge)
        else originalGraph.getEdges().forEach { fixedEdges(g, it) }
        score = strategy.evaluate(this)
    }

    private fun fixedEdges(g: Graph, removedEdge: Edge) {
        val (u, v) = removedEdge
        g.com(u).map { it to u }
            .plus(g.com(v).map { it to v })
            .forEach { (s, t) ->
                if (g.deg(s) <= k || g.deg(t) <= k) {
                    fixEdges.add(s edg t)
                    fixEdges.add(t edg s)
                }
            }
    }

    private fun genGraph() = originalGraph.clone()
        .apply {
            remEdges.forEach { remEdg(it) }
        }
}

/**
 * Возвращаемый результат в {@link algorithm.thesis.CourseworkKt#findSpanningKConnectedSubgraph(Graph,Int,LocalConnectivity,Strategy)}
 */
data class Result(val answer: Graph, val rec: Int, val timestamps: Timestamps)
