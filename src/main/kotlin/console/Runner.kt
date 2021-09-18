package console

import algorithm.*
import com.udojava.evalex.Expression
import graphs.Graph
import kotlin.math.roundToInt

fun printPath(g: Graph, u: Int, v: Int) {
    g.checkCorrectVer(u, v)
    val path = route(g, u, v)
    if (path.isEmpty())
        println("The vertex $v is not reachable from $u.")
    else
        println(path.joinToString(" "))
}

fun printMaxFlow(g: Graph, u: Int, v: Int) {
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

fun printHCycle(g: Graph, tree: Boolean) {
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

fun printHChain(g: Graph, start: Int, tree: Boolean) {
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

fun printECycle(g: Graph) {
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

fun printEChain(g: Graph, start: Int) {
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

fun redo(g: Graph, exprStr: String, isRound: Boolean) {
    val exp = Expression(exprStr)
    val count = redo(g) { u, v, w ->
        val res = exp.with("u", u.toString())
            .and("v", v.toString())
            .and("w", w.toString())
            .eval()
        if (isRound)
            res.toDouble().roundToInt()
        else res.intValueExact()
    }
    println(
        "Обновлены веса в $count ${
            if (g.oriented) "дугах" else "рёбрах"
        }"
    )
}

fun connectivity(g: Graph, isVertex: Boolean, isEdge: Boolean) {
    val vc = vertexConnectivity(g)
    val ec = edgeConnectivity(g)
    if (vc != 0 && ec != 0)
        println(
            "Graph $vc" +
                    if (ec != vc) ("-vertex-connected and $ec-edge-connected")
                    else "-connected"
        )
    else println("The graph is not connected.")
}
