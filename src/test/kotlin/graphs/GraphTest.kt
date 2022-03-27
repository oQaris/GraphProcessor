package graphs

import graphs.impl.EdgeListGraph
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class GraphTest {

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

    @Test
    fun getNumVerTest() {
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
