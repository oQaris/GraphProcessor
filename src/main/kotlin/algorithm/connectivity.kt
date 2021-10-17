package algorithm

import graphs.AdjacencyMatrixGraph
import graphs.Graph
import mu.KotlinLogging
import java.lang.Integer.max
import java.util.*
import kotlin.math.ceil
import kotlin.properties.Delegates

private val logger = KotlinLogging.logger {}

/**
 * Нахождение вершинной связности путём поиска минимальной локальной вершинной связности между каждой парой вершин в графе.
 *
 * @param g Граф для которого определяется связность
 * @return Глобальная вершинная k-связность графа G.
 */
fun vertexConnectivity(g: Graph): Int {
    return g.getPairVer().minOf { (u, v) ->
        localVertexConnectivity(g, u, v)
    }
}

/**
 * Нахождение рёберной связности путём поиска минимальной локальной рёберной связности между каждой парой вершин в графе.
 *
 * @param g Граф для которого определяется связность.
 * @return Глобальная рёберная k-связность графа G.
 */
fun edgeConnectivity(g: Graph): Int {
    return g.getPairVer().minOf { (u, v) ->
        localEdgeConnectivity(g, u, v)
    }
}

private fun connectivity(g: Graph, localConnectivity: ((g: Graph, u: Int, v: Int) -> Int)): Int {
    var minCon = Int.MAX_VALUE
    g.getPairVer().forEach { (u, v) ->
        minCon = localConnectivity(g, u, v)
        if (minCon == 0) return 0
    }
    return minCon
}

/**
 * Вычисление локальной вершинной связности путём поиска вершинно-независисмых цепей с помощью нахождения максимального потока.
 *
 * @param g     Граф для которого определяется связность.
 * @param s     Стартовая вершина.
 * @param t     Конечная вершина.
 * @return Количество вершинно-независисмых (s,t)-цепей в графе G.
 */
fun localVertexConnectivity(g: Graph, s: Int, t: Int): Int {
    val cpy = AdjacencyMatrixGraph(g)       // Создаём копию графа
    redo(cpy) { _, _, _ -> 1 }              // Устанавливаем веса равные единице
    cpy.oriented = true                   // Заменяем рёбра на пару симметричных дуг
    val oldNumVer = cpy.numVer
    cpy.addVer(oldNumVer - 2)         // Добавляем в граф n-2 вершин
    (0 until oldNumVer).minus(setOf(s, t))  // Для каждой из исходных вершин, кроме s и t
        .forEachIndexed { idx, ver ->
            val outVer = cpy.com(ver)
            val newVer = oldNumVer + idx
            cpy.addEdg(ver, newVer)     // Направляем дугу от i к n+i вершине
            outVer.forEach {
                cpy.remEdg(ver, it)     // Переносим все исходящие дуги из i-й вершины
                cpy.addEdg(newVer, it)  // В n+i-ю вершину
            }
            // Все входящие дуги остаются в i-й вершине
        }
    return maxFlow(cpy, s, t).value         // Возвращаем величину макс. потока в новом графе
}

/**
 * Вычисление локальной рёберной связности графа путём поиска рёберно-независисмых цепей с помощью нахождения максимального потока.
 *
 * @param g     Граф для которого определяется связность.
 * @param s     Стартовая вершина.
 * @param t     Конечная вершина.
 * @return Количество рёберно-независимых (s,t)-цепей в графе G=.
 */
fun localEdgeConnectivity(g: Graph, s: Int, t: Int): Int {
    val cpy = AdjacencyMatrixGraph(g)   // Создаём копию графа
    redo(cpy) { _, _, _ -> 1 }          // Устанавливаем веса равные единице
    cpy.oriented = true               // Заменяем рёбра на пару симметричных дуг
    return maxFlow(cpy, s, t).value     // Возвращаем величину макс. потока в новом графе
}

// ------------------------- Курсовая ------------------------- //

