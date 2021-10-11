package algorithm

import org.junit.jupiter.api.Test
import storage.genGraphWithGC

internal class ConnectivityTest {

    @Test
    fun findSpanningKConnectedSubgraphTest() {
        val n = 5
        while (true) {
            var f = 0.1f
            for (i in (1..9)) {
                val graph = genGraphWithGC(n, f)
                val res = findSpanningKConnectedSubgraph(graph, 2)
                println(res.first)
                f += 0.1f
            }
        }
    }
}
