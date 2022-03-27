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
infix fun Pair<Int, Int>.toEdge(weight: Int) = Edge(first, second, weight)