/**
 * Множество всех остовных подграфов заданной k-связности. Хранится исходный граф + список непройденных рёбер
 * (рёбра не входящие в список считаются фиксированными от удаления).
 */
data class Subgraph(
    val graph: Graph,
    val rawEdges: MutableList<Pair<Int, Int>>,
    val k: Int
) {
    var score by Delegates.notNull<Int>()

    /**
     * Функция пересчёта оценки при изменении графа. Из списка непройденных рёбер убираем те, удаление которых в исходном графе нарушит его k-связность.
     * @param removedEdge Удалённое ребро. Если не null, то будут рассматриваться только рёбра, инцидентные его концам.
     */
    fun updateScore(removedEdge: Pair<Int, Int>? = null) {
        if (removedEdge != null)
            remRawEdges(removedEdge)
        else graph.getEdges().forEach { remRawEdges(it) }

        score = max(
            if (k == 1) graph.numVer - 1
            else ceil(k * graph.numVer / 2.0).toInt(),
            graph.numEdg - rawEdges.size
        )
    }

    private fun remRawEdges(removedEdge: Pair<Int, Int>) {
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
}

class Record {
    var value = 0
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
    localConnectivity: ((Graph, Int, Int) -> Int) = ::localEdgeConnectivity,
    valRecord: Record = Record()
): Pair<Graph, Pair<Pair<Long, Long>, Long>> {

    require(k > 0)
    require(connectivity(g, localConnectivity) >= k) { "Граф должен иметь связность >= $k" }
    var rec = g.numEdg
    valRecord.value = rec
    var minG = g
    var id = 0L
    val startTime = System.currentTimeMillis()
    var timeRec = startTime
    var prevTimeRec = startTime
    var prevPrevTimeRec = startTime

    val leaves = TreeSet(Comparator
        .comparing(Subgraph::score)
        //.reversed() //если раскомментировать, то будут в начале графы с максимальной оценкой
        .thenComparing { sub -> sub.rawEdges.size }
        .thenComparing { sub -> sub.graph.name })

    leaves.add(Subgraph(g, g.getEdges().sortEdges(g), k).apply { updateScore() }
        .apply { logger.debug { "Оценка исходного графа $score" } })

    try {
        while (leaves.isNotEmpty()) {
            val curElem = leaves.pollFirst()!!

            if (curElem.score >= rec)
                break

            val (curG, curEdges) = curElem
            if (curEdges.isEmpty())
                continue

            val edge = curEdges.removeLast()

            if (localConnectivity(curG, edge.first, edge.second) > k) {

                val newG = AdjacencyMatrixGraph(curG).apply {
                    remEdg(edge)
                    name = id++.toString()
                }
                val numEdges = newG.numEdg

                logger.debug { "Удалили ребро $edge у графа ${newG.name}. Нефиксированные рёбра: ${curElem.rawEdges}" }
                val nm = Subgraph(newG, curEdges.sortEdges(newG), k).apply { updateScore(edge) }
                logger.debug { "Оценка получившегося графа ${nm.score}" }

                if (numEdges < rec) {
                    rec = numEdges
                    valRecord.value = rec
                    logger.debug { "Теперь рекорд $rec" }
                    minG = newG
                    prevPrevTimeRec = prevTimeRec
                    prevTimeRec = timeRec
                    timeRec = System.currentTimeMillis()
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
    }
    logger.info { "Рекорд: $rec" }
    logger.info { "Исходный граф: $g" }
    logger.info { "Получившийся граф: $minG" }
    return minG to ((prevPrevTimeRec - startTime to prevTimeRec - startTime)
            to System.currentTimeMillis() - startTime)
}

fun List<Pair<Int, Int>>.sortEdges(g: Graph) = LinkedList(
    this.shuffled()/*.sortedWith(Comparator
        .comparing<Pair<Int, Int>?, Int?> { (u, v) -> min(g.deg(u), g.deg(v)) }
        .thenComparing { (u, v) -> g.deg(u) + g.deg(v) }
        .reversed())*/
)
