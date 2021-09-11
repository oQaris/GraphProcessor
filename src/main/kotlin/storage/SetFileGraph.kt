package storage

import graphs.Graph
import java.io.File
import java.util.*

class SetFileGraph(private var file: File) {
    private val map: MutableMap<String, Graph>

    constructor(f: File, vararg graphs: Graph) : this(f) {
        for (g in graphs) map[g.name] = g
    }

    private fun updateGFS() {
        file.bufferedReader().use { it.readText() }
            .split(":")
            .filter { it.isNotBlank() }
            .chunked(2)
            .map { parseAdjacencyMatrix(it[0], it[1]) }
            .forEach { map[it.name] = it }
    }

    fun writeAllToFile() {
        file.bufferedWriter().use { bw ->
            for (g in map.values) {
                val size = g.numVer
                bw.append(":").append(g.name).append(":")
                bw.newLine()
                for (i in 0 until size) {
                    for (j in 0 until size)
                        bw.append(if (g.isCom(i, j)) g.getWeightEdg(i, j).toString() else "-").write(" ")
                    bw.newLine()
                }
                bw.flush()
            }
        }
    }

    fun add(graph: Graph): Boolean {
        return map.put(graph.name, graph) == null
    }

    operator fun get(name: String) = map[name]

    fun remove(name: String): Graph? {
        return map.remove(name)
    }

    val names: Set<String>
        get() = map.keys
    val graphs: MutableCollection<Graph>
        get() = map.values

    private fun isNormName(name: String?): Boolean {
        return name != null && name.isNotEmpty() && !name.contains(" ") && !idOfNew.contains(name.lowercase(Locale.getDefault()))
    }

    companion object {
        val idOfNew: Set<String> = HashSet(listOf("new", "новый", "создать"))

        @JvmStatic
        fun isNewName(name: String): Boolean {
            return idOfNew.contains(name.trim().lowercase(Locale.getDefault()))
        }
    }

    init {
        if (!file.isFile)
            file.createNewFile()
        map = LinkedHashMap()
        updateGFS()
    }
}
