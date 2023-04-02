package algorithm.clustering

import algorithm.thesis.Event
import console.algorithm.clustering.clustering
import graphs.Graph
import org.junit.jupiter.api.Test
import storage.Generator
import storage.SetFileGraph
import utils.*
import java.io.File

typealias Driver = (Event) -> Unit

class ClusteringBenchmark {

    @Test
    fun `Number of vertices in the graph`() {
        val pArr = listOf(1 / 3f, 1 / 2f, 2 / 3f, 1f)
        val isNewDataGen = true

        val sfg = SetFileGraph(File("test_result/ClusteringVertex.txt"))
        if (isNewDataGen) sfg.clear()

        pArr.forEach { p ->
            println("$p;Mean;Mode;Median;Max;Min;Mean_Rec;Mode_Rec;Median_Rec;Max_Rec;Min_Rec;")
            for (ver in 15..20) {
                val gen = Generator(
                    numVer = ver,
                    p = p,
                    except = sfg.values
                )
                savedTest("$p-$ver", 100, isNewDataGen, gen, sfg) { graph, driver ->
                    clustering(graph, 3, driver)
                }
            }
        }
    }

    private fun savedTest(
        id: String,
        expCount: Int,
        isNewDataGen: Boolean,
        generator: Generator,
        sfg: SetFileGraph,
        timedFunc: (Graph, Driver) -> Unit
    ) {
        val times = mutableListOf<Long>()
        val timesRec = mutableListOf<Long>()
        val deepsTree = mutableListOf<Long>()

        repeat(expCount) { numEx ->
            val name = "${id}_${numEx}"
            val graph =
                if (isNewDataGen)
                    try {
                        generator.name = name
                        generator.build().apply { sfg.add(this) }
                    } catch (e: Exception) {
                        return@repeat
                    }
                else sfg[name]

            val stdTimer = Timestamps()
            val recTimer = Timestamps()
            var deepTree = 0
            val driver: Driver = {
                when (it) {
                    Event.OFF -> stdTimer.make()
                    Event.ADD -> recTimer.make()
                    Event.EXE -> deepTree++
                    else -> {}
                }
            }
            timedFunc(graph, driver)

            times.add(stdTimer.times.single())
            if (recTimer.times.isNotEmpty())
                timesRec.add(recTimer.times.last())
            deepsTree.add(deepTree.toLong())
        }
        if (isNewDataGen) {
            sfg.push(true)
            sfg.clear()
        }
        println(
            "$id;${times.mean()};${times.mode()};${times.median()};${times.max()};${times.min()};" +
                    "${timesRec.mean()};${timesRec.mode()};${timesRec.median()};${timesRec.max()};${timesRec.min()};" +
                    "${deepsTree.mean()};${deepsTree.mode()};${deepsTree.median()};${deepsTree.max()};${deepsTree.min()};"
        )
    }
}
