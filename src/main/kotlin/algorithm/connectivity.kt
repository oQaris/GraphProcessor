package algorithm

import graphs.AdjacencyMatrixGraph
import graphs.Graph
import kotlin.math.min

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

fun connectivity(g: Graph, localConnectivity: ((g: Graph, u: Int, v: Int) -> Int)): Int {
    var minCon = Int.MAX_VALUE
    g.getPairVer().forEach { (u, v) ->
        minCon = min(minCon, localConnectivity(g, u, v))
        if (minCon == 0) return 0
    }
    return minCon
}

/**
 * Вычисление локальной вершинной связности путём поиска вершинно-независимых цепей с помощью нахождения максимального потока.
 *
 * @param g     Граф для которого определяется связность.
 * @param s     Стартовая вершина.
 * @param t     Конечная вершина.
 * @return Количество вершинно-независимых (s,t)-цепей в графе G.
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
