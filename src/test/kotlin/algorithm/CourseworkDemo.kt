package algorithm

import algorithm.thesis.findSpanningKConnectedSubgraph
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import storage.Generator
import storage.SetFileGraph
import utils.TimeMeter
import java.io.File

@Disabled
internal class CourseworkDemo {

    @Test
    fun `number of edges in the graph`() {
        val isNewDataGen = true
        val k = 2
        val n = 30
        val expCount = 24
        val sfg = SetFileGraph(File("TestData2.txt"), false)
        println("N_edges;Mean;Mode;Median;Max;Min;Mean_Rec;Mode_Rec;Median_Rec;Max_Rec;Min_Rec;")

        for (numEdg in /*Generator.minNumEdge(n, k)*/325..Generator.maxNumEdge(n)/*435*/) {
            val timeMeter = TimeMeter()
            val timeMeterRec = TimeMeter()

            repeat(expCount) { numEx ->
                val graph =
                    if (isNewDataGen)
                        Generator(
                            n,
                            numEdg,
                            conn = k,
                            except = sfg.values,
                            name = "${numEdg}_${numEx}"
                        ).build().apply { sfg.add(this) }
                    else sfg["${numEdg}_${numEx}"]

                val res = findSpanningKConnectedSubgraph(graph, k)
                timeMeter.addTimestamp(res.timestamps.get.last())
                if (res.timestamps.get.size >= 3)
                    timeMeterRec.addTimestamp(res.timestamps.get.let { it[it.size - 3] })
            }
            if (isNewDataGen) {
                sfg.push(true)
                sfg.clear()
            }
            println(
                "$numEdg;${timeMeter.getMean()};${timeMeter.getMode()};${timeMeter.getMedian()};${timeMeter.getMax()};${timeMeter.getMin()};" +
                        "${timeMeterRec.getMean()};${timeMeterRec.getMode()};${timeMeterRec.getMedian()};${timeMeterRec.getMax()};${timeMeterRec.getMin()};"
            )
        }
    }

    @Test
    fun `number of vertices in the graph`() {
        val pArr = listOf(1 / 3f, 1 / 2f, 2 / 3f, 1f)
        val isNewDataGen = true
        val k = 2

        val sfg = SetFileGraph(File("TestVertex.txt"))
        if (isNewDataGen) sfg.clear()

        pArr.forEach { p ->
            println("$p;Mean;Mode;Median;Max;Min;Mean_Rec;Mode_Rec;Median_Rec;Max_Rec;Min_Rec;")
            for (n in 1..40) {
                val timeMeter = TimeMeter()
                val timeMeterRec = TimeMeter()

                repeat(50) { numEx ->
                    val name = "${n}_${numEx}"
                    val graph =
                        if (isNewDataGen)
                            try {
                                Generator(
                                    n,
                                    p = p,
                                    conn = k,
                                    except = sfg.values,
                                    name = name
                                ).build().apply { sfg.add(this) }
                            } catch (e: Exception) {
                                return@repeat
                            }
                        else sfg[name]

                    val res = findSpanningKConnectedSubgraph(graph, k)
                    timeMeter.addTimestamp(res.timestamps.get.last())
                    if (res.timestamps.get.size >= 3)
                        timeMeterRec.addTimestamp(res.timestamps.get.let { it[it.size - 3] })
                }
                if (isNewDataGen) {
                    sfg.push(true)
                    sfg.clear()
                }
                println(
                    "$n;${timeMeter.getMean()};${timeMeter.getMode()};${timeMeter.getMedian()};${timeMeter.getMax()};${timeMeter.getMin()};" +
                            "${timeMeterRec.getMean()};${timeMeterRec.getMode()};${timeMeterRec.getMedian()};${timeMeterRec.getMax()};${timeMeterRec.getMin()};"
                )
            }
        }
    }

    @Test
    fun `k in full graph`() {
        val graphs = listOf(
            Generator(28, p = 1f).build(),
            Generator(29, p = 1f).build(),
            Generator(30, p = 1f).build(),
            Generator(31, p = 1f).build()
        )
        println("k;${graphs.joinToString(";") { it.numVer.toString() }};")
        for (k in 1 until 32) {
            val timestamps = mutableListOf<Long>()
            graphs.forEach {
                try {
                    val res = findSpanningKConnectedSubgraph(it, k)
                    timestamps.add(res.timestamps.get.last())
                } catch (e: Exception) {
                    timestamps.add(0L)
                }
            }
            println("$k;${timestamps.joinToString(";")};")
        }
    }

    @Test
    fun `Comparison of the speed of algorithms with edge and vertex connectivity`() {

    }

    @Test
    fun `Struct 3`() {
        val k = 3
        val graph7 = Generator(7, p = 1f).build()
        val graph8 = Generator(8, p = 1f).build()
        val graph9 = Generator(9, p = 1f).build()
        println("${findSpanningKConnectedSubgraph(graph7, k, localConnectivity = ::localVertexConnectivity).answer}")
        println()
        println("${findSpanningKConnectedSubgraph(graph7, k, localConnectivity = ::localEdgeConnectivity).answer}")
        println()
        println("---------------------------------------------------")
        println("${findSpanningKConnectedSubgraph(graph8, k, localConnectivity = ::localVertexConnectivity).answer}")
        println()
        println("${findSpanningKConnectedSubgraph(graph8, k, localConnectivity = ::localEdgeConnectivity).answer}")
        println()
        println("---------------------------------------------------")
        println("${findSpanningKConnectedSubgraph(graph9, k, localConnectivity = ::localVertexConnectivity).answer}")
        println()
        println("${findSpanningKConnectedSubgraph(graph9, k, localConnectivity = ::localEdgeConnectivity).answer}")
        println()
    }
}
