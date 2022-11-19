package algorithm

import graphs.Graph
import graphs.requireG
import graphs.toEdge

data class AugmentingPath(val value: Int, val path: List<Int>)
data class FlowResult(val value: Int, val flow: List<AugmentingPath>)

/**
 * Поиск максимального потока с помощью алгоритма Эдмондса-Карпа
 *
 * @param g     Сеть для поиска максимального потока.
 * @param start     Стартовая вершина.
 * @param end     Конечная вершина.
 * @return Величина максимального потока и список увеличивающих путей
 */
fun maxFlow(g: Graph, start: Int, end: Int): FlowResult {
    requireG(start != end) { "The vertices must be different." }
    var b = 0             // Величина потока
    val flow = g.clone()  // Поток
    val copy = g.clone()  // Остаточная сеть
    flow.oriented = true
    copy.oriented = true
    val out = ArrayList<AugmentingPath>(g.numVer)   // Список увеличивающих путей
    redo(flow) { _, _, _ -> 0 }                     // Обнуляем веса в графе потока

    var path = route(copy, start, end) // Ищем путь поиском в ширину
    while (path.isNotEmpty()) {
        // Определяем минимальную пропускную способность
        val delta = path.zipWithNext().minOf { copy.getWeightEdg(it)!! }
        b += delta
        // Заменяем дуги
        path.zipWithNext().forEach {
            // Если в исходном графе нет дуги, по которой проходит увеличивающий путь,
            // то уменьшаем вес симметричной дуги на сигму, если есть - увеличиваем прямую дугу
            if (flow.isCom(it))
                flow.addEdg(it.toEdge((flow.getWeightEdg(it) ?: 0) + delta))
            else flow.addEdg(it.toEdge((flow.getWeightEdg(it.inv()) ?: 0) - delta).revert())
            // Пересчитываем веса
            val reverseFlow = flow.getWeightEdg(it.inv()) ?: 0
            val newWeightUV = (copy.getWeightEdg(it) ?: 0) - delta + reverseFlow
            val newWeightVU = (copy.getWeightEdg(it.inv()) ?: 0) + delta - reverseFlow
            // Устанавливаем новые веса,
            // если вес обратился в 0 - удаляем дугу
            if (newWeightUV == 0)
                copy.remEdg(it)
            else copy.addEdg(it.toEdge(newWeightUV))
            if (newWeightVU == 0)
                copy.remEdg(it.inv())
            else copy.addEdg(it.toEdge(newWeightVU).revert())
        }
        out.add(AugmentingPath(delta, path)) // формируем объект со списком и величиной увеличивающего пути
        path = route(copy, start, end)       // находим новый увеличивающий путь
    }
    return FlowResult(b, out)
}

fun <A, B> Pair<A, B>.inv() = second to first
