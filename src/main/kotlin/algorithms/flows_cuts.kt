package algorithms

import graphs.AdjacencyMatrixGraph
import graphs.Graph

data class AugmentingPath(val value: Int, val path: List<Int>)
data class FlowResult(val value: Int, val flow: List<AugmentingPath>)

fun <A, B> Pair<A, B>.inv() = second to first

fun maxFlow(g: Graph, start: Int, end: Int): FlowResult {
    require(start != end) { "Вершины должны быть различны!" }
    var b = 0                           // Величина потока
    val flow = AdjacencyMatrixGraph(g)  // Поток
    val copy = AdjacencyMatrixGraph(g)  // Остаточная сеть
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
                flow.setWeightEdg(it, flow.getWeightEdg(it) ?: 0 + delta)
            else flow.setWeightEdg(it.inv(), flow.getWeightEdg(it.inv()) ?: 0 - delta)
            // Пересчитываем веса
            val reverseFlow = flow.getWeightEdg(it.inv()) ?: 0
            val newWeightUV = (copy.getWeightEdg(it) ?: 0) - delta + reverseFlow
            val newWeightVU = (copy.getWeightEdg(it.inv()) ?: 0) + delta - reverseFlow
            // Устанавливаем новые веса,
            // если вес обратился в 0 - удаляем дугу
            if (newWeightUV == 0) copy.remEdg(it) else copy.setWeightEdg(it, newWeightUV)
            if (newWeightVU == 0) copy.remEdg(it.inv()) else copy.setWeightEdg(it.inv(), newWeightVU)
        }
        out.add(AugmentingPath(delta, path)) // формируем объект со списком и величиной увеличивающего пути
        path = route(copy, start, end) // находим новый увеличивающий путь
    }
    return FlowResult(b, out)
}
