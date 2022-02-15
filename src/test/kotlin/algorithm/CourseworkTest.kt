package algorithm

import algorithm.thesis.NegativeWeightedStrategy
import algorithm.thesis.UnweightedStrategy
import algorithm.thesis.WeightedStrategy
import algorithm.thesis.findSpanningKConnectedSubgraph
import graphs.AdjacencyMatrixGraph
import graphs.Graph
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import storage.Generator
import storage.SetFileGraph
import kotlin.test.assertTrue

internal class CourseworkTest {

    @Test
    fun someGraphsTest() {
        val sfg = SetFileGraph()

        assertEquals(
            6, findSpanningKConnectedSubgraph(
                sfg["bfly"], 2, ::localEdgeConnectivity
            ).rec
        )
        assertThrows<IllegalArgumentException> {
            findSpanningKConnectedSubgraph(
                sfg["bfly"],
                2,
                ::localVertexConnectivity
            )
        }

        assertEquals(4, findSpanningKConnectedSubgraph(sfg["Tgraf"], 1, strategy = UnweightedStrategy()).rec)
        assertEquals(6, findSpanningKConnectedSubgraph(sfg["Tgraf"], 1, strategy = WeightedStrategy()).rec)
        assertEquals(6, findSpanningKConnectedSubgraph(sfg["Tgraf"], 2, strategy = UnweightedStrategy()).rec)
        assertEquals(16, findSpanningKConnectedSubgraph(sfg["Tgraf"], 2, strategy = WeightedStrategy()).rec)
        assertEquals(24, findSpanningKConnectedSubgraph(sfg["4-cub"], 3, strategy = UnweightedStrategy()).rec)
        assertEquals(24, findSpanningKConnectedSubgraph(sfg["4-cub"], 3, strategy = WeightedStrategy()).rec)
    }

    @Test
    fun vertexConnStruct3kTest() = startTestWith(3, false)

    @Test
    fun edgeConnStruct3kTest() = startTestWith(3, true)

    private fun startTestWith(k: Int, isEdgeConn: Boolean, numExp: Int = 10) {
        val conn = if (isEdgeConn) ::localEdgeConnectivity else ::localVertexConnectivity
        val startN = k + 2
        for (n in startN until startN + numExp) {
            val graph = Generator(numVer = n, p = 0.8f, conn = k, localConn = conn).build()
            val res = findSpanningKConnectedSubgraph(graph, k, conn)
            checkMinWeightWithConn(res.answer, k, conn)
        }
    }

    @Test
    fun fullNkTest() {
        for (n in 5..10) {
            val graph = Generator(n, p = 1f, weights = -2..5).build()
            println(graph)
            val res = findSpanningKConnectedSubgraph(graph, 3, strategy = NegativeWeightedStrategy())
            println(res.answer)
            checkMinWeightWithConn(res.answer, 3)
        }
    }

    private fun checkMinWeightWithConn(
        g: Graph,
        k: Int,
        localConnectivity: LocalConnectivity = ::localEdgeConnectivity,
    ) {
        assertEquals(k, connectivity(g, localConnectivity)) { "Граф не $k-связен!" }
        g.getEdges().forEach {
            if (g.getWeightEdg(it)!! > 0) {
                val gCpy = AdjacencyMatrixGraph(g)
                gCpy.remEdg(it)
                assertTrue(
                    connectivity(gCpy, localConnectivity) < k,
                    "Граф $g не минимальный! Можно удалить ребро $it"
                )
            }
        }
    }

    @Test
    fun textX() {
        val sfg = SetFileGraph()
        val graph = sfg["textX"]
        println(graph)
        val res = findSpanningKConnectedSubgraph(graph, 3, strategy = NegativeWeightedStrategy())
        // должен удалиться 0-4 и 1-3 или 0-1 и 3-4
        println(res.answer)
        checkMinWeightWithConn(res.answer, 3)

        val graph2 = sfg["textX"]
        println(graph2)
        val res2 = findSpanningKConnectedSubgraph(graph2, 3, strategy = NegativeWeightedStrategy())
        // должен удалиться 0-5
        println(res2.answer)
        checkMinWeightWithConn(res2.answer, 3)
    }
}
