package algorithms

import graphs.Graph

/**
 * Нахождение маршрута из вершины start в end.
 * Используется метод обхода в ширину.
 * Веса не учитываются (ищется путь минимальный по количеству входящих в него вершин).
 * Если пути не существует, возвращается пустой список,
 * иначе - список из 2х и более элементов, где 1-й элемент - start последний - end.
 *
 * @param g     Граф, в котором производится поиск
 * @param start Начальная вершина
 * @param end   Конечная вершина
 * @return Список вершин, содержащий маршрут из начальной вершины в конечную
 */
fun route(g: Graph, start: Int, end: Int): MutableList<Int> {
    g.checkCorrectVer(start, end)
    val path = mutableListOf<Int>()     // Путь от начальной вершины к конечной
    val q = mutableListOf(start)        // Очередь вершин, которые предстоит посетить
    val used = BooleanArray(g.numVer)   // Массив посещённых вершин (для каждой отмечается, посетили её или нет)
    used[start] = true
    val parents = IntArray(g.numVer)    // Массив родителя каждой вершины (т.е. из какой вершины мы попали в данную)
    parents[start] = -1
    while (q.isNotEmpty()) {
        val curV = q.removeFirst()
        if (curV == end) {              // Если дошли до конечной вершины
            var v = end
            while (v != -1) {
                path.add(0, v)
                v = parents[v]
            }
            break
        }
        for (nxtV in g.com(curV)) {     // Из всех вершин смежных с данной
            if (!used[nxtV]) {          // Если вершина не посещённая
                used[nxtV] = true
                q.add(nxtV)
                parents[nxtV] = curV
            }
        }
    }
    return path
}
