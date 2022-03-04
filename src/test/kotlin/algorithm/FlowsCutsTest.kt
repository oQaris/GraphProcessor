package algorithm

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import storage.SetFileGraph
import java.io.File

internal class FlowsCutsTest {

    @Test
    fun maxFlowTest() {
        val sfg = SetFileGraph(File("GraphData"))

        val tGraph = sfg["Tgraf"]
        assertEquals(4, maxFlow(tGraph, 4, 0).value)
        assertEquals(3, maxFlow(tGraph, 2, 3).value)

        assertEquals(2, maxFlow(sfg["bfly"], 0, 3).value)

        assertEquals(4, maxFlow(sfg["4-cub"], 0, 15).value)

        val flowGraph = sfg["flow"]
        assertEquals(7, maxFlow(flowGraph, 0, 5).value)
        assertEquals(0, maxFlow(flowGraph, 5, 0).value)

        val flow2Graph = sfg["flow2"]
        assertEquals(5, maxFlow(flow2Graph, 0, 5).value)
        assertEquals(0, maxFlow(flow2Graph, 5, 0).value)
    }
}
