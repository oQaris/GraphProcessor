package graphs.impl

import graphs.*
import graphs.GraphException.Companion.ERR_SIZE_SQ
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

    override var numVer by Delegates.notNull<Int>()
    /*get() = buildSet {
        edgesList.forEach {
            add(it.first)
            add(it.second)
        }
    }.size*/

    override val numEdg: Int
        get() = edgesList.size

    override val sumWeights: Int
        get() = edgesList.sumOf { it.weight }

    private val comparator = Comparator.comparing<Edge?, Int?> { e -> e.first }
        .thenComparing { e -> e.second }
        .thenComparing { e -> e.weight }

    var edgesList: MutableSet<Edge> = TreeSet(comparator)

    constructor(name: String, size: Int) : this(name) {
        require(size > 0) { ERR_SIZE_SQ }
        numVer = size
        oriented = false
    }

    constructor(name: String, srcData: Array<Array<Int?>>) : this(name) {
        checkSize(srcData)
        for (i in srcData.indices)
            for (j in srcData.indices)
                if (srcData[i][j] != null)
                    edgesList.add(i edg j w srcData[i][j]!!)
        numVer = srcData.size
        oriented = checkOriented()
    }

    constructor(name: String, srcData: List<List<Int?>>) : this(name) {
        checkSize(srcData)
        for (i in srcData.indices)
            for (j in srcData.indices)
                if (srcData[i][j] != null)
                    edgesList.add(i edg j w srcData[i][j]!!)
        numVer = srcData.size
        oriented = checkOriented()
    }

    constructor(name: String, srcData: Array<Array<Boolean>>) : this(name) {
        checkSize(srcData)
        for (i in srcData.indices)
            for (j in srcData.indices)
                if (srcData[i][j])
                    edgesList.add(i edg j w 1)
        numVer = srcData.size
        oriented = checkOriented()
    }

    constructor(src: Graph) : this(src.name) {
        edgesList = TreeSet(comparator)
            .apply { addAll(src.getEdges()) }
        numVer = src.numVer
        oriented = src.oriented
    }

    override fun addVer(count: Int) {
        numVer += count
    }

    override fun addEdg(u: Int, v: Int, weight: Int) {
        checkCorrectVer(u, v)
        remEdg(u, v)
        if (!oriented) {
            val (min, max) = norm(u, v)
            edgesList.add(min edg max w weight)
        } else edgesList.add(u edg v w weight)
    }

    override fun getWeightEdg(u: Int, v: Int): Int? {
        checkCorrectVer(u, v)
        return edgesList.find {
            it.first == u && it.second == v
                    || (if (!oriented)
                it.first == v && it.second == u
            else false)
        }?.weight
    }

    override fun remVer(ver: Int) {
        checkCorrectVer(ver)
        edgesList.removeIf { it.first == ver || it.second == ver }
    }

    override fun remEdg(u: Int, v: Int) {
        checkCorrectVer(u, v)
        if (!oriented) {
            val (min, max) = norm(u, v)
            edgesList.removeIf { it.first == min && it.second == max }
        } else edgesList.removeIf { it.first == u && it.second == v }
    }

    override fun deg(ver: Int, isOut: Boolean): Int {
        checkCorrectVer(ver)
        return edgesList.count { if (isOut) it.first == ver else it.second == ver }
    }

    override fun com(ver: Int): MutableList<Int> {
        checkCorrectVer(ver)
        return edgesList.filter { it.first == ver }.map { it.second }.toMutableList()
    }

    override fun getEdges(): MutableList<Edge> {
        return edgesList.toMutableList()
    }

    override fun clone() = EdgeListGraph(this)

    private fun checkOriented(): Boolean {
        return edgesList.any { edgesList.contains(it.revert()) }
    }

    // Не менять, используется парсером
    override fun toString() = standardToString(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EdgeListGraph
        if (name != other.name) return false
        if (edgesList != other.edgesList) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + edgesList.hashCode()
        return result
    }

    // Задаёт естественный порядок для хранения в неориентированном графе
    private fun norm(u: Int, v: Int) = if (u < v) u to v else v to u
}
