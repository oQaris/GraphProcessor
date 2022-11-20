package graphs.impl

import graphs.*
import storage.standardToString
import java.util.*
import kotlin.properties.Delegates

/**
 * Реализация графа на основе списка рёбер
 */
class EdgeListGraph(
    override val name: String
) : Graph {

    override var oriented: Boolean = false
    //todo set

    override var numVer by Delegates.notNull<Int>()

    override val numEdg: Int
        get() = edgesSet.size

    override val sumWeights: Int
        get() = edgesSet.sumOf { it.weight }

    private val comparator = Comparator.comparing<Edge?, Int?> { e -> e.first }
        .thenComparing { e -> e.second }
        .thenComparing { e -> e.weight }

    var edgesSet: MutableSet<Edge> = TreeSet(comparator)

    // ---------------------------------- Конструкторы --------------------------------- //

    constructor(name: String, size: Int) : this(name) {
        requireG(size > 0) { ERR_SIZE_EM }
        numVer = size
        oriented = false
    }

    constructor(name: String, srcData: List<List<Int?>>) : this(name) {
        checkSize(srcData)
        oriented = isOriented(srcData)
        for (i in srcData.indices)
            for (j in srcData.indices)
                if (srcData[i][j] != null) {
                    val edge = i edg j w srcData[i][j]!!
                    if (oriented) edgesSet.add(edge)
                    else edgesSet.add(norm(edge))
                }
        numVer = edgesSet.flatMap {
            listOf(it.first, it.second)
        }.toSet().size
    }

    constructor(name: String, srcData: Array<Array<Int?>>)
            : this(name, srcData.map { it.asList() })

    constructor(name: String, srcData: Array<Array<Boolean>>)
            : this(name, srcData.map { row -> row.map { if (it) 1 else 0 } })

    constructor(src: Graph) : this(src.name) {
        edgesSet = TreeSet(comparator)
            .apply { addAll(src.getEdges()) }
        numVer = src.numVer
        oriented = src.oriented
    }

    override fun clone() = EdgeListGraph(this)

    // ---------------------------------- Методы интерфейса --------------------------------- //

    override fun addVer(count: Int) {
        numVer += count
    }

    override fun addEdg(u: Int, v: Int, weight: Int) {
        checkCorrectVer(u, v)
        remEdg(u, v)
        if (!oriented) {
            val (min, max) = norm(u, v)
            edgesSet.add(min edg max w weight)
        } else edgesSet.add(u edg v w weight)
    }

    override fun getWeightEdg(u: Int, v: Int): Int? {
        checkCorrectVer(u, v)
        return edgesSet.find {
            it.first == u && it.second == v
                    || (if (!oriented)
                it.first == v && it.second == u
            else false)
        }?.weight
    }

    override fun remVer(ver: Int) {
        checkCorrectVer(ver)
        edgesSet.removeIf { it.first == ver || it.second == ver }
    }

    override fun remEdg(u: Int, v: Int) {
        checkCorrectVer(u, v)
        if (!oriented) {
            val (min, max) = norm(u, v)
            edgesSet.removeIf { it.first == min && it.second == max }
        } else edgesSet.removeIf { it.first == u && it.second == v }
    }

    override fun deg(ver: Int, isOut: Boolean): Int {
        checkCorrectVer(ver)
        return edgesSet.count { if (isOut) it.first == ver else it.second == ver }
    }

    override fun com(ver: Int): MutableList<Int> {
        checkCorrectVer(ver)
        return edgesSet.filter { it.first == ver }.map { it.second }.toMutableList()
    }

    override fun getEdges(): MutableList<Edge> {
        return edgesSet.toMutableList()
    }

    // Задаёт естественный порядок для хранения в неориентированном графе
    private fun norm(u: Int, v: Int) = if (u < v) u to v else v to u

    private fun norm(edge: Edge): Edge {
        val (u, v) = norm(edge.first, edge.second)
        return u edg v w edge.weight
    }

    override fun toString() = standardToString(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EdgeListGraph
        if (name != other.name) return false
        if (edgesSet != other.edgesSet) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + edgesSet.hashCode()
        return result
    }
}
