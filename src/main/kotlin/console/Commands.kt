package console

import algorithm.*
import graphs.Graph
import picocli.CommandLine.*
import storage.SetFileGraph
import java.io.File

@Command(
    name = "gp",
    version = ["GP 0.2.1"],
    subcommands = [GPNew::class, GPShow::class, GPRemove::class, GPConnectivity::class, GPPlanarity::class, GPIsomorphism::class,
        GPPath::class, GPMaxFlow::class, GPECycle::class, GPHCycle::class, GPRedo::class],
    description = ["A lightweight CLI-utility that can work with several graphs " +
            "(weighted, directed, multi-graphs are supported), find Hamiltonian and Euler cycles and chains, " +
            "build a search tree, find shortest paths and return the vertex and edge k-connectivity of a graph, " +
            "determine planarity, graph isomorphism and much more! (See https://github.com/oQaris/GraphProcessor)"],
    mixinStandardHelpOptions = true
)
class BaseCommand

private val gfs = SetFileGraph(File("GraphData"))

open class GraphParameter {
    class GraphConverter : ITypeConverter<Graph> {
        override fun convert(value: String) =
            gfs[value]
    }

    class GraphCandidates : ArrayList<String>(gfs.keys)

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
class GPNew : Runnable {
    override fun run() {
        gfs.add(GPInterface.newGraph())
        gfs.push()
    }
}

@Command(
    name = "show",
    description = ["Print one or more saved graphs."]
)
class GPShow : Runnable {

    private class Exclusive : ArrayGraphParameter() {

        @Option(names = ["-a", "--all"], description = ["Display all saved graphs."])
        var isAll = false

        @Option(names = ["-n", "--names"], description = ["Display only the names of all saved graphs."])
        var isOnlyNames = false
    }

    @ArgGroup(exclusive = true, multiplicity = "1")
    private lateinit var exclusive: Exclusive

    override fun run() {
        if (exclusive.isOnlyNames)
            println(gfs.keys.joinToString("\n"))
        else if (exclusive.isAll)
            println(gfs.values.joinToString("\n"))
        else println(exclusive.graphs.joinToString("\n"))
    }
}

@Command(
    name = "remove",
    description = ["Delete graph."]
)
class GPRemove : GraphParameter(), Runnable {
    override fun run() {
        gfs.remove(graph.name)
        gfs.push()
    }
}

@Command(
    name = "connectivity",
    description = ["Compute vertex and edge k-connectivity."]
)
class GPConnectivity : GraphParameter(), Runnable {

    private class Exclusive {
        @Option(
            names = ["-v", "--vertex-connectivity"],
            description = ["Calculates the vertex connectivity of a graph."]
        )
        var isVertexConn = false

        @Option(
            names = ["-e", "--edge-connectivity"],
            description = ["Calculates the edge connectivity of a graph."]
        )
        var isEdgeConn = false
    }

    @ArgGroup(exclusive = true)
    private lateinit var exclusive: Exclusive

    override fun run() {
        connectivity(graph, exclusive.isVertexConn, exclusive.isEdgeConn)
    }
}

@Command(
    name = "connectivity",
    description = ["Compute vertex and edge k-connectivity."]
)
class GPSubgraph : GraphParameter(), Runnable {

    private class Exclusive {
        @Option(
            names = ["-v", "--vertex-connectivity"],
            description = ["Defines the vertex connectivity of the subgraph."]
        )
        var vertexConn = -1

        @Option(
            names = ["-e", "--edge-connectivity"],
            description = ["Defines the edge connectivity of the subgraph."]
        )
        var edgeConn = -1
    }

    //todo вынести
    @Option(
        names = ["-l", "--logging"],
        // negatable = true,
        description = ["Turns on the output to the console of each stage of the algorithm."]
    )
    var isLog = false

    @ArgGroup(exclusive = true)
    private lateinit var exclusive: Exclusive

    override fun run() {
        val res =
            if (exclusive.edgeConn != -1)
                findSpanningKConnectedSubgraph(graph, exclusive.edgeConn, ::localEdgeConnectivity)
            else
                findSpanningKConnectedSubgraph(graph, exclusive.vertexConn, ::localVertexConnectivity)
        println("Time: " + res.timestamps)
        println(res.answer)
    }
}

@Command(
    name = "planarity",
    description = ["Check the graph for planarity."]
)
class GPPlanarity : GraphParameter(), Runnable {
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
class GPIsomorphism : Runnable {
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
class GPPath : GraphParameter(), Runnable {
    @Parameters(description = [DESCRIPTION_VERTEX])
    var firstVertex = -1

    @Parameters(description = [DESCRIPTION_VERTEX])
    var secondVertex = -1

    override fun run() {
        printPath(graph, firstVertex, secondVertex)
    }
}

@Command(
    name = "maxflow",
    description = ["Find the maximum flow from the first vertex to the second."]
)
class GPMaxFlow : GraphParameter(), Runnable {
    @Parameters(description = [DESCRIPTION_VERTEX])
    var firstVertex = -1

    @Parameters(description = [DESCRIPTION_VERTEX])
    var secondVertex = -1

    override fun run() {
        printMaxFlow(graph, firstVertex, secondVertex)
    }
}

@Command(
    name = "euler",
    description = ["Find Euler cycle/chain in the graph."]
)
class GPECycle : GraphParameter(), Runnable {
    @Option(names = ["-c", "--chain"], description = ["does not work"])
    var chain = 0

    @Option(names = ["-m", "--multistart"], description = ["does not work"])
    var multistart = false

    override fun run() {
        printECycle(graph)
    }
}

@Command(
    name = "hamilton",
    description = ["Find a Hamiltonian cycle/chain in the graph."]
)
class GPHCycle : GraphParameter(), Runnable {

    @Option(names = ["-t", "--tree"], description = ["Turns on tree output during search."])
    var tree = false

    override fun run() {
        printHCycle(graph, tree)
    }
}

@Command(
    name = "redo",
    description = ["Change or add weights. Doesn't create new edges."]
)
class GPRedo : GraphParameter(), Runnable {
    @Option(
        names = ["-r", "--rounding-enable"],
        description = ["Turns on rounding to an integer if the expression evaluates to a fraction."]
    )
    var isRoundingEnable = false

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
        gfs.push()
    }
}
