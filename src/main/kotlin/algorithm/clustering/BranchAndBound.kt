package console.algorithm.clustering

import algorithm.findComponents
import algorithm.isCluster
import graphs.Edge
import graphs.Graph
import graphs.impl.EdgeListGraph
import java.util.*

var globalID = 0L
val ascLastComparator: Comparator<Subgraph> =
    compareBy<Subgraph> { it.score }.reversed().thenBy { it.id }.reversed()

class Subgraph(val graph: Graph, var score: Int, val rawEdges: MutableList<Edge>, val id: Long = globalID++) {

    override fun toString(): String {
        return "${graph.name}_$id//$score//$rawEdges"
    }
}

fun clustering(base: Graph, maxSizeCluster: Int): Graph? {
    require(maxSizeCluster <= 3)
    val leaves = PriorityQueue(ascLastComparator)
    var rec = Int.MAX_VALUE
    var answer: Graph? = null

    var exec = 0 //todo
    leaves.add(Subgraph(base.clone(), 0, base.getEdges().toMutableList()))
    while (leaves.isNotEmpty()) {
        val curElem = leaves.poll()
        if (curElem.score >= rec)
            break
        if (curElem.rawEdges.isEmpty())
            continue

        // проверяем, что размер максимальной компоненты связности,
        // образованной фиксированными рёбрами не больше maxSizeCluster
        val fixedEdges = curElem.graph.getEdges() - curElem.rawEdges.toSet()
        if (maxSizeComponent(slice(curElem.graph, fixedEdges)) > maxSizeCluster)
            continue

        exec++
        leaves.add(curElem)

        val edge = curElem.rawEdges.removeFirst()
        val newG = curElem.graph.clone().apply { remEdg(edge) }
        val newScore = curElem.score + 1
        // проверим, что граф кластерный
        if (isCluster(newG) && maxSizeComponent(newG) <= maxSizeCluster) {
            rec = newScore
            answer = newG
            //todo надо удалять не перспективные
        } else {
            leaves.add(Subgraph(newG, newScore, curElem.rawEdges.toMutableList()))
        }
    }
    println(exec)
    return answer
}

fun maxSizeComponent(graph: Graph) =
    findComponents(graph).groupBy { it }.maxOf { it.value.size }

fun slice(graph: Graph, edges: List<Edge>) =
    EdgeListGraph(graph.name, graph.numVer).apply {
        edges.forEach {
            addEdg(it)
        }
    }
