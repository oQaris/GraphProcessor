package graphs

data class Edge(
    val first: Int,
    val second: Int,
    val weight: Int
) {
    override fun toString(): String = "($first, $second)"

    /**
     * Меняет направление ребра
     */
    fun revert() = Edge(second, first, weight)

    /**
     * Преобразовывает ребро в пару вершин, отсекая информацию о весе
     */
    fun toPair() = first to second
}

/**
 * Создаёт новое ребро, соединяющее две вершины. Вес по-умолчанию равен 1.
 */
infix fun Int.edg(that: Int) = Edge(this, that, 1)

/**
 * Меняет вес созданного ребра.
 */
infix fun Edge.w(weight: Int) = Edge(first, second, weight)

/**
 * Преобразует пару вершин в ребро с заданным весом
 */
fun Pair<Int, Int>.toEdge(weight: Int) = Edge(first, second, weight)

/**
 * Преобразует пару вершин в ребро с единичным весом
 */
fun Pair<Int, Int>.toEdge() = Edge(first, second, 1)
