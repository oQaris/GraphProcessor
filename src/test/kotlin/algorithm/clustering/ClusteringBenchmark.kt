package algorithm.clustering

import algorithm.BenchmarkTestBase
import algorithm.thesis.Event
import console.algorithm.clustering.clustering
import org.junit.jupiter.api.Test
import storage.Generator
import storage.SetFileGraph
import utils.*
import java.io.File

class ClusteringBenchmark : BenchmarkTestBase() {

    @Test
    fun `Number of vertices in the graph`() {
        println("Heap max size: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "MB")
        val pArr = listOf(1 / 3f, 1 / 2f, 2 / 3f, 1f)
        val isNewDataGen = true

        val sfg = SetFileGraph(File("test_result/ClusteringVertex.txt"))
        if (isNewDataGen) sfg.clear()

        pArr.forEach { p ->
            println("$p;Mean;Mode;Median;Max;Min;Mean_Rec;Mode_Rec;Median_Rec;Max_Rec;Min_Rec;Mean_tree;Mode_tree;Median_tree;Max_tree;Min_tree;")
            for (ver in 4..20) {
                val breakLevel = when (p) {
                    1 / 2f -> 17
                    2 / 3f -> 15
                    1f -> 12
                    else -> Int.MAX_VALUE
                }
                if (ver > breakLevel)
                    break
                val gen = Generator(
                    numVer = ver,
                    p = p,
                    except = sfg.values
                )
                val graphGetter = sfgGetter("$p-$ver", isNewDataGen, gen, sfg)
                val times = timedEvents(1, graphGetter) { graph, driver ->
                    clustering(graph, 3, driver)
                }
                val finish = times[Event.OFF]!!.flatten()
                val record = times[Event.ADD]!!.map { it.last() }
                val deeps = times[Event.EXE]!!.map { it.count().toLong() }
                println(
                    "$ver;${finish.mean()};${finish.mode()};${finish.median()};${finish.max()};${finish.min()};" +
                            "${record.mean()};${record.mode()};${record.median()};${record.max()};${record.min()};" +
                            "${deeps.mean()};${deeps.mode()};${deeps.median()};${deeps.max()};${deeps.min()};"
                )
            }
        }
    }
}
