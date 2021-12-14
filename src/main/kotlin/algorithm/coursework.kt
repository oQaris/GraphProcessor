package algorithm

import graphs.AdjacencyMatrixGraph
import graphs.Graph
import mu.KotlinLogging
import storage.Generator
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

private val logger = KotlinLogging.logger {}

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
        this.rawEdges = unfixedEdges.toMutableList()
        strategy.sortEdges(this.rawEdges, graph)
        updateScore(lastRemEdge)
    }

    /**
     * Функция пересчёта оценки при изменении графа. Из списка непройденных рёбер убираем те,
     * удаление которых в исходном графе нарушит его k-связность.
     * @param removedEdge Удалённое ребро.
     * Если не null, то будут рассматриваться только рёбра, инцидентные его концам.
     */
    fun updateScore(removedEdge: Pair<Int, Int>? = null) {
        if (removedEdge != null)
            remUnsuitableEdges(removedEdge)
        else graph.getEdges().forEach { remUnsuitableEdges(it) }
        score = strategy.evaluate(this)
    }

    private fun remUnsuitableEdges(removedEdge: Pair<Int, Int>) {
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
 * Интерфейс для управления структурой дерева поиска в методе ветвей и границ.
 * `evaluate` нужен для вычисления оценок узлов дерева (подграфов),
 * чем ниже оценка, тем раньше он обработается (т.к. задача минимизации), а
 * `sortEdges` - для задания порядка сохранения рёбер в графе (т.е. удаляются с конца)
 */
interface Strategy {
    fun record(graph: Graph): Int
    fun evaluate(sub: Subgraph) = Int.MIN_VALUE
    fun sortEdges(edges: MutableList<Pair<Int, Int>>, graph: Graph)
}

class UnweightedStrategy : Strategy {
    override fun record(graph: Graph) = graph.numEdg

    override fun evaluate(sub: Subgraph) =
        max(
            Generator.minNumEdge(sub.graph.numVer, sub.k),
            sub.graph.numEdg - sub.rawEdges.size
        )

    override fun sortEdges(edges: MutableList<Pair<Int, Int>>, graph: Graph) = edges.sortWith(
        compareBy<Pair<Int, Int>> { (u, v) -> min(graph.deg(u), graph.deg(v)) }
            .thenBy { (u, v) -> graph.deg(u) + graph.deg(v) }
            .reversed())
}

class WeightedStrategy : Strategy {
    override fun record(graph: Graph) = graph.sumWeights

    override fun evaluate(sub: Subgraph): Int {
        val reqMinWeight = sub.graph.getEdges()
            .map { sub.graph.getWeightEdg(it)!! }
            .sortedBy { it }
            .take(Generator.minNumEdge(sub.graph.numVer, sub.k))
            .sumOf { it }
        val curMinWeight = sub.graph.getEdges()
            .minus(sub.rawEdges)
            .sumOf { sub.graph.getWeightEdg(it)!! }
        return max(reqMinWeight, curMinWeight)
    }

    override fun sortEdges(edges: MutableList<Pair<Int, Int>>, graph: Graph) = edges.sortWith(
        compareBy<Pair<Int, Int>> { graph.getWeightEdg(it) }
            .thenBy { (u, v) -> min(graph.deg(u), graph.deg(v)) }
            .thenBy { (u, v) -> graph.deg(u) + graph.deg(v) }
            .reversed())
}

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
        //.reversed() // в начале графы с максимальной оценкой
        .thenBy { it.rawEdges.size }
        .thenBy { it.order }) // этот костыль нужен чтоб в TreeSet не удалялись графы, которые равны по компаратору

    leaves.add(Subgraph(g, k, strategy, g.getEdges(), order = ++order)
        .apply { logger.debug { "Оценка исходного графа $score" } })

    try {
        while (leaves.isNotEmpty()) {
            val curElem = leaves.pollFirst()!!

            if (curElem.score >= rec)
                break

            val (curG, curEdges) = curElem
            if (curEdges.isEmpty())
                continue

            val edge = curEdges.removeFirst()

            if (localConnectivity(curG, edge.first, edge.second) > k) {

                val newG = AdjacencyMatrixGraph(curG).apply { remEdg(edge); ++order }

                logger.debug { "Удалили ребро $edge у графа ${order}. Нефиксированные рёбра: ${curElem.rawEdges}" }
                val nm = Subgraph(
                    newG, k, strategy,
                    curEdges.toMutableList(),
                    lastRemEdge = edge,
                    order = order
                )
                logger.debug { "Оценка получившегося графа ${nm.score}" }

                val newRec = strategy.record(newG)
                if (newRec < rec) {
                    rec = newRec
                    logger.info { "Теперь рекорд $rec" }
                    minG = newG
                    timestamps.make()
                    leaves.removeIf { it.score >= rec }
                }
                if (nm.score < rec && !leaves.add(nm))
                    throw IllegalArgumentException("Что то пошло не так 1.")
            }
            curElem.updateScore()
            curElem.order = ++order
            if (curElem.score < rec && !leaves.add(curElem))
                throw IllegalArgumentException("Что то пошло не так 2.")
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

data class Result(val answer: Graph, val rec: Int, val timestamps: Timestamps)

class Timestamps {
    private val start = System.currentTimeMillis()
    private var times = mutableListOf<Long>()

    val get by lazy { times.map { it - start } }

    fun make() = times.add(System.currentTimeMillis())
}
