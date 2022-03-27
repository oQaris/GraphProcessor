package algorithm.thesis

import algorithm.LocalConnectivity
import algorithm.connectivity
import algorithm.localEdgeConnectivity
import algorithm.localVertexConnectivity
import com.github.shiguruikai.combinatoricskt.permutationsWithRepetition
import graphs.Graph
import graphs.GraphException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import storage.Generator
import storage.SetFileGraph
import storage.requireG

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
            checkMinWeightWithConn(res.answer, k, conn, isConsiderZeroEdges = true)
        }
    }

    @Test
    @Disabled
    fun fullNkTest() {
        for (n in 5..10) {
            val graph = Generator(n, p = 1f, weights = -2..5).build()
            println(graph)
            val res = findSpanningKConnectedSubgraph(graph, 3, strategy = NegativeWeightedStrategy())
            println(res.answer)
            checkMinWeightWithConn(res.answer, 3, isConsiderZeroEdges = true)
        }
    }

    @Test
    fun textX_zeroYes() {
        val sfg = SetFileGraph()
        val graph = sfg["textX"]
        println(graph)
        val res = findSpanningKConnectedSubgraph(graph, 3, strategy = NegativeWeightedStrategy())
        // должен удалиться 0-4 и 1-3 или 0-1 и 3-4
        println(res.answer)
        checkMinWeightWithConn(res.answer, 3)
    }

    @Test
    @Disabled
    fun textY_zeroNo() {
        val sfg = SetFileGraph()
        val graph2 = sfg["textY"]
        println(graph2)
        val res2 = findSpanningKConnectedSubgraph(graph2, 3, strategy = NegativeWeightedStrategy())
        // должен удалиться 0-1 и 0-7
        println(res2.answer)
        checkMinWeightWithConn(res2.answer, 3, isConsiderZeroEdges = true)
    }

    @Test
    @Disabled
    fun textZ_kMore3() {
        val sfg = SetFileGraph()
        val graph = sfg["textZ"]
        println(graph)
        val res = findSpanningKConnectedSubgraph(graph, 3, strategy = NegativeWeightedStrategy())
        println(res.answer)
        checkMinWeightWithConn(res.answer, 3, isConsiderZeroEdges = true)
    }

    private fun checkMinWeightWithConn(
        g: Graph,
        k: Int,
        localConnectivity: LocalConnectivity = ::localEdgeConnectivity,
        isConsiderZeroEdges: Boolean = false
    ) {
        requireG(connectivity(g, localConnectivity) >= k) { "Граф не $k-связен!" }
        for (edg in g.getEdges()) {
            val weightEdg = edg.weight

            if (weightEdg >= 0) {
                if (!isConsiderZeroEdges && weightEdg == 0)
                    continue

                val gCpy = g.clone()
                gCpy.remEdg(edg)
                requireG(connectivity(gCpy, localConnectivity) < k)
                { "Граф $g не минимальный! Можно удалить ребро $edg" }
            }
        }
    }

    @Test
    @Disabled
    fun signsTest() {
        val signsAll = listOf<(Int, Int) -> Boolean>(
            { a, b -> a > b }, { a, b -> a < b }, { a, b -> a >= b }, { a, b -> a <= b })

        val testData = buildList {
            repeat(5) {
                val generator = Generator(it + 5, p = 1f, weights = 2..8)
                repeat(5) {
                    add(generator.build())
                }
            }
        }

        signs_loop@ for (signs in signsAll.permutationsWithRepetition(3)) {

            val startTime = System.nanoTime()
            for (graph in testData) {
                val res = findSpanningKConnectedSubgraph(graph, 3, strategy = WeightedStrategy()/*, signs = signs*/)
                try {
                    checkMinWeightWithConn(res.answer, 3, isConsiderZeroEdges = false)
                } catch (e: GraphException) {
                    continue@signs_loop
                }
            }
            print(System.nanoTime() - startTime)
            print(";")

            println(signs.joinToString("") { func ->
                if (func(1, 2)) {
                    if (func(1, 1)) "<=;"
                    else "<;"
                } else {
                    if (func(1, 1)) ">=;"
                    else ">;"
                }
            })
        }
    }
}
