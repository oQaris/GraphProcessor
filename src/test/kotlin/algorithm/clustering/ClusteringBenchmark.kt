package algorithm.clustering

import algorithm.BenchmarkTestBase
import algorithm.distance
import console.algorithm.clustering.GamsModel
import console.algorithm.clustering.clustering
import console.algorithm.clustering.greedy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import storage.Generator
import storage.SetFileGraph
import java.io.File
import kotlin.random.Random
import kotlin.test.assertEquals

class ClusteringBenchmark : BenchmarkTestBase() {

    @BeforeEach
    fun info() {
        println("Heap max size: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "MB")
    }

    @Test
    fun `Number of vertices`() {
        val s = 3
        val expCount = 100
        val expName = "test_data/greedyV"

        val model = GamsModel()

        warmup()
        val pToVer = mapOf(
            1 / 3f to 100,
            //1 / 2f to 15,
            //1f to 99
        )
        pToVer.forEach { (p, maxVer) ->
            val bench = SfgBenchmark("$expName.txt", p.toString(), true)
            for (ver in s..maxVer) {
                val gen = Generator(
                    numVer = ver,
                    p = p
                )
                bench.printCustom(expCount, ver.toString(), gen) { graph ->
                    val exact = model.clustering(graph, s)
                    val approx = greedy(graph, s)
                    distance(graph, approx).toDouble() / distance(graph, exact).toDouble()
                }
            }
        }
        model.disconnect()
    }

    @Test
    fun `Number of edges`() {
        val maxSizeCluster = 4
        val numVer = 15
        val expCount = 4
        val maxEdge = Generator.maxNumEdge(numVer)
        println(maxEdge)

        warmup()
        val bench = SfgBenchmark("test_data/testE.txt", numVer.toString(), true)
        (41..maxEdge).forEach { numEdg ->
            val gen = Generator(
                numVer = numVer,
                numEdg = numEdg
            )
            bench.printMeasure(expCount, numEdg.toString(), gen) { graph, driver ->
                clustering(graph, maxSizeCluster, driver)
            }
        }
    }

    @Test
    fun `Greedy approx`() {
        val s = 3
        val numVer = 15
        val expCount = 100
        val maxEdge = Generator.maxNumEdge(numVer)
        println(maxEdge)

        warmup()
        val model = GamsModel()
        val bench = SfgBenchmark("test_data/greedyE.txt", numVer.toString(), true)
        (1..maxEdge).forEach { numEdg ->
            val gen = Generator(
                numVer = numVer,
                numEdg = numEdg
            )
            bench.printCustom(expCount, numEdg.toString(), gen) { graph ->
                val exact = model.clustering(graph, s)
                val approx = greedy(graph, s)
                distance(graph, approx).toDouble() / distance(graph, exact).toDouble()
            }
        }
    }

    @Test
    fun `Max size cluster in solution`() {
        val expCount = 1
        val gen = Generator(
            numVer = 14,
            p = 0.5f
        )
        warmup()

        val bench = SfgBenchmark("test_data/testS.txt", gen.toString(), true)
        for (s in 1..gen.numVer) {
            val curExpCount = /*when (s) {
                7 -> 0.75
                8 -> 0.5
                else -> 1.0
            } * */expCount

            bench.printMeasure(curExpCount.toInt(), s.toString(), gen) { graph, driver ->
                clustering(graph, s, driver)
            }
        }
    }

    @Test
    @Disabled("Перезапишет эталонные данные")
    fun `Random generate set distance`() {
        val sfg = SetFileGraph(File("rnd_test.txt"))
        sfg.clear()
        var numExp = 0
        for (ver in 4..11) {
            repeat(7) {
                val p = Random.nextFloat()
                val gen = Generator(numVer = ver, p = p)
                val g = gen.build()

                var name = numExp.toString() + "_"
                for (s in 3..4) {
                    val dist = distance(g, clustering(g, s)!!)
                    name += dist
                    name += "-"
                }
                name = name.dropLast(1)
                sfg.add(g, name)
                numExp++
                println("$numExp - $name")
            }
        }
        sfg.push()
    }

    @Test
    fun `Random test set distance`() {
        val sfg = SetFileGraph(File("rnd_test.txt"))
        sfg.forEach { name, g ->
            println(name)
            val dists = name.split("_")[1].split("-").map { it.toInt() }
            for ((i, s) in (3..4).withIndex()) {
                println(s)
                val dist = distance(g, clustering(g, s)!!)
                assertEquals(dists[i], dist, "$name was dist=$dist for s=$s")
            }
        }
    }

    @Test
    fun compareWithGams() {
        val maxSizeCluster = 3
        val expCount = 4

        warmup()
        val bench = SfgBenchmark("test_data/testCmp.txt", "0.5", true)
        for (ver in maxSizeCluster..20) {
            val gen = Generator(
                numVer = ver,
                p = 0.5f
            )
            bench.printMeasure(expCount, ver.toString(), gen) { graph, driver ->
                clustering(graph, maxSizeCluster, driver)
            }
        }
    }

    private fun warmup() {
        SetFileGraph().values.forEach { graph ->
            if (graph.numVer < 13) {
                graph.oriented = false
                try {
                    clustering(graph, 3) { print("") }
                } catch (ignored: Exception) {
                }
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
