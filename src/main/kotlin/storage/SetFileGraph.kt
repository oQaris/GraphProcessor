package storage

import graphs.Graph
import graphs.GraphException
import java.io.File

class SetFileGraph(
    private var file: File,
    private val map: MutableMap<String, Graph>
) {
    val names = map.keys
    val graphs = map.values

    constructor(f: File, vararg graphs: Graph) :
            this(f, graphs.map { it.name }.zip(graphs).toMap().toMutableMap())

    constructor(f: File = File("GraphData")) : this(f, mutableMapOf())

    private fun pull() {
        file.bufferedReader().use { it.readText() }
            .split(":")
            .filter { it.isNotBlank() }
            .chunked(2)
            .map { parseAdjacencyMatrix(it[0], it[1]) }
            .forEach { map[it.name] = it }
    }

    fun push() {
        file.bufferedWriter().use { bw ->
            for (g in map.values) {
                bw.write(g.toString())
                bw.flush()
            }
        }
    }

    fun add(graph: Graph): Boolean {
        return map.put(graph.name, graph) == null
    }

    operator fun get(name: String) =
        map[name] ?: throw GraphException("The graph named $name is missing from the file $file")

    fun remove(name: String): Graph? {
        return map.remove(name)
    }

    init {
        if (!file.isFile)
            file.createNewFile()
        pull()
    }
}
