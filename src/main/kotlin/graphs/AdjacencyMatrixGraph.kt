package graphs

import java.util.*

/**
 * Реализация графа на базе матрицы смежности
 */
class AdjacencyMatrixGraph(override var name: String) : Graph {
    override var oriented: Boolean = false
        set(value) {
            if (field && !value)
                for (i in data.indices)
                    for (j in 0 until i) {
                        if (data[i][j] == data[j][i]) continue
                        val dIJ = if (data[i][j] == null) Int.MIN_VALUE else data[i][j]
                        val dJI = if (data[j][i] == null) Int.MIN_VALUE else data[i][j]
                        if (dIJ!! > dJI!!) data[j][i] = dIJ else data[i][j] = dJI
                    }
            field = value
        }
    private lateinit var data: Array<Array<Int?>>

    override var numVer: Int = 0
        get() = data.size

    override var numEdg: Int = 0
        get() {
            var sumEdg = 0
            for (i in data.indices)
                for (j in 0 until i) {
                    if (data[i][j] != null)
                        sumEdg++
                    if (oriented && data[j][i] != null)
                        sumEdg++
                }
            return sumEdg
        }

    //todo избавиться от дублирования кода как то
    private fun <T> checkSize(srcData: List<List<T>>) {
        require(srcData.isNotEmpty()) { ERR_SIZE_EM }
        srcData.forEach {
            require(srcData.size == it.size) { ERR_SIZE_SQ }
        }
    }

    private fun <T> checkSize(srcData: Array<Array<T>>) {
        require(srcData.isNotEmpty()) { ERR_SIZE_EM }
        srcData.forEach {
            require(srcData.size == it.size) { ERR_SIZE_SQ }
        }
    }

    constructor(name: String, size: Int) : this(name) {
        require(size > 0) { ERR_SIZE_SQ }
        data = Array(size) { arrayOfNulls(size) }
        oriented = false
    }

    constructor(name: String, srcData: Array<Array<Int?>>, unsafe: Boolean = false) : this(name) {
        data = if (unsafe) srcData
        else {
            checkSize(srcData)
            cloneArray(srcData)
        }
        oriented = checkOriented()
    }

    constructor(name: String, srcData: List<List<Int?>>) : this(name) {
        checkSize(srcData)
        data = Array(srcData.size) { arrayOfNulls<Int?>(srcData.size) }
        for (i in data.indices) for (j in data.indices) data[i][j] = srcData[i][j]
        oriented = checkOriented()
    }

    constructor(name: String, srcData: Array<Array<Boolean>>) : this(name) {
        checkSize(srcData)
        data = Array(srcData.size) { arrayOfNulls<Int?>(srcData.size) }
        for (i in data.indices) for (j in data.indices) if (srcData[i][j]) data[i][j] = 1
        oriented = checkOriented()
    }

    constructor(src: AdjacencyMatrixGraph) : this(src.name) {
        oriented = src.oriented
        data = cloneArray(src.data)
    }

    constructor(src: Graph) : this(src.name) {
        data = Array(src.numVer) { arrayOfNulls<Int?>(src.numVer) }
        for (i in data.indices)
            for (j in data.indices)
                data[i][j] = src.getWeightEdg(i, j)
        oriented = checkOriented()
    }

    override fun getWeightEdg(u: Int, v: Int): Int? {
        checkCorrectVer(u, v)
        return data[u][v]
    }

    override fun addVer(count: Int) {
        require(count >= 0) { "The number of vertices added must be non-negative." }
        val n = data.size + count
        val dataCpy = Array(n) { arrayOfNulls<Int?>(n) }
        for (i in data.indices)
            System.arraycopy(data[i], 0, dataCpy[i], 0, data[i].size)
        data = dataCpy
    }

    override fun addEdg(u: Int, v: Int, weight: Int) {
        checkCorrectVer(u, v)
        data[u][v] = weight
        if (!oriented) data[v][u] = weight
    }

    /**
     * Получить копию матрицы смежности
     *
     * @return Двумерный массив Integer, содержащий матрицу смежности, где arr[i][j]==null означает, что вершины i и j не смежны,
     * иначе - указан вес ребра ij (для невзвешанного графа - 1)
     */
    val matrix: Array<Array<Int?>>
        get() = cloneArray(data)


    private fun checkOriented(): Boolean {
        for (i in data.indices) {
            for (j in 0 until i) {
                if (data[j][i] != data[i][j]) return true
            }
        }
        return false
    }

    override fun deg(ver: Int, isOut: Boolean): Int {
        checkCorrectVer(ver)
        var deg = 0
        if (isOut) for (i in data.indices) {
            if (data[ver][i] != null) deg++
        } else for (i in data.indices) {
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

    override fun getEdges(): MutableList<Pair<Int, Int>> {
        val out: MutableList<Pair<Int, Int>> = ArrayList()
        for (i in data.indices) {
            for (j in (if (oriented) 0 else i + 1) until data.size) {
                if (isCom(i, j)) out.add(i to j)
            }
        }
        return out
    }

    override fun remVer(ver: Int) {
        checkCorrectVer(ver)
        for (i in data.indices) for (j in data.indices) if (i == ver || j == ver) data[i][j] = null
    }

    override fun remEdg(u: Int, v: Int) {
        checkCorrectVer(u, v)
        if (oriented) data[u][v] = null else {
            data[u][v] = null
            data[v][u] = null
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        val size = numVer
        sb.append(name).append(": ").append(size)
        sb.append("\n")
        for (i in 0 until size) {
            for (j in 0 until size) sb.append(if (isCom(i, j)) getWeightEdg(i, j) else "-").append(" ")
            sb.append("\n")
        }
        return sb.toString()
    }

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

    companion object {
        val ERR_SIZE_EM = "The adjacency matrix of a graph must not be empty."
        val ERR_SIZE_SQ = "The adjacency matrix of the graph must be square."

        private fun cloneArray(src: Array<Array<Int?>>): Array<Array<Int?>> {
            val target = Array<Array<Int?>>(src.size) { arrayOfNulls(src[0].size) }
            for (i in src.indices)
                System.arraycopy(src[i], 0, target[i], 0, src[i].size)
            return target
        }
    }
}
