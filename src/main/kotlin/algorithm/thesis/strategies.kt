package algorithm.thesis

import graphs.Graph
import storage.Generator
import kotlin.math.max
import kotlin.math.min

/**
 * Интерфейс для управления структурой дерева поиска в методе ветвей и границ
 */
interface Strategy {
    /**
     * Минимизируемое значение для данного графа
     */
    fun record(graph: Graph): Int

    /**
     * Вычисление оценок узлов дерева (подграфов),
     * чем ниже оценка, тем раньше он обработается (т.к. задача минимизации).
     * Если все оценки больше или равны рекорду - завершение работы
     */
    fun evaluate(sub: Subgraph) = Int.MIN_VALUE

    /**
     * Задаёт порядок сохранения рёбер в графе (т.е. удаляются с конца)
     */
    fun sortEdges(edges: MutableList<Pair<Int, Int>>, graph: Graph)

    /**
     * Указывает, нужна ли пересортировка с помощью `sortEdges`
     * после каждого удаления ребра, или только 1 раз в начсле
     */
    val reSort: Boolean get() = true
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

    //override val reSort = false
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
            .minus(sub.rawEdges.toSet())
            .sumOf { sub.graph.getWeightEdg(it)!! }
        return max(reqMinWeight, curMinWeight)
    }

    override fun sortEdges(edges: MutableList<Pair<Int, Int>>, graph: Graph) = edges.sortWith(
        compareBy<Pair<Int, Int>> { graph.getWeightEdg(it) }
            .thenBy { (u, v) -> min(graph.deg(u), graph.deg(v)) }
            .thenBy { (u, v) -> graph.deg(u) + graph.deg(v) }
            .reversed())
}
