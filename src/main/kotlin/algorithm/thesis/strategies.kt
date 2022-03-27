package algorithm.thesis

import graphs.Edge
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
    fun evaluate(sub: Node) = Int.MIN_VALUE

    /**
     * Задаёт порядок удаления рёбер в графе
     */
    fun sortEdges(edges: MutableList<Edge>, graph: Graph)

    /**
     * Указывает, нужна ли пересортировка с помощью `sortEdges`
     * после каждого удаления ребра, или только 1 раз в начсле
     */
    val reSort: Boolean get() = true
}

open class UnweightedStrategy : Strategy {
    override fun record(graph: Graph) = graph.numEdg

    override fun evaluate(sub: Node) =
        max(
            Generator.minNumEdge(sub.graph.numVer, sub.k),
            sub.graph.numEdg - sub.rawEdges.size
        )

    override fun sortEdges(edges: MutableList<Edge>, graph: Graph) = edges.sortWith(
        compareBy<Edge> { (u, v) -> min(graph.deg(u), graph.deg(v)) }
            .thenBy { (u, v) -> graph.deg(u) + graph.deg(v) }
            .reversed())

    //override val reSort = false
}

class WeightedStrategy : Strategy {
    override fun record(graph: Graph) = graph.sumWeights

    override fun evaluate(sub: Node): Int {
        val reqMinWeight = sub.graph.getEdges()
            .map { it.weight }
            .sortedBy { it }
            .take(Generator.minNumEdge(sub.graph.numVer, sub.k))
            .sumOf { it }
        val curMinWeight = sub.graph.getEdges()
            .minus(sub.rawEdges.toSet())
            .sumOf { it.weight }
        return max(reqMinWeight, curMinWeight)
    }

    override fun sortEdges(edges: MutableList<Edge>, graph: Graph) = edges.sortWith(
        compareBy<Edge> { it.weight }
            .thenBy { (u, v) -> min(graph.deg(u), graph.deg(v)) }
            .thenBy { (u, v) -> graph.deg(u) + graph.deg(v) }
            .reversed())
}

class NegativeWeightedStrategy : Strategy {
    override fun record(graph: Graph) = graph.sumWeights

    override fun evaluate(sub: Node): Int {
        val weights = sub.graph.getEdges()
            .map { it.weight }

        val allNegs = weights.filter { it < 0 }

        val addEdgesCount = (Generator.minNumEdge(sub.graph.numVer, sub.k) - allNegs.size)
            .let { if (it < 0) 0 else it }

        val reqMinWeight = allNegs.sum() +
                weights.minus(allNegs.toSet())
                    .sortedBy { it }
                    .take(addEdgesCount)
                    .sum()

        val curMinWeight = sub.graph.getEdges()
            .minus(sub.rawEdges.toSet())
            .map { it.weight }
            .minus(allNegs.toSet())
            .sum() + allNegs.sum()

        return max(reqMinWeight, curMinWeight)
    }

    override fun sortEdges(edges: MutableList<Edge>, graph: Graph) = edges.sortWith(
        compareBy<Edge> { it.weight }
            .thenBy { (u, v) -> min(graph.deg(u), graph.deg(v)) }
            .thenBy { (u, v) -> graph.deg(u) + graph.deg(v) }
            .reversed())
}
