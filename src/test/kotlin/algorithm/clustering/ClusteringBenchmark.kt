package algorithm.clustering

import algorithm.BenchmarkTestBase
import algorithm.thesis.Event
import console.algorithm.clustering.clustering
import org.junit.jupiter.api.Test
import storage.Generator
import storage.SetFileGraph
import utils.*
import java.io.File
import java.util.concurrent.TimeUnit

class ClusteringBenchmark : BenchmarkTestBase() {
    private val MAX_SIZE_CLUSTER = 4

    @Test
    fun `Number of vertices in the graph`() {
        val expCount = 10
        println("Heap max size: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "MB")
        println("Count of Experiments: $expCount")
        val pArr = listOf(1 / 3f, 1 / 2f, 2 / 3f, 9 / 10f)
        val isNewDataGen = true

        val sfg = SetFileGraph(File("test_result/test.txt"))
        if (isNewDataGen) sfg.clear()

        println("Start Warmup")
        warmup()

        pArr.forEach { p ->
            println("$p;Mean;Mode;Median;Max;Min;Mean_Rec;Mode_Rec;Median_Rec;Max_Rec;Min_Rec;Mean_tree;Mode_tree;Median_tree;Max_tree;Min_tree;")
            for (ver in 3..13) {
                val breakLevel = when (p) {
                    1 / 2f -> 12
                    2 / 3f -> 11
                    9 / 10f -> 10
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
                val times = timedEvents(expCount, graphGetter, TimeUnit.MILLISECONDS) { graph, driver ->
                    clustering(graph, MAX_SIZE_CLUSTER, driver)
                }
                val finish = times[Event.OFF]!!.flatten()
                val record = times[Event.REC]!!.map { it.last() }
                val deeps = times[Event.EXE]!!.map { it.count().toLong() }
                println(
                    "$ver;${finish.mean()};${finish.mode()};${finish.median()};${finish.max()};${finish.min()};" +
                            "${record.mean()};${record.mode()};${record.median()};${record.max()};${record.min()};" +
                            "${deeps.mean()};${deeps.mode()};${deeps.median()};${deeps.max()};${deeps.min()};"
                )
            }
        }
    }

    private fun warmup() {
        SetFileGraph().values.forEach { graph ->
            if (graph.numVer < 10) {
                clustering(graph, MAX_SIZE_CLUSTER) { print("") }
            }
        }
    }
}
