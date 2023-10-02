package console.algorithm.clustering

import algorithm.findComponents
import algorithm.isClusteringMaxSize
import algorithm.thesis.Event
import graphs.Edge
import graphs.Graph
import graphs.edg
import graphs.impl.AdjacencyMatrixGraph
import graphs.toEdge
import java.util.*

var globalID = 0L
val ascLastComparator: Comparator<Subgraph> =
    compareBy<Subgraph> { it.score }.reversed().thenBy { it.id }.reversed()

class Subgraph(
    val graph: Graph,
    var score: Int = 0,
    val rawEdges: MutableList<Edge> = graph.getEdges().toMutableList(),
    val id: Long = globalID++
) {

    override fun toString(): String {
        return "${graph.name}_$id//$score//$rawEdges"
    }
}

fun clustering(
    base: Graph,
    maxSizeCluster: Int,
    driver: (Event) -> Unit = {}
): Graph? {
    require(maxSizeCluster <= 3)
    val leaves = PriorityQueue(ascLastComparator)
    var rec = Int.MAX_VALUE
    var answer: Graph? = null

    leaves.add(Subgraph(base.clone(), 0, base.getEdges().toMutableList()))
    driver.invoke(Event.ON)

    while (leaves.isNotEmpty()) {
        val curElem = leaves.poll()
        if (curElem.score >= rec)
            break

        driver.invoke(Event.EXE)
        val edge = curElem.rawEdges.removeFirst()

        if (isRepresentative(curElem, maxSizeCluster)) {
            val fixed = fixAddPreprocess(curElem)
            if (isClusteringMaxSize(fixed.graph, maxSizeCluster)) {
                driver.invoke(Event.REC)
                rec = fixed.score
                answer = fixed.graph
                leaves.removeIf { it.score >= rec }
            }
            leaves.add(preSortEdges(fixed, edge))
        }

        val newG = curElem.graph.clone().apply { remEdg(edge) }
        val newScore = curElem.score + 1

        if (isClusteringMaxSize(newG, maxSizeCluster)) {
            driver.invoke(Event.REC)
            rec = newScore
            answer = newG
            leaves.removeIf { it.score >= rec }
        } else {
            val newNode = Subgraph(newG, newScore, curElem.rawEdges.toMutableList())
            if (isRepresentative(newNode, maxSizeCluster))
                leaves.add(newNode)
        }
    }
    driver.invoke(Event.OFF)
    return answer
}

/**
 * РАБОТАЕТ ТОЛЬКО ДЛЯ К=3 !!!
 */
fun fixAddPreprocess(node: Subgraph): Subgraph {
    val components = fixedEdgesComponents(node)
    val extraEdges = components.withIndex()
        .groupBy { it.value }
        .filter { it.value.size == 3 }.flatMap { (_, curCmp) ->
            val curCmpVer = curCmp.map { it.index }.toSet()
            // Рёбра, исходящие от тругольника
            curCmpVer.flatMap { v ->
                (node.graph.com(v) - curCmpVer).map { it edg v }
            }
        }.toSet()
    return if (extraEdges.isNotEmpty()) {
        val newG = node.graph.clone().apply { extraEdges.forEach { remEdg(it) } }
        val newScore = node.score + extraEdges.size
        Subgraph(newG, newScore, (node.rawEdges - extraEdges).toMutableList())
    } else node
}

fun preSortEdges(node: Subgraph, edge: Edge): Subgraph {
    fun incByVer(v: Int) = node.graph.com(v).zip(generateSequence { v }.asIterable()).map { it.toEdge() }
    val incidentEdges = incByVer(edge.first) + incByVer(edge.second)
    node.rawEdges.sortBy { e -> e !in incidentEdges && e.revert() !in incidentEdges }
    return node
}

/**
 * проверяет, что:
 * узел содержит необработанные рёбра;
 * размер максимальной компоненты связности, образованной фиксированными рёбрами не больше maxSizeCluster;
 * выполняется критерий кластерности - нет незавершённых треугольников в эффективном графе.
 */
fun isRepresentative(node: Subgraph, maxSizeCluster: Int): Boolean {
    if (node.rawEdges.isEmpty())
        return false
    val components = fixedEdgesComponents(node)
    return maxSizeComponent(components) <= maxSizeCluster
            && correctCriterionOfClustering(components, node.graph)
}

fun fixedEdgesComponents(node: Subgraph): IntArray {
    val fixedEdges = node.graph.getEdges() - node.rawEdges.toSet()
    val fixedSubgraph = slice(node.graph, fixedEdges)
    return findComponents(fixedSubgraph)
}

fun maxSizeComponent(components: IntArray) =
    components.groupBy { it }.maxOf { it.value.size }

/**
 * Критерий кластерности графа.
 * Если компонента связности, порождённная фиксированными рёбрами, состоит из тёх вершин,
 * то они должны быть все смежны в исходном графе.
 */
fun correctCriterionOfClustering(components: IntArray, origGraph: Graph): Boolean {
    return components.withIndex()
        .groupBy { it.value }
        .filter { it.value.size == 3 }.all { (_, curCmp) ->
            val curCmpVer = curCmp.map { it.index }
            // Проверяем, что 3 вершины образуют треугольник
            curCmpVer.all { v ->
                origGraph.com(v).containsAll(curCmpVer.toSet() - v)
            }
        }
}

fun slice(graph: Graph, edges: List<Edge>) =
    AdjacencyMatrixGraph(graph.name, graph.numVer).apply {
        edges.forEach {
            addEdg(it)
        }
    }
