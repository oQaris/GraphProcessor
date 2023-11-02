package console.algorithm.clustering

import algorithm.findComponents
import algorithm.isClusteringMaxSize
import algorithm.thesis.Event
import com.github.shiguruikai.combinatoricskt.combinations
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
    override fun toString(): String {
        return "${graph.name}_$id//$score//$rawDetails"
    }
}

fun clustering(
    base: Graph,
    maxSizeCluster: Int,
    driver: (Event) -> Unit = {}
): Graph? {
    require(!base.oriented) { "Only non-orientated graphs are supported" }
    require(maxSizeCluster <= base.numVer) { "maxSizeCluster ($maxSizeCluster) > base.numVer (${base.numVer})" }
    val leaves = PriorityQueue(compareBy<Subgraph> { it.score }.reversed().thenBy { it.id }.reversed())
    var rec = Int.MAX_VALUE
    var answer: Graph? = null

    fun updateTree(newNode: Subgraph, proceedPair: Pair<Int, Int>) {
        if (newNode.score < rec && isClusteringMaxSize(newNode.graph, maxSizeCluster)) {
            driver.invoke(Event.REC)
            rec = newNode.score
            answer = newNode.graph
            leaves.removeIf { it.score >= rec }
        } else if (isValid(newNode, maxSizeCluster, rec)) {
            resortDetails(newNode, proceedPair)
            leaves.add(newNode)
        }
    }
    leaves.add(Subgraph(base.clone(), 0, base.getPairVer().toMutableList()))
    driver.invoke(Event.ON)

    while (leaves.isNotEmpty()) {
        val curElem = leaves.poll()

        driver.invoke(Event.EXE)
        val pair = curElem.rawDetails.removeFirst()

        // Удаление или добавление ребра
        val newG = curElem.graph.clone().apply {
            if (curElem.graph.isCom(pair)) remEdg(pair)
            else addEdg(pair.toEdge())
        }
        updateTree(Subgraph(newG, curElem.score + 1, curElem.rawDetails.toMutableList()), pair)

        // Фиксирование ребра
        updateTree(onFixingEdgePostprocess(curElem, maxSizeCluster), pair)
    }
    driver.invoke(Event.OFF)
    return answer
}

/**
 * Находит все кластеры размера [sizeCluster], образованные фиксированными рёбрами,
 * и удаляет из исходного графа все рёбра, смежные с составляющими кластер,
 * затем увеличивает оценку узла на число удалённых рёбер и удаляет рёбра из очереди необработанных.
 */
fun onFixingEdgePostprocess(node: Subgraph, sizeCluster: Int): Subgraph {
    val components = fixedEdgesComponents(node)
    val extraEdges = components.withIndex()
        .groupBy { it.value }
        .filter {
            // value - группа с одинаковыми iv.value (номером компоненты)
            it.value.size == sizeCluster &&
                    // проверка кластерности фиксированного подмножества
                    it.value.combinations(2).all { iv ->
                        // iv.index - номер вершины
                        iv[0].index to iv[1].index !in node.rawDetails
                    }
        }.flatMap { (_, curCmp) ->
            val curCmpVer = curCmp.map { it.index }.toSet()
            curCmpVer.flatMap { v ->
                // Рёбра, исходящие от кластера
                (node.graph.com(v) - curCmpVer).map { if (it < v) it to v else v to it }
            }
        }.toSet()
    return node.apply {
        graph.apply { extraEdges.forEach { remEdg(it) } }
        score += extraEdges.size
        rawDetails -= extraEdges
    }
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
 * 3) размер максимальной компоненты связности, образованной фиксированными рёбрами не больше maxSizeCluster;
 * 4) выполняется критерий кластерности.
 */
fun isValid(node: Subgraph, maxSizeCluster: Int, record: Int): Boolean {
    if (node.score >= record || node.rawDetails.isEmpty())
        return false
    val components = fixedEdgesComponents(node)
    return maxSizeComponent(components) <= maxSizeCluster
            && correctCriterionOfClustering(components, node.graph)
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
 * Найдём компоненты связности, порождённые множеством фиксированных рёбер, содержащие ровно 3 вершины
 * (обозначим их 1, 2 и 3), соединённых двумя рёбрами, б.о.о. (1,2) и (2,3).
 * Убедимся, что между крайними вершинами (1 и 3) существует ребро в [origGraph]
 */
fun correctCriterionOfClustering(components: IntArray, origGraph: Graph): Boolean {
    return components.withIndex()
        .groupBy { it.value }
        .filter { it.value.size == 3 }
        .all { (_, curCmp) ->
            val curCmpVer = curCmp.map { it.index }
            // Проверяем, что все 3 вершины смежны друг с другом в исходном графе
            // (если 2, то критерий не выполняется, а 1 быть не может, поскольку компонента связности размера 3)
            curCmpVer.combinations(2).all { vers ->
                origGraph.isCom(vers[0], vers[1])
            }
        }
}

fun slice(graph: Graph, edges: List<Edge>) =
    AdjacencyMatrixGraph(graph.name, graph.numVer).apply {
        edges.forEach {
            addEdg(it)
        }
    }
