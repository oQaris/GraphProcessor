package algorithm

import graphs.AdjacencyMatrixGraph
import graphs.Graph
import mu.KotlinLogging
import java.util.*
import kotlin.math.ceil
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
    val rawEdges: MutableList<Pair<Int, Int>>,
    val k: Int,
    private val strategy: Strategy,
    lastRemEdge: Pair<Int, Int>? = null
) {
    var score by Delegates.notNull<Int>()

    init {
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
    fun evaluate(sub: Subgraph): Int
    fun sortEdges(edges: List<Pair<Int, Int>>, graph: Graph): MutableList<Pair<Int, Int>>
}

private class UnweightedStrategy : Strategy {
    override fun evaluate(sub: Subgraph) = max(
        if (sub.k == 1) sub.graph.numVer - 1
        else ceil(sub.k * sub.graph.numVer / 2.0).toInt(),
        sub.graph.numEdg - sub.rawEdges.size
    )

    override fun sortEdges(edges: List<Pair<Int, Int>>, graph: Graph) =
        edges.sortedWith(Comparator
            .comparing<Pair<Int, Int>, Int> { (u, v) -> min(graph.deg(u), graph.deg(v)) }
            .thenComparing { (u, v) -> graph.deg(u) + graph.deg(v) }).toMutableList()
}

/*private class WeightedStrategy : Strategy {
    override fun evaluate(sub: Subgraph): Int {
        //TODO: Реализовать
    }

    override fun sortEdges(edges: List<Pair<Int, Int>>, graph: Graph): MutableList<Pair<Int, Int>> {
        //TODO: Реализовать
    }
}*/

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
    localConnectivity: ((Graph, Int, Int) -> Int) = ::localEdgeConnectivity,
    strategy: Strategy = UnweightedStrategy()
): Result {

    require(k > 0)
    require(connectivity(g, localConnectivity) >= k) { "The graph must have connectivity >= $k" }
    var rec = g.numEdg
    var minG = g
    var id = 0L
    val timestamps = Timestamps()

    val leaves = TreeSet(Comparator
        .comparing(Subgraph::score)
        //.reversed() //если раскомментировать, то будут в начале графы с максимальной оценкой
        .thenComparing { sub -> sub.rawEdges.size }
        .thenComparing { sub -> sub.graph.name })

    leaves.add(Subgraph(g, g.getEdges().sortEdges(g), k, strategy)
        .apply { logger.debug { "Оценка исходного графа $score" } })

    try {
        while (leaves.isNotEmpty()) {
            val curElem = leaves.pollFirst()!!

            if (curElem.score >= rec)
                break

            val (curG, curEdges) = curElem
            if (curEdges.isEmpty())
                continue

            val edge = curEdges.removeLast()/*.removeFirst() - чтобы наоборот*/

            if (localConnectivity(curG, edge.first, edge.second) > k) {

                val newG = AdjacencyMatrixGraph(curG).apply {
                    remEdg(edge)
                    name = id++.toString()
                }
                val numEdges = newG.numEdg

                logger.debug { "Удалили ребро $edge у графа ${newG.name}. Нефиксированные рёбра: ${curElem.rawEdges}" }
                val nm = Subgraph(newG, curEdges.sortEdges(newG), k, strategy, lastRemEdge = edge)
                logger.debug { "Оценка получившегося графа ${nm.score}" }

                if (numEdges < rec) {
                    rec = numEdges
                    logger.debug { "Теперь рекорд $rec" }
                    minG = newG
                    timestamps.make()
                    leaves.removeIf { it.score >= rec }
                }
                if (nm.score < rec && !leaves.add(nm))
                    throw IllegalArgumentException("Что то пошло не так 1.")
            }
            curElem.updateScore()
            if (curElem.score < rec && !leaves.add(curElem
                    .apply { graph.name = id++.toString() })
            ) throw IllegalArgumentException("Что то пошло не так 2.")
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

fun List<Pair<Int, Int>>.sortEdges(g: Graph) =
    this.sortedWith(Comparator
        .comparing<Pair<Int, Int>, Int> { (u, v) -> min(g.deg(u), g.deg(v)) }
        .thenComparing { (u, v) -> g.deg(u) + g.deg(v) }).toMutableList()

data class Result(val answer: Graph, val rec: Int, val timestamps: Timestamps)

class Timestamps {
    private val start = System.currentTimeMillis()
    private var times = mutableListOf<Long>()

    fun get() = times.map { it - start }

    fun make() = times.add(System.currentTimeMillis())
}
