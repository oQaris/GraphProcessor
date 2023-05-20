package console.algorithm.clustering

import algorithm.findComponents
import algorithm.isClustering
import algorithm.thesis.Event
import graphs.Edge
import graphs.Graph
import graphs.impl.AdjacencyMatrixGraph
import java.util.*

var globalID = 0L
val ascLastComparator: Comparator<Subgraph> =
    compareBy<Subgraph> { it.score }.reversed().thenBy { it.id }.reversed()

class Subgraph(val graph: Graph, var score: Int, val rawEdges: MutableList<Edge>, val id: Long = globalID++) {

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
        if (curElem.rawEdges.isEmpty())
            continue

        driver.invoke(Event.EXE)
        val edge = curElem.rawEdges.removeFirst()
        if (isRepresentative(curElem, maxSizeCluster))
            leaves.add(curElem)

        val newG = curElem.graph.clone().apply { remEdg(edge) }
        val newScore = curElem.score + 1

        if (isClustering(newG) && maxSizeComponent(findComponents(newG)) <= maxSizeCluster) {
            driver.invoke(Event.ADD)
            rec = newScore
            answer = newG
            leaves.removeIf { it.score >= rec }
        } else {
            val newNode = Subgraph(newG, newScore, curElem.rawEdges.toMutableList())
            leaves.add(newNode)
        }
    }
    driver.invoke(Event.OFF)
    return answer
}

/**
 * проверяет, что:
 * размер максимальной компоненты связности, образованной фиксированными рёбрами не больше maxSizeCluster;
 * выполняется критерий кластерности - нет незавершённых треугольников в эффективном графе.
 */
fun isRepresentative(node: Subgraph, maxSizeCluster: Int): Boolean {
    val fixedEdges = node.graph.getEdges() - node.rawEdges.toSet()
    val fixedSubgraph = slice(node.graph, fixedEdges)
    val components = findComponents(fixedSubgraph)
    return maxSizeComponent(components) <= maxSizeCluster
            && correctCriterionOfClustering(components, node.graph)
}

fun maxSizeComponent(components: IntArray) =
    components.groupBy { it }.maxOf { it.value.size }

/**
 * Критерий кластерности графа
 */
fun correctCriterionOfClustering(components: IntArray, origGraph: Graph): Boolean {
    return components.groupBy { it }
        .filter { it.value.size == 3 }.all { chain ->
            val numComponent = chain.value.first()
            val tripleV = components.indices
                .filter { idx -> components[idx] == numComponent }
            require(tripleV.size == 3)
            // Проверяем, что 3 вершины образуют треугольник
            tripleV.all { v ->
                origGraph.com(v).containsAll(tripleV.toSet() - v)
            }
        }
}

fun slice(graph: Graph, edges: List<Edge>) =
    AdjacencyMatrixGraph(graph.name, graph.numVer).apply {
        edges.forEach {
            addEdg(it)
        }
    }
