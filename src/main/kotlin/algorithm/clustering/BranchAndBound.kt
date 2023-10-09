package console.algorithm.clustering

import algorithm.findComponents
import algorithm.isClusteringMaxSize
import algorithm.thesis.Event
import graphs.Edge
import graphs.Graph
import graphs.impl.AdjacencyMatrixGraph
import graphs.toEdge
import java.util.*

var globalID = 0L
val ascLastComparator: Comparator<Subgraph> =
    compareBy<Subgraph> { it.score }.reversed().thenBy { it.id }.reversed()

class Subgraph(
    val graph: Graph,
    var score: Int,
    val rawDetails: MutableList<Pair<Int, Int>>,
    val id: Long = globalID++
) {

    fun realEdges(): List<Edge> {
        return rawDetails.filter { graph.isCom(it) }.map { it.toEdge() }
    }

    override fun toString(): String {
        return "${graph.name}_$id//$score//$rawDetails"
    }
}

fun clustering(
    base: Graph,
    maxSizeCluster: Int,
    driver: (Event) -> Unit = {}
): Graph? {
    val leaves = PriorityQueue(compareBy<Subgraph> { it.score }.reversed().thenBy { it.id }.reversed())
    var rec = Int.MAX_VALUE
    var answer: Graph? = null

    fun addIfValid(node: Subgraph) {
        if (isValid(node, maxSizeCluster, rec))
            leaves.add(node)
    }
    leaves.add(Subgraph(base.clone(), 0, base.getPairVer().toMutableList()))
    driver.invoke(Event.ON)

    while (leaves.isNotEmpty()) {
        val curElem = leaves.poll()

        driver.invoke(Event.EXE)
        val pair = curElem.rawDetails.removeFirst()
        addIfValid(curElem)

        val newG = curElem.graph.clone().apply {
            if (curElem.graph.isCom(pair)) remEdg(pair)
            else addEdg(pair.toEdge())
        }
        val newScore = curElem.score + 1

        if (newScore < rec && isClusteringMaxSize(newG, maxSizeCluster)) {
            driver.invoke(Event.REC)
            rec = newScore
            answer = newG
            leaves.removeIf { it.score >= rec }
        } else {
            val newNode = Subgraph(newG, newScore, curElem.rawDetails.toMutableList())
            resortDetails(newNode, pair)
            addIfValid(newNode)
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
        .filter { it.value.size == 3 }
        .flatMap { (_, curCmp) ->
            val curCmpVer = curCmp.map { it.index }.toSet()
            // Рёбра, исходящие от тругольника
            curCmpVer.flatMap { v ->
                (node.graph.com(v) - curCmpVer).map { it to v }
            }
        }.toSet()
    return if (extraEdges.isNotEmpty()) {
        val newG = node.graph.clone().apply { extraEdges.forEach { remEdg(it) } }
        val newScore = node.score + extraEdges.size
        Subgraph(newG, newScore, (node.rawDetails - extraEdges).toMutableList())
    } else node
}

/**
 * Сортирует детали в подграфе в порядке:
 * 1) рёбра смежные с обработанным
 * 2) нерёбра смежные с обработанным
 * 3) рёбра не смежные с обработанным
 * 4) нерёбра несмежные с обработанным
 */
fun resortDetails(node: Subgraph, pair: Pair<Int, Int>) {
    val partResult = Array(4) { mutableListOf<Pair<Int, Int>>() }
    node.rawDetails.forEach { detail ->
        val isAdjacent = detail.first == pair.first || detail.second == pair.first
                || detail.first == pair.second || detail.second == pair.second
        val idx = if (node.graph.isCom(detail)) {
            if (isAdjacent) 0 else 2
        } else {
            if (isAdjacent) 1 else 3
        }
        partResult[idx].add(detail)
    }
    node.rawDetails.clear()
    node.rawDetails.addAll(partResult.asList().flatten())
}

/**
 * проверяет, что:
 * 1) узел содержит необработанные рёбра;
 * 2) расстояние до исходного графа меньше текущего рекорда;
 * 3) размер максимальной компоненты связности, образованной фиксированными рёбрами не больше maxSizeCluster.
 */
fun isValid(node: Subgraph, maxSizeCluster: Int, record: Int): Boolean {
    if (node.score >= record || node.rawDetails.isEmpty())
        return false
    val components = fixedEdgesComponents(node)
    return maxSizeComponent(components) <= maxSizeCluster
}

fun fixedEdgesComponents(node: Subgraph): IntArray {
    val fixedEdges = node.graph.getEdges() - node.rawDetails.map { it.toEdge() }.toSet()
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
