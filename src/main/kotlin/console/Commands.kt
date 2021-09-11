package console

import algorithms.*
import com.udojava.evalex.Expression
import graphs.Graph
import interactive.GPInterface
import picocli.CommandLine.*
import storage.SetFileGraph
import java.io.File
import kotlin.math.roundToInt


@Command(
    name = "gp",
    version = ["GP 0.2.1"],
    subcommands = [New::class, Show::class, Remove::class, Connectivity::class, Planarity::class,
        Isomorphism::class, Path::class, MaxFlow::class, ECycle::class, HCycle::class, Redo::class, HelpCommand::class],
    description = ["A lightweight CLI-utility that can work with several graphs " +
            "(weighted, directed, multi-graphs are supported), find Hamiltonian and Euler cycles and chains, " +
            "build a search tree, find shortest paths and return the vertex and edge k-connectivity of a graph, " +
            "determine planarity, graph isomorphism and much more! (See https://github.com/oQaris/GraphProcessor)"],
    mixinStandardHelpOptions = true
)
class BaseCommand

val gfs = SetFileGraph(File("GraphData"))

internal class GraphConverter : ITypeConverter<Graph> {
    override fun convert(value: String) =
        gfs[value] ?: throw TypeConversionException("No graph named \"$value\" was found.")
}

internal class GraphCandidates : ArrayList<String>(gfs.names)

open class GraphParameter {
    @Parameters(
        index = "0", description = [DESCRIPTION_GRAPH],
        converter = [GraphConverter::class],
        completionCandidates = GraphCandidates::class
    )
    lateinit var graph: Graph
}

// -------------- Specific commands -------------- //

@Command(
    name = "new",
    description = ["Create and save a graph."]
)
class New : Runnable {
    override fun run() {
        gfs.add(GPInterface.newGraph())
        gfs.writeAllToFile()
    }
}

@Command(
    name = "show",
    description = ["Print one or more saved graphs."]
)
class Show : Runnable {

    class Exclusive {
        @Parameters(
            arity = "1..*",
            description = [DESCRIPTION_GRAPH],
            converter = [GraphConverter::class],
            completionCandidates = GraphCandidates::class
        )
        lateinit var graphs: Array<Graph>

        @Option(names = ["-a", "--all"], description = ["Вывести все сохранённые графы"])
        var isAll: Boolean = false

        @Option(names = ["-n", "--names"], description = ["Вывести только названия всех сохранённых графов"])
        var isOnlyNames: Boolean = false
    }

    @ArgGroup(exclusive = true, multiplicity = "1")
    lateinit var exclusive: Exclusive

    override fun run() {
        if (exclusive.isOnlyNames)
            println(gfs.names.joinToString("\n"))
        else if (exclusive.isAll)
            println("Тут будет полный вывод")
        else println(exclusive.graphs.joinToString())
    }
}

@Command(
    name = "remove",
    description = ["Delete graph."]
)
class Remove : GraphParameter(), Runnable {
    override fun run() {
        gfs.remove(graph.name)
        gfs.writeAllToFile()
    }
}

@Command(
    name = "connectivity",
    description = ["Compute vertex and edge k-connectivity."]
)
class Connectivity : GraphParameter(), Runnable {
    override fun run() {
        val vc = vertexConnectivity(graph)
        val ec = edgeConnectivity(graph)
        if (vc != 0 && ec != 0)
            println(
                "Graph $vc" +
                        if (ec != vc) ("-vertex-connected and $ec-edge-connected")
                        else "-connected"
            )
        else println("The graph is not connected.")
    }
}

@Command(
    name = "planarity",
    description = ["Check the graph for planarity."]
)
class Planarity : GraphParameter(), Runnable {
    override fun run() {
        if (planarity(graph))
            println("The graph is planar.")
        else
            println("The graph is not planar.")
    }
}

@Command(
    name = "isomorphism",
    description = ["Check two graphs for isomorphism."]
)
class Isomorphism : Runnable {
    @Parameters(description = [DESCRIPTION_GRAPH], converter = [GraphConverter::class])
    lateinit var firstGraph: Graph

    @Parameters(description = [DESCRIPTION_GRAPH], converter = [GraphConverter::class])
    lateinit var secondGraph: Graph

    override fun run() {
        if (isomorphism(firstGraph, secondGraph))
            println("Graphs are isomorphic.")
        else println("Graphs are not isomorphic.")
    }
}

@Command(
    name = "path",
    description = ["Find the shortest path between the given vertices."]
)
class Path : GraphParameter(), Runnable {
    @Parameters(description = [DESCRIPTION_VERTEX])
    var firstVertex: Int = -1

    @Parameters(description = [DESCRIPTION_VERTEX])
    var secondVertex: Int = -1

    override fun run() {
        printPath(graph, firstVertex, secondVertex)
    }
}

@Command(
    name = "maxflow",
    description = ["Find the maximum flow from the first vertex to the second."]
)
class MaxFlow : GraphParameter(), Runnable {
    @Parameters(description = [DESCRIPTION_VERTEX])
    var firstVertex: Int = -1

