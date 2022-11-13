package algorithm.thesis

import algorithm.localEdgeConnectivity
import algorithm.localVertexConnectivity
import graphs.Graph
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import storage.Generator
import storage.GraphGenerationException
import storage.SetFileGraph
import utils.*
import java.io.File
import kotlin.random.Random

@Disabled
internal class CourseworkDemo {

    @Test
    fun `Number of edges in the graph`() {
        val k = 2
        val n = 30
        val sfg = SetFileGraph(File("TestData2.txt"), false)

        println("N_edges;Mean;Mode;Median;Max;Min;Mean_Rec;Mode_Rec;Median_Rec;Max_Rec;Min_Rec;")
        for (numEdg in Generator.minNumEdge(n, k)..Generator.maxNumEdge(n)) {
            val generator = Generator(
                n,
                numEdg,
                conn = k,
                except = sfg.values,
                name = "${numEdg}_"
            )
            savedTest(numEdg, 24, true, generator, sfg)
        }
    }

    @Test
    fun `Number of vertices in the graph`() {
        val pArr = listOf(1 / 3f, 1 / 2f, 2 / 3f, 1f)
        val isNewDataGen = true
        val k = 2

        val sfg = SetFileGraph(File("TestVertex.txt"))
        if (isNewDataGen) sfg.clear()

        pArr.forEach { p ->
            println("$p;Mean;Mode;Median;Max;Min;Mean_Rec;Mode_Rec;Median_Rec;Max_Rec;Min_Rec;")
            for (n in 1..40) {
                val gen = Generator(
                    n,
                    p = p,
                    conn = k,
                    except = sfg.values,
                    name = "name"
                )
                savedTest(n, 50, isNewDataGen, gen, sfg)
            }
        }
    }

    private fun savedTest(id: Int, expCount: Int, isNewDataGen: Boolean, generator: Generator, sfg: SetFileGraph) {
        val times = mutableListOf<Long>()
        val timesRec = mutableListOf<Long>()

        repeat(expCount) { numEx ->
            val name = "${id}_${numEx}"
            val graph =
                if (isNewDataGen)
                    try {
                        generator.build().apply { sfg.add(this) }
                        // set name
                    } catch (e: Exception) {
                        return@repeat
                    }
                else sfg[name]

            val timer = Timestamps()
            findSpanningKConnectedSubgraph(graph, 2, driver = { timer.make() })

            times.add(timer.get().last())
            if (timer.get().size >= 3)
                timesRec.add(timer.get().let { it[it.size - 3] })
        }
        if (isNewDataGen) {
            sfg.push(true)
            sfg.clear()
        }

        println(
            "$id;${times.mean()};${times.mode()};${times.median()};${times.max()};${times.min()};" +
                    "${timesRec.mean()};${timesRec.mode()};${timesRec.median()};${timesRec.max()};${timesRec.min()};"
        )
    }

    @Test
    fun `K in full graph`() {
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
                    val timer = Timestamps()
                    findSpanningKConnectedSubgraph(it, k, driver = { timer.make() })
                    timestamps.add(timer.getLast())
                } catch (e: Exception) {
                    timestamps.add(0L)
                }
            }
            println("$k;${timestamps.joinToString(";")};")
        }
    }

    @Test
    fun `k in full graph extended`() {
        val graph = Generator(30, p = 1f).build()
        println(graph)
        println("k;rec;prev;ppprev;")
        for (k in 1 until graph.numVer) {
            val timer = Timestamps()
            findSpanningKConnectedSubgraph(graph, k, driver = { timer.make() })
            println(
                "$k;${timer.getLast()};" +
                        "${timer.get().dropLast(1).last()};" +
                        "${timer.get().dropLast(2).last()};"
            )
        }
    }

    @Test
    fun `edge vs vertex connectivity`() {
        println("edge;vertex")
        while (true) {
            val timesListE = mutableListOf<Long>()
            val timesListV = mutableListOf<Long>()

            val n = (10..25).random()
            val k = (2..5).random()
            val p = Random.nextDouble(0.2, 1.0).toFloat()

            try {
                repeat(4) {
                    val g = Generator(n, p = p, conn = k).build()
                    val timerE = Timestamps()
                    val timerV = Timestamps()

                    findSpanningKConnectedSubgraph(
                        g, k,
                        localConnectivity = ::localEdgeConnectivity,
                        driver = { timerE.make() }
                    )
                    findSpanningKConnectedSubgraph(
                        g, k,
                        localConnectivity = ::localVertexConnectivity,
                        driver = { timerV.make() }
                    )
                    timesListE.add(timerE.getLast())
                    timesListV.add(timerV.getLast())
                }
            } catch (_: GraphGenerationException) {
                continue
            }
            println("${timesListE.median()};${timesListV.median()}")
        }
    }

    @Test
    fun `Weighted range in full graph`() {
        val n = 25
        val k = 3

        println("range;time")
        for (lim in (2..Generator.maxNumEdge(n))) {
            val timesList = mutableListOf<Long>()

            repeat(10) {
                val timer = Timestamps()
                val g = Generator(n, p = 1f, weights = (0..lim)).build()
                findSpanningKConnectedSubgraph(g, k, strategy = WeightedStrategy(), driver = { timer.make() })
                timesList.add(timer.getLast())
            }

            println("$lim;${timesList.median()}")
        }
    }

    @Test
    fun `Time to reach an approximate solution`() {
        val lastTimesCount = 4
        val conn = 2
        val p = 0.5f

        fun helper(n: Int) {

            val timesArr = Array(lastTimesCount) { mutableListOf<Long>() }

            repeat(100) {
                val graph = Generator(numVer = n, p = p, conn = conn).build()

                val timer = Timestamps()
                findSpanningKConnectedSubgraph(graph, conn, driver = { timer.make() })

                timer.get().takeLast(lastTimesCount)
                    .forEachIndexed { index, time -> timesArr[index].add(time) }
            }
            val times = timesArr.map { it.median() }
            println("$n;${times.joinToString(";")};")
        }

        for (n in 5..50)
            helper(n)
    }

    @Test
    fun `Time to reach an approximate solution with no match for grade`() {
        val lastTimesCount = 4
        val k = 2
        val p = 0.3f

        fun score(g: Graph) = Subgraph(g, k, WeightedStrategy(), g.getEdges()).score

        fun helper(n: Int) {
            val timesArr = Array(lastTimesCount) { mutableListOf<Long>() }

            var numEx = 0
            while (++numEx != 50) {
                val graph = Generator(numVer = n, p = p, conn = k, weights = 1..5).build()

                val timer = Timestamps()
                val result = findSpanningKConnectedSubgraph(graph, k,
                    strategy = WeightedStrategy(),
                    driver = { timer.make() })

                if (score(result.answer) == score(graph)) {
                    --numEx
                    println("Пропуск")
                    continue
                }
                timer.get().takeLast(lastTimesCount)
                    .forEachIndexed { index, time -> timesArr[index].add(time) }
            }
            val times = timesArr.map { it.median() }
            println("$n;${times.joinToString(";")};")
        }

        for (n in 9..50)
            helper(n)
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
