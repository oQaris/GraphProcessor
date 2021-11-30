package algorithm

import org.junit.jupiter.api.Test
import storage.Generator
import storage.SetFileGraph
import utils.TimeMeter
import java.io.File
import kotlin.test.assertEquals

internal class CourseworkDemo {

    @Test
    fun `number of edges in the graph`() {
        val isNewDataGen = true
        val k = 2
        val n = 30
        val expCount = 100
        val sfg = SetFileGraph(File("TestData2.txt"))
        println("N_edges;Mean;Mode;Median;Max;Min;Mean_Rec;Mode_Rec;Median_Rec;Max_Rec;Min_Rec;")

        for (numEdg in /*Generator.minNumEdge(n, k)*/61..Generator.maxNumEdge(n)) {
            val timeMeter = TimeMeter()
            val timeMeterRec = TimeMeter()

            repeat(expCount) { numEx ->
                val graph =
                    if (isNewDataGen)
                        Generator(n, numEdg, conn = k, withGC = true, name = "${numEdg}_${numEx}").build()
                            .apply { sfg.add(this) }
                    else sfg["$numEdg$numEx"]

                val res = findSpanningKConnectedSubgraph(graph, k)
                timeMeter.addTimestamp(res.timestamps.get.last())
                if (res.timestamps.get.size >= 3)
                    timeMeterRec.addTimestamp(res.timestamps.get.let { it[it.size - 3] })
            }
            if (isNewDataGen) sfg.push()
            println(
                "$numEdg;${timeMeter.getMean()};${timeMeter.getMode()};${timeMeter.getMedian()};${timeMeter.getMax()};${timeMeter.getMin()};" +
                        "${timeMeterRec.getMean()};${timeMeterRec.getMode()};${timeMeterRec.getMedian()};${timeMeterRec.getMax()};${timeMeterRec.getMin()};"
            )
        }
    }

    @Test
    fun `number of vertices in the graph`() {

    }

    @Test
    fun `k in full graph`() {
        val n = 35
        println("k;Time;")
        for (k in 1 until n) {
            val graph = Generator(n, p = 1f, conn = k, withGC = k != 1).build()
            val res = findSpanningKConnectedSubgraph(graph, k)
            println(
                "$k;${res.timestamps.get.last()};"
            )
            assertEquals(Generator.minNumEdge(n, k), res.answer.numEdg)
        }
    }

    @Test
    fun `Comparison of the speed of algorithms with edge and vertex connectivity`() {

    }
}