    @Parameters(description = [DESCRIPTION_VERTEX])
    var secondVertex: Int = -1

    override fun run() {
        printMaxFlow(graph, firstVertex, secondVertex)
    }
}

@Command(
    name = "euler",
    description = ["Find Euler cycle/chain in the graph."]
)
class ECycle : GraphParameter(), Runnable {
    @Option(names = ["-c", "--chain"], description = ["does not work"])
    var chain: Int = 0

    @Option(names = ["-m", "--multistart"], description = ["does not work"])
    var multistart: Boolean = false

    override fun run() {
        printECycle(graph)
    }
}

@Command(
    name = "hamilton",
    description = ["Find a Hamiltonian cycle/chain in the graph."]
)
class HCycle : GraphParameter(), Runnable {

    @Option(names = ["-t", "--tree"], description = ["Turns on tree output during search."])
    var tree: Boolean = false

    override fun run() {
        printHCycle(graph, tree)
    }
}

@Command(
    name = "redo",
    description = ["Change or add weights. Doesn't create new edges."]
)
class Redo : GraphParameter(), Runnable {
    @Option(
        names = ["-r", "--rounding-enable"],
        description = ["Turns on rounding to an integer if the expression evaluates to a fraction."]
    )
    var isRoundingEnable: Boolean = false

    @Parameters(description = [DESCRIPTION_WEIGHT])
    var expression: String? = null

    override fun run() {
        val result = Expression(expression)
        redo(graph) { u, v, w ->
            val res = result.with("u", u.toString())
                .and("v", v.toString())
                .and("w", w.toString())
                .eval()
            if (isRoundingEnable)
                res.toDouble().roundToInt()
            else res.intValueExact()
        }
        gfs.writeAllToFile()
    }
}


private fun printPath(g: Graph, u: Int, v: Int) {
    g.checkCorrectVer(u, v)
    val path = route(g, u, v)
    if (path.isEmpty())
        println("The vertex $v is not reachable from $u.")
    else
        println(path.joinToString(" "))
}

private fun printMaxFlow(g: Graph, u: Int, v: Int) {
    g.checkCorrectVer(u, v)
    val maxFlow = maxFlow(g, u, v)
    if (maxFlow.value == 0)
        print("The vertex $v is not reachable from $u.")
    else {
        println("The value of the maximum flow from $u to $v:  ${maxFlow.value}")
        println("List of augmentation paths:")
        for (path in maxFlow.flow) {
            print("(${path.value})  ")
            println(path.path.joinToString(" -> "))
        }
    }
}

private fun printHCycle(g: Graph, tree: Boolean) {
    if (vertexConnectivity(g) < 2) {
        println("There is no Hamiltonian cycle in this graph.")
        return
    }
    try {
        println(hamiltonCycle(g, 1, true, tree))
    } catch (ex: IllegalArgumentException) {
        println(ex.message)
    }
}

private fun printHChain(g: Graph, start: Int, tree: Boolean) {
    require(start >= 0 && start < g.numVer) { "Incorrect entry of the vertex for the start." }
    if (start == 0) {
        // Мультистарт
        var cc = 0
        for (i in 1..g.numVer) try {
            hamiltonCycle(g, i, cycle = false, tree = false)
            break
        } catch (e: IllegalArgumentException) {
            cc++
        }
        if (cc == g.numVer) println("The graph does not contain a Hamiltonian chain.")
        else println(
            hamiltonCycle(g, cc + 1, false, tree)
        )
    } else  // Старт с заданной вершины
        try {
            println(hamiltonCycle(g, start, false, tree))
        } catch (e: IllegalArgumentException) {
            println("The graph does not contain a Hamiltonian chain from a given vertex.")
        }
}

private fun printECycle(g: Graph) {
    if (vertexConnectivity(g) < 2) {
        println("There is no Euler cycle in this graph.")
        return
    }
    var numEvenVer = 0
    for (i in g.numVer - 1 downTo 0) if (g.deg(i) % 2 === 0) numEvenVer++
    if (g.numVer - numEvenVer === 0) println(
        "ЭЦ:  " + eulerCycle(
            g,
            1
        )
    ) else println("There is no Euler cycle in this graph.")
}

private fun printEChain(g: Graph, start: Int) {
    var numEvenVer = 0
    var oddVer = 0
    for (i in g.numVer - 1 downTo 0) if (g.deg(i) % 2 === 0) numEvenVer++ else oddVer = i
    if (g.numVer - numEvenVer === 2) {
        println("There is an Euler chain:")
        if (start == 0) println(
            eulerCycle(
                g,
                oddVer + 1
            )
        ) else if (g.deg(start) % 2 === 1) println(
            eulerCycle(
                g,
                start
            )
        ) else println("There is no Euler chain from the given vertex in this graph.")
    } else println("There is no Euler chain in this graph.")
}
