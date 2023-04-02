package algorithm.thesis

import graphs.Edge
import graphs.Graph
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import storage.Generator
import utils.Timestamps
import kotlin.math.max
import kotlin.math.min

@Disabled
internal class SortEdgesDemo {

    @Test
    fun sortMethodTest() {
        println("n;natural;sum_deg;max_deg;min_deg;advanced;")
        for (n in 40 until 41) {

            fun alg(strategy: Strategy): Long {
                val timer = Timestamps()
                findSpanningKConnectedSubgraph(
                    Generator(n, p = 1f).build(), 3, strategy = strategy, driver = { timer.make() }
                )
                return timer.times.last
            }

            print("$n;")
            repeat(4) {
                val res: Long = when (it) {
                    0 -> alg(object : UnweightedStrategy() {
                        override fun sortEdges(edges: MutableList<Edge>, graph: Graph) = Unit
                    })
                    1 -> alg(object : UnweightedStrategy() {
                        override fun sortEdges(edges: MutableList<Edge>, graph: Graph) = edges.sortWith(
                            compareBy<Edge> { (u, v) -> graph.deg(u) + graph.deg(v) }
                                .reversed())
                    })
                    2 -> alg(object : UnweightedStrategy() {
                        override fun sortEdges(edges: MutableList<Edge>, graph: Graph) = edges.sortWith(
                            compareBy<Edge> { (u, v) -> max(graph.deg(u), graph.deg(v)) }
                                .reversed())
                    })
                    3 -> alg(object : UnweightedStrategy() {
                        override fun sortEdges(edges: MutableList<Edge>, graph: Graph) = edges.sortWith(
                            compareBy<Edge> { (u, v) -> min(graph.deg(u), graph.deg(v)) }
                                .reversed())
                    })
                    else -> 0
                }
                print("$res;")
            }
            println()
        }
    }

    @Test
    fun isResortTest() {
        fun alg(graph: Graph, isResort: Boolean): Long {
            val timer = Timestamps()
            findSpanningKConnectedSubgraph(
                graph, 3, strategy = object : UnweightedStrategy() {
                    override fun sortEdges(edges: MutableList<Edge>, graph: Graph) = edges.sortWith(
                        compareBy<Edge> { (u, v) -> graph.deg(u) + graph.deg(v) }
                            .reversed())

                    override val reSort: Boolean
                        get() = isResort
                }, driver = { timer.make() }
            )
            return timer.times.last
        }

        println("n;with_resort;no_resort;")
        for (n in 6..40) {
            print("$n;")
            var res1 = 0L
            var res2 = 0L
            val g = Generator(n, p = 0.6f, conn = 3).build()
            repeat(100) {
                repeat(2) {
                    when (it) {
                        0 -> res1 += alg(g, true)
                        1 -> res2 += alg(g, false)
                    }
                }
            }
            res1 /= 10
            res2 /= 10
            println("$res1;$res2")
        }
    }
}
