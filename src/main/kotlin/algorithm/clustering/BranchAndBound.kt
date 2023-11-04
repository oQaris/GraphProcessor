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
    private val rawDetails: MutableList<Pair<Int, Int>>,
    val id: Long = globalID++
) {
    //todo защита от изменения
    var fixCmp: IntArray = fixedEdgesComponents()

    fun isTerminal() = rawDetails.isEmpty()

    fun fixNextDetail(): Pair<Int, Int> {
        return rawDetails.removeFirst().also { pair ->
            if (graph.isCom(pair))
                fixCmp = fixedEdgesComponents()
        }
    }

    fun fixDetails(otherDetails: Collection<Pair<Int, Int>>) {
        if (rawDetails.removeAll(otherDetails))
            fixCmp = fixedEdgesComponents()
    }

    fun copyRawDetails() = rawDetails.toMutableList()

    fun isFixed(other: Pair<Int, Int>) = !rawDetails.contains(other)

    private fun fixedEdgesComponents(): IntArray {
        val fixedEdges = graph.getEdges() - rawDetails.map { it.toEdge() }.toSet()
        val fixedSubgraph = slice(graph, fixedEdges)
        return findComponents(fixedSubgraph)
    }

    /**
     * Сортирует детали в подграфе в порядке:
     * 1) рёбра смежные с обработанным
     * 2) нерёбра смежные с обработанным
     * 3) рёбра не смежные с обработанным
     * 4) нерёбра несмежные с обработанным
     */
    fun resortDetails(pair: Pair<Int, Int>) {
        val partResult = Array(4) { mutableListOf<Pair<Int, Int>>() }
        rawDetails.forEach { detail ->
            val isAdjacent = detail.first == pair.first || detail.second == pair.first
                    || detail.first == pair.second || detail.second == pair.second
            val idx = if (graph.isCom(detail)) {
                if (isAdjacent) 0 else 2
            } else {
                if (isAdjacent) 1 else 3
            }
            partResult[idx].add(detail)
        }
        rawDetails.clear()
        rawDetails.addAll(partResult.asList().flatten())
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
    require(!base.oriented) { "Only non-orientated graphs are supported" }
    require(maxSizeCluster <= base.numVer) { "maxSizeCluster ($maxSizeCluster) > base.numVer (${base.numVer})" }
    globalID = 0L
    val leaves = PriorityQueue(minScoreComparator())
    var rec = Int.MAX_VALUE
    var answer: Graph? = null

    fun updateTree(newNode: Subgraph, proceedPair: Pair<Int, Int>) {
        //todo Если последнее увеличение оценки происходило не более чем на 1, то на этом этапе алгоритм можно завершить,
        // поскольку все последующие итерации приведут лишь к увеличению оценок подмножеств минимум на 1.
        if (newNode.score < rec && isClusteringMaxSize(newNode.graph, maxSizeCluster)) {
            driver.invoke(Event.REC)
            rec = newNode.score
            answer = newNode.graph
            leaves.removeIf { it.score >= rec }
        } else if (isValid(newNode, maxSizeCluster, rec)) {
            newNode.resortDetails(proceedPair)
            leaves.add(newNode)
        }
    }
    leaves.add(Subgraph(base.clone(), 0, base.getPairVer().toMutableList()))
    driver.invoke(Event.ON)

    while (leaves.isNotEmpty()) {
        val curElem = leaves.poll()

        driver.invoke(Event.EXE)
        val pair = curElem.fixNextDetail()

        // Удаление или добавление ребра
        val newG = curElem.graph.clone().apply {
            if (isCom(pair)) remEdg(pair)
            else addEdg(pair.toEdge())
        }
        val changed = Subgraph(newG, curElem.score + 1, curElem.copyRawDetails())

        if (!curElem.graph.isCom(pair)) {
            // Если было добавление, то проводим те же операции, что и при фиксировании ребра
            updateTree(onFixingEdgePostprocess(changed, maxSizeCluster), pair)
            // Фиксирование пары вершин
            updateTree(curElem, pair)
        } else {
            // Удаление ребра
            updateTree(changed, pair)
            // Если фиксируется ребро
            updateTree(onFixingEdgePostprocess(curElem, maxSizeCluster), pair)
        }
    }
    driver.invoke(Event.OFF)
    return answer
}

fun minScoreComparator(): Comparator<Subgraph> {
    return compareBy<Subgraph> { it.score }.reversed().thenBy { it.id }.reversed()
}

/**
 * Находит все кластеры размера [sizeCluster], образованные фиксированными рёбрами,
 * и удаляет из исходного графа все рёбра, смежные с составляющими кластер,
 * затем увеличивает оценку узла на число удалённых рёбер и удаляет рёбра из очереди необработанных.
 */
fun onFixingEdgePostprocess(node: Subgraph, sizeCluster: Int): Subgraph {
    val extraEdges = node.fixCmp.withIndex()
        .groupBy { it.value }
        .filter {
            // value - группа с одинаковыми iv.value (номером компоненты)
            it.value.size == sizeCluster &&
                    // проверка кластерности фиксированного подмножества
                    it.value.combinations(2).all { iv ->
                        // iv.index - номер вершины
                        val edge = iv[0].index to iv[1].index
                        node.isFixed(edge) && node.graph.isCom(edge)
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
        fixDetails(extraEdges)
    }
}

/**
 * проверяет, что:
 * 1) узел содержит необработанные рёбра;
 * 2) расстояние до исходного графа меньше текущего рекорда;
 * 3) размер максимальной компоненты связности, образованной фиксированными рёбрами не больше maxSizeCluster;
 * 4) выполняется критерий кластерности.
 */
fun isValid(node: Subgraph, maxSizeCluster: Int, record: Int): Boolean {
    if (node.score >= record || node.isTerminal())
        return false
    return maxSizeComponent(node.fixCmp) <= maxSizeCluster
            //&& correctCriterionOfClustering(node.fixCmp, node.graph)
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
    //todo фиксировать сразу последнюю
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
