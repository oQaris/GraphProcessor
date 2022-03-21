package graphs

import graphs.GraphException.Companion.ERR_SIZE_SQ
import storage.Generator
import storage.standardToString

class EdgeListGraph(
    override val name: String
) : Graph {

    override var oriented: Boolean = false

    override val numVer: Int
        get() = buildSet {
            edgesList.forEach {
                add(it.first)
                add(it.second)
            }
        }.size

    override val numEdg: Int
        get() = edgesList.size

    override val sumWeights: Int
        get() = edgesList.sumOf { it.weight }

    lateinit var edgesList: MutableList<Edge>

    constructor(name: String, size: Int) : this(name) {
        require(size > 0) { ERR_SIZE_SQ }
        edgesList = ArrayList(Generator.maxNumEdge(size))
        oriented = false
    }

    constructor(name: String, srcData: Array<Array<Int?>>) : this(name) {
        AdjacencyMatrixGraph.checkSize(srcData)
        edgesList = ArrayList(Generator.maxNumEdge(srcData.size))
        for (i in srcData.indices)
            for (j in srcData.indices)
                if (srcData[i][j] != null)
                    edgesList.add(i edg j w srcData[i][j]!!)
        oriented = checkOriented()
    }

    constructor(name: String, srcData: List<List<Int?>>) : this(name) {
        AdjacencyMatrixGraph.checkSize(srcData)
        edgesList = ArrayList(Generator.maxNumEdge(srcData.size))
        for (i in srcData.indices)
            for (j in srcData.indices)
                if (srcData[i][j] != null)
                    edgesList.add(i edg j w srcData[i][j]!!)
        oriented = checkOriented()
    }

    constructor(name: String, srcData: Array<Array<Boolean>>) : this(name) {
        AdjacencyMatrixGraph.checkSize(srcData)
        edgesList = ArrayList(Generator.maxNumEdge(srcData.size))
        for (i in srcData.indices)
            for (j in srcData.indices)
                if (srcData[i][j])
                    edgesList.add(i edg j w 1)
        oriented = checkOriented()
    }

    constructor(src: EdgeListGraph) : this(src.name) {
        edgesList = src.edgesList.toMutableList()
        oriented = src.oriented
    }

    constructor(src: Graph) : this(src.name) {
        edgesList = ArrayList(src.numEdg)
        src.getEdges().forEach {
            edgesList.add(it.toEdge(src.getWeightEdg(it)!!))
        }
        oriented = checkOriented()
    }

    override fun addVer(count: Int) {
    }

    override fun addEdg(u: Int, v: Int, weight: Int) {
        edgesList.add(u edg v w weight)
    }

    override fun getWeightEdg(u: Int, v: Int): Int? {
        return edgesList.find { it.first == u && it.second == v }?.weight
    }

    override fun remVer(ver: Int) {
        edgesList.removeIf { it.first == ver || it.second == ver }
    }

    override fun remEdg(u: Int, v: Int) {
        edgesList.removeIf { it.first == u && it.second == v }
    }

    override fun deg(ver: Int, isOut: Boolean): Int {
        return edgesList.count { if (isOut) it.first == ver else it.second == ver }
    }

    override fun com(ver: Int): MutableList<Int> {
        return edgesList.filter { it.first == ver }.map { it.second }.toMutableList()
    }

    override fun getEdges(): MutableList<Pair<Int, Int>> {
        return edgesList.map { it.first to it.second }.toMutableList()
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
}
