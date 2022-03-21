package storage

import graphs.Graph
import graphs.GraphException
import java.io.File
import java.io.FileOutputStream

class SetFileGraph(
    private var file: File,
    private val map: MutableMap<String, Graph>,
    pullOldData: Boolean
) : MutableMap<String, Graph> by map {

    init {
        if (!file.isFile) file.createNewFile()
        if (pullOldData) pull()
    }

    constructor(f: File, vararg graphs: Graph, pullOldData: Boolean = true) :
            this(f, graphs.map { it.name }.zip(graphs).toMap().toMutableMap(), pullOldData)

    constructor(f: File = File("GraphData"), pullOldData: Boolean = true) :
            this(f, mutableMapOf(), pullOldData)

    private fun pull() {
        file.bufferedReader().use { it.readText() }
            .split(":")
            .asSequence() // TODO: Провести тесты на скорость
            .filter { it.isNotBlank() }
            .chunked(2)
            .map { parse(it[0], it[1]) }
            .forEach { map[it.name] = it }
    }

    fun push(append: Boolean = false) {
        FileOutputStream(file, append)
            .bufferedWriter().use { bw ->
                for (g in map.values) {
                    bw.write(g.toString())
                    bw.flush()
                }
            }
    }

    fun add(graph: Graph): Boolean {
        return map.put(graph.name, graph) == null
    }

    override operator fun get(key: String) =
        map[key] ?: throw GraphException("The graph named $key is missing from the file $file")
}
