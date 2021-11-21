package algorithm

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import storage.SetFileGraph
import storage.genConnectedGraph

internal class CourseworkTest {

    @Test
    fun someGraphsTest() {
        val sfg = SetFileGraph()

        assertEquals(
            6, findSpanningKConnectedSubgraph(
                sfg["bfly"], 2, localConnectivity = ::localEdgeConnectivity
            ).rec
        )
        assertThrows<IllegalArgumentException> {
            findSpanningKConnectedSubgraph(
                sfg["bfly"],
                2,
                localConnectivity = ::localVertexConnectivity
            )
        }

        assertEquals(4, findSpanningKConnectedSubgraph(sfg["Tgraf"], 1).rec)
        assertEquals(6, findSpanningKConnectedSubgraph(sfg["Tgraf"], 2).rec)
        assertEquals(24, findSpanningKConnectedSubgraph(sfg["4-cub"], 3).rec)
    }

    @Test
    fun vertexConnStruct3kTest() {
        for (n in 5..10) {
            val graph = genConnectedGraph(n, 0.8f, 3)
            val res = findSpanningKConnectedSubgraph(graph, 3, localConnectivity = ::localVertexConnectivity)
            assertEquals(3, vertexConnectivity(res.answer))
        }
    }

    @Test
    fun edgeConnStruct2kTest() {
        for (n in 5..10) {
            val graph = genConnectedGraph(n, 0.8f, 3)
            val res = findSpanningKConnectedSubgraph(graph, 3, localConnectivity = ::localEdgeConnectivity)
            assertEquals(3, edgeConnectivity(res.answer))
        }
    }
}
