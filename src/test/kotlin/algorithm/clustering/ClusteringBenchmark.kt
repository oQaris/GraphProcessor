package algorithm.clustering

import algorithm.BenchmarkTestBase
import algorithm.distance
import console.algorithm.clustering.clustering
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import storage.Generator
import storage.SetFileGraph

class ClusteringBenchmark : BenchmarkTestBase() {

    @BeforeEach
    fun info() {
        println("Heap max size: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "MB")
    }

    @Test
    fun `Number of vertices in the graph`() {
        val maxSizeCluster = 3
        val expCount = 1

        warmup()
        val pToVer = mapOf(
            1 / 5f to 17,
            1 / 2f to 15,
            4 / 5f to 13
        )
        pToVer.forEach { (p, maxVer) ->
            val bench = SfgBenchmark("test_result/testP-$p.txt", p.toString(), true)
            for (ver in 3..maxVer) {
                val gen = Generator(
                    numVer = ver,
                    p = p
                )
                bench.printMeasure(expCount, ver.toString(), gen) { graph, driver ->
                    clustering(graph, maxSizeCluster, driver)
                }
            }
        }
    }

    @Test
    fun `Max size cluster in solution`() {
        val numVer = 10
        val p = 0.5f
        val expCount = 1

        warmup()
        val bench = SfgBenchmark("test_result/testS.txt", p.toString(), true)
        for (s in 1..numVer - 3) {
            val gen = Generator(
                numVer = numVer,
                p = p
            )
            val curExpCount = when (s) {
                7 -> 0.75
                8 -> 0.5
                else -> 1.0
            } * expCount

            bench.printMeasure(curExpCount.toInt(), s.toString(), gen) { graph, driver ->
                clustering(graph, s, driver)
            }
        }
    }

    private fun warmup() {
        SetFileGraph().values.forEach { graph ->
            if (graph.numVer < 13) {
                graph.oriented = false
                clustering(graph, 3) { print("") }
            }
        }
    }

    @Test
    fun demo() {
        // 21 секунду выполняется
        val gen = Generator(
            numVer = 17,
            p = 0.25f,
        )
        val input = gen.build()
        println("Random graph:\n$input")
        val result = clustering(input, 3)
        println("Clustering graph:\n$result")
        println("Distance:\n${distance(input, result!!)}")
    }
}
