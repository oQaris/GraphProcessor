package graphs.impl

import graphs.*
import storage.standardToString
import java.util.*

/**
 * Реализация графа на базе матрицы смежности
 */
class AdjacencyMatrixGraph : Graph {
    override val name: String
    override var oriented: Boolean = false
        set(value) {
            // делаем неориентированным
            if (field && !value)
                getPairVer().forEach { (i, j) ->
                    val dIJ = data[i][j] ?: Int.MIN_VALUE
                    val dJI = data[j][i] ?: Int.MIN_VALUE
                    // Устанавливаем большее значение
                    if (dIJ > dJI) data[j][i] = dIJ else data[i][j] = dJI
                }
            field = value
        }

    private lateinit var data: Array<Array<Int?>>

    override val numVer: Int
        get() = data.size

    override var numEdg: Int = 0

    override var sumWeights: Int = 0

    // ---------------------------------- Конструкторы --------------------------------- //

    private constructor(name: String) {
        this.name = name
    }

    constructor(name: String, size: Int) : this(name) {
        requireG(size > 0) { ERR_SIZE_EM }
        data = Array(size) { arrayOfNulls(size) }
        oriented = false
        recalcStats()
    }

    constructor(name: String, srcData: Array<Array<Int?>>, unsafe: Boolean = false) : this(name) {
        data = if (unsafe) srcData
        else {
            checkSize(srcData)
            cloneArray(srcData)
        }
        oriented = checkOriented()
        recalcStats()
    }

    constructor(name: String, srcData: List<List<Int?>>) : this(name) {
        checkSize(srcData)
        data = Array(srcData.size) { arrayOfNulls<Int?>(srcData.size) }
        for (i in data.indices)
            for (j in data.indices)
                data[i][j] = srcData[i][j]
        oriented = checkOriented()
        recalcStats()
    }

    constructor(name: String, srcData: Array<Array<Boolean>>)
            : this(name, srcData.map { row -> row.map { if (it) 1 else 0 } })

    constructor(src: AdjacencyMatrixGraph) : this(src.name) {
        oriented = src.oriented
        data = cloneArray(src.data)
        recalcStats()
    }

    constructor(src: Graph) : this(src.name) {
        data = Array(src.numVer) { arrayOfNulls<Int?>(src.numVer) }
        for (i in data.indices)
            for (j in data.indices)
                data[i][j] = src.getWeightEdg(i, j)
        oriented = checkOriented()
        recalcStats()
    }

    override fun clone() = AdjacencyMatrixGraph(this)

    private fun recalcStats() {
        numEdg = getPairVer().count { (i, j) -> data[i][j] != null }
        sumWeights = getEdges().sumOf { (i, j) -> data[i][j]!! }
    }

    // ---------------------------------- Методы интерфейса --------------------------------- //

    override fun getWeightEdg(u: Int, v: Int): Int? {
        checkCorrectVer(u, v)
        return data[u][v]
    }

    override fun addVer(count: Int) {
        requireG(count >= 0) { "The number of vertices added must be non-negative." }
        val n = data.size + count
        val dataCpy = Array(n) { arrayOfNulls<Int?>(n) }
        for (i in data.indices)
            System.arraycopy(data[i], 0, dataCpy[i], 0, data[i].size)
        data = dataCpy
    }

    override fun addEdg(u: Int, v: Int, weight: Int) {
        checkCorrectVer(u, v)

        sumWeights +=
            if (data[u][v] == null) {
                numEdg++
                weight
            } else weight - data[u][v]!!

        data[u][v] = weight
        if (!oriented)
            data[v][u] = weight
    }

    override fun deg(ver: Int, isOut: Boolean): Int {
        checkCorrectVer(ver)
        var deg = 0
        if (isOut) // оптимизация для скорости работы, чтобы не делать проверку в цикле
            for (i in data.indices) {
                if (data[ver][i] != null) deg++
            }
        else
            for (i in data.indices) {
                if (data[i][ver] != null) deg++
            }
        return deg
    }

    override fun com(ver: Int): LinkedList<Int> {
        checkCorrectVer(ver)
        val com = LinkedList<Int>()
        for (i in data.indices) {
            if (i != ver && data[ver][i] != null)
                com.add(i)
        }
        return com
    }

    fun inVer(ver: Int): LinkedList<Int> {
        checkCorrectVer(ver)
        val com = LinkedList<Int>()
        for (i in data.indices) {
            if (i != ver && data[i][ver] != null)
                com.add(i)
        }
        return com
    }

    override fun getEdges(): MutableList<Edge> {
        val out: MutableList<Edge> = ArrayList()
        for (i in data.indices) {
            for (j in (if (oriented) 0 else i + 1) until data.size) {
                if (isCom(i, j))
                    out.add(i edg j w data[i][j]!!)
            }
        }
        return out
    }

    override fun remVer(ver: Int) {
        checkCorrectVer(ver)
        getPairVer().forEach { (u, v) ->
            if (u == ver || v == ver)
                remEdgDecStats(u, v)
        }
    }

    override fun remEdg(u: Int, v: Int) {
        checkCorrectVer(u, v)
        remEdgDecStats(u, v)
    }

    private fun remEdgDecStats(u: Int, v: Int) {
        if (data[u][v] != null) {
            numEdg--
            sumWeights -= data[u][v]!!
        }
        data[u][v] = null
        if (!oriented)
            data[v][u] = null
    }

    private fun checkOriented(): Boolean {
        for (i in data.indices)
            for (j in 0 until i)
                if (data[j][i] != data[i][j]) return true
        return false
    }

    /**
     * Получить копию *матрицы смежности*
     *
     * @return Двумерный массив [Integer], содержащий матрицу смежности, где ```matrix[i][j]==null``` означает,
     * что вершины [i] и [j] не смежны, иначе - указан вес ребра ij (для невзвешенного графа - 1)
     */
    val matrix: Array<Array<Int?>>
        get() = cloneArray(data)

    override fun toString() = javaClass.simpleName + standardToString(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as AdjacencyMatrixGraph
        return data.size == that.data.size && oriented == that.oriented &&
                data.contentDeepEquals(that.data)
    }

    override fun hashCode(): Int {
        var result = Objects.hash(name, data.size, oriented)
        result = 31 * result + data.contentDeepHashCode()
        return result
    }
}
