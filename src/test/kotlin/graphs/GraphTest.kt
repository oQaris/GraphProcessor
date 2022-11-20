package graphs

import graphs.impl.AdjacencyMatrixGraph
import graphs.impl.EdgeListGraph
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class GraphTest {

    private fun graphsProvider() = listOf(
        AdjacencyMatrixGraph("null", 4),
        EdgeListGraph("null", 4)
    )

    @Test
    fun orientedTest() {
        val graph: Graph = EdgeListGraph("null", 4).apply {
            oriented = true
            addEdg(0 edg 1 w 3)
            addEdg(2 edg 1 w 0)
            addEdg(3 edg 1 w 1)
            addEdg(3 edg 2 w 4)
            addEdg(1 edg 2 w 3)
        }
        assertEquals(true, graph.oriented)
        assertEquals(4, graph.numVer)
        assertEquals(5, graph.numEdg)
        assertEquals(11, graph.sumWeights)
        assertEquals("null", graph.name)
        assertEquals(5, graph.getEdges().size)
        assertEquals(6 * 2, graph.getPairVer().size)
    }

    @ParameterizedTest
    @MethodSource("graphsProvider")
    fun getNumVerTest(graph: Graph) {
    }

    @Test
    fun setNumVerTest() {
    }

    @Test
    fun getNumEdgTest() {
    }

    @Test
    fun setNumEdgTest() {
    }

    @Test
    fun getWeightEdgTest() {
    }

    @Test
    fun sumWeightsTest() {
    }

    @Test
    fun addVerTest() {
    }

    @Test
    fun addEdgTest() {
    }

    @Test
    fun degTest() {
    }

    @Test
    fun comTest() {
    }

    @Test
    fun getEdgesTest() {
    }

    @Test
    fun remVerTest() {
    }

    @Test
    fun remEdgTest() {
    }
}
