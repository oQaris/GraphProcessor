package storage

import graphs.Graph
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*

class SetFileGraph(private var file: File) {
    private val map: MutableMap<String, Graph?>

    constructor(f: File, vararg graphs: Graph) : this(f) {
        for (g in graphs) map[g.name] = g
    }

    private fun updateGFS() {
        file.bufferedReader().use { it.readText() }.split(":").chunked(2)
            .map { parseAdjacencyMatrix(it[0], it[1]) }.forEach { map[it.name] = it }
    }

    fun writeAllToFile() {
        try {
            BufferedWriter(OutputStreamWriter(FileOutputStream(file, false), StandardCharsets.UTF_8)).use { bw ->
                for (g in map.values) {
                    val size = g!!.numVer
                    bw.append(g.name).append(": ").write(size.toString())
                    bw.newLine()
                    for (i in 0 until size) {
                        for (j in 0 until size)
                            bw.append(if (g.isCom(i, j)) g.getWeightEdg(i, j).toString() else "-").write(" ")
                        bw.newLine()
                    }
                    bw.flush()
                }
            }
        } catch (e: IOException) {
            throw IllegalArgumentException("Нет доступа к файлу!")
        }
    }

    fun add(graph: Graph): Boolean {
        return map.put(graph.name, graph) == null
    }

    operator fun get(name: String): Graph {
        return map[name] ?: throw IllegalArgumentException("Граф с данным именем не найден!")
    }

    fun remove(name: String): Graph? {
        return map.remove(name)
    }

    val names: Set<String>
        get() = map.keys
    val graphs: MutableCollection<Graph?>
        get() = map.values

    private fun isNormName(name: String?): Boolean {
        return name != null && name.isNotEmpty() && !name.contains(" ") && !idOfNew.contains(name.toLowerCase(Locale.getDefault()))
    }

    companion object {
        val idOfNew: Set<String> = HashSet(listOf("new", "новый", "создать"))

        @JvmStatic
        fun isNewName(name: String): Boolean {
            return idOfNew.contains(name.trim { it <= ' ' }.toLowerCase(Locale.getDefault()))
        }
    }

    init {
        map = LinkedHashMap()
        updateGFS()
    }
}
