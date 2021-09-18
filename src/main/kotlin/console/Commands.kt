package console

import algorithm.isomorphism
import algorithm.planarity
import graphs.Graph
import picocli.CommandLine.*
import storage.SetFileGraph
import java.io.File

@Command(
    name = "gp",
    version = ["GP 0.2.1"],
    subcommands = [New::class, Show::class, Remove::class, Connectivity::class, Planarity::class, Isomorphism::class,
        Path::class, MaxFlow::class, ECycle::class, HCycle::class, Redo::class],
    description = ["A lightweight CLI-utility that can work with several graphs " +
            "(weighted, directed, multi-graphs are supported), find Hamiltonian and Euler cycles and chains, " +
            "build a search tree, find shortest paths and return the vertex and edge k-connectivity of a graph, " +
            "determine planarity, graph isomorphism and much more! (See https://github.com/oQaris/GraphProcessor)"],
    mixinStandardHelpOptions = true
)
private class BaseCommand

private val gfs = SetFileGraph(File("GraphData"))

private open class GraphParameter {
    class GraphConverter : ITypeConverter<Graph> {
        override fun convert(value: String) =
            gfs[value] ?: throw TypeConversionException("No graph named \"$value\" was found.")
    }

    class GraphCandidates : ArrayList<String>(gfs.names)

    @Parameters(
        index = "0", description = [DESCRIPTION_GRAPH],
        converter = [GraphConverter::class],
        completionCandidates = GraphCandidates::class
    )
    lateinit var graph: Graph
}

private open class ArrayGraphParameter {
    @Parameters(
        arity = "1..*",
        description = [DESCRIPTION_GRAPH],
        converter = [GraphParameter.GraphConverter::class],
        completionCandidates = GraphParameter.GraphCandidates::class
    )
    lateinit var graphs: Array<Graph>
}

// ---------------------------- Specific commands ---------------------------- //

@Command(
    name = "new",
    description = ["Create and save a graph."]
)
private class New : Runnable {
    override fun run() {
        gfs.add(GPInterface.newGraph())
        gfs.writeAllToFile()
    }
}

@Command(
    name = "show",
    description = ["Print one or more saved graphs."]
)
private class Show : Runnable {

    private class Exclusive : ArrayGraphParameter() {

        @Option(names = ["-a", "--all"], description = ["Display all saved graphs."])
        var isAll: Boolean = false

        @Option(names = ["-n", "--names"], description = ["Display only the names of all saved graphs."])
        var isOnlyNames: Boolean = false
    }

    @ArgGroup(exclusive = true, multiplicity = "1")
    lateinit var exclusive: Exclusive

    override fun run() {
        if (exclusive.isOnlyNames)
            println(gfs.names.joinToString("\n"))
        else if (exclusive.isAll)
            println(gfs.graphs.joinToString("\n"))
        else println(exclusive.graphs.joinToString("\n"))
    }
}

@Command(
    name = "remove",
    description = ["Delete graph."]
)
private class Remove : GraphParameter(), Runnable {
    override fun run() {
        gfs.remove(graph.name)
        gfs.writeAllToFile()
    }
}

@Command(
    name = "connectivity",
    description = ["Compute vertex and edge k-connectivity."]
)
private class Connectivity : GraphParameter(), Runnable {

    private class Exclusive {
        @Option(
            names = ["-v", "--vertex-connectivity"],
            description = ["Calculates the vertex connectivity of a graph."]
        )
        var isVertexConn: Boolean = false

        @Option(
            names = ["-e", "--edge-connectivity"],
            description = ["Calculates the edge connectivity of a graph."]
        )
        var isEdgeConn: Boolean = false
    }

    @ArgGroup(exclusive = true)
    lateinit var exclusive: Exclusive

    override fun run() {
        connectivity(graph, exclusive.isVertexConn, exclusive.isEdgeConn)
    }
}

@Command(
    name = "planarity",
    description = ["Check the graph for planarity."]
)
private class Planarity : GraphParameter(), Runnable {
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
private class Isomorphism : Runnable {
    @Parameters(description = [DESCRIPTION_GRAPH], converter = [GraphParameter.GraphConverter::class])
    lateinit var firstGraph: Graph

    @Parameters(description = [DESCRIPTION_GRAPH], converter = [GraphParameter.GraphConverter::class])
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
private class Path : GraphParameter(), Runnable {
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
private class MaxFlow : GraphParameter(), Runnable {
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
private class ECycle : GraphParameter(), Runnable {
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
private class HCycle : GraphParameter(), Runnable {

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
private class Redo : GraphParameter(), Runnable {
    @Option(
        names = ["-r", "--rounding-enable"],
        description = ["Turns on rounding to an integer if the expression evaluates to a fraction."]
    )
    var isRoundingEnable: Boolean = false

    @Parameters(
        description = ["The new weight of all vertices. " +
                "It is either a constant or an expression (no spaces). " +
                "You can use special characters - " +
                "'U' (starting vertex number), " +
                "'V' (ending vertex number) and " +
                "'W' (current vertex weight)."]
    )
    lateinit var expression: String

    override fun run() {
        redo(graph, expression, isRoundingEnable)
        gfs.writeAllToFile()
    }
}
