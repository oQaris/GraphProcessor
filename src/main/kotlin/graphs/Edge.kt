package graphs

data class Edge(
    val first: Int,
    val second: Int,
    val weight: Int
) {

    /**
     * Returns string representation of the [Edge] including its [first] and [second] values.
     */
    override fun toString(): String = "($first, $second)"

    fun revert() = Edge(second, first, weight)
}

infix fun Int.edg(that: Int) = Edge(this, that, 0)

infix fun Edge.w(weight: Int) = Edge(first, second, weight)

infix fun Pair<Int, Int>.toEdge(weight: Int) = Edge(first, second, weight)