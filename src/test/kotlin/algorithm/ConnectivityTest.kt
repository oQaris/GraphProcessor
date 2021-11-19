package algorithm

import graphs.Graph
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import storage.SetFileGraph
import storage.genConnectedGraph
import storage.genGraphWithGC
import java.io.Closeable
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.test.assertEquals

internal class ConnectivityTest {
    private val logger = KotlinLogging.logger {}

    fun <T : Closeable, R> T.useWith(block: T.() -> R): R = use { with(it, block) }

    val NUM_EX = 10

    @Test
    fun p2Test() {
        val n = 15
        var p = 0
        while (p <= NUM_EX) {
            for (i in 1..10) {
                val graph = genGraphWithGC(n, p.toFloat() / 10)
                val res = findSpanningKConnectedSubgraph(graph, 2)
                print(res.timestamps.get().last())
                print(";")
            }
            p += 1
            println()
        }
    }

    @Test
    fun r2Test() {
        val p = 0.4f
        for (n in (5..(NUM_EX + 5))) {
            logger.info { "n = $n" }
            var count = 1
            var (rp, r) = (0L to 0L)
            var all = 0L
            while (count > 0) {
                val graph = genGraphWithGC(n, p)
                val future = CompletableFuture.supplyAsync {
                    findSpanningKConnectedSubgraph(graph, 2)
                }
                try {
                    val res = future[1, TimeUnit.MINUTES]

                    val ts = res.timestamps.get().takeLast(3)
                    rp += ts[0]
                    r += ts[1]
                    all += ts[2]

                    assertEquals(n, res.answer.numEdg)
                    count--

                } catch (e: TimeoutException) {
                    future.cancel(true)
                    logger.info("Timed out")
                }
            }
            println("${rp / 5};${r / 5};${all / 5}")
        }
    }

    @Test
    fun rnTest() {
        val graph = genGraphWithGC(NUM_EX + 1, 1f)
        for (k in (1..NUM_EX)) {
            logger.info { "k = $k" }
            val res = findSpanningKConnectedSubgraph(graph, k)
            val ts = res.timestamps.get().takeLast(4).toMutableList()
            while (ts.size != 4)
                ts.add(ts[0])
            println("${ts[0]};${ts[1]};${ts[3]}")
        }
    }

    @Test
    fun recTest() {
        val n = 20
        val graphs = Array(6) { genGraphWithGC(n, 0.6f) }
        val res = graphs.map { findSpanningKConnectedSubgraph(it, 2) }
            .map { it.timestamps.get() }
        for (i in 0 until res.maxOf { it.size }) {
            println("$i;" + res.map { if (i < it.size) it[i] else 0 }.joinToString(";"))
        }
    }

    @Test
    fun veTest() {
        val n = 20
        var i = 1
        fun megaFun(graphs: Array<Graph>, k: Int) {
            fun List<Result>.uxxx() = this.map { it.timestamps.get().takeLast(3) }
                .reduceRight { list, acc ->
                    acc.zip(list).map { it.first + it.second }.map { it / list.size }
                }

            val resE = graphs
                .map { findSpanningKConnectedSubgraph(it, k, localConnectivity = ::localEdgeConnectivity) }.uxxx()
            val resV = graphs
                .map { findSpanningKConnectedSubgraph(it, k, localConnectivity = ::localVertexConnectivity) }.uxxx()

            println("$i;${resE[0]};${resE[2]};${resV[0]};${resV[2]}")
            i++
        }
        for (p in 1..10) {
            for (k in 1 until n) {
                val graphs = Array(10) { genConnectedGraph(n, p.toFloat() / 10, k) }
                megaFun(graphs, k)
            }
        }
    }

    @Test
    fun connected3Test() {
        val sfg = SetFileGraph()
        val res = findSpanningKConnectedSubgraph(sfg["big"]!!, 2)
        println(res.answer)
        println(res.answer.numEdg)
    }

    @Test
    fun ggg() {
        val list = File("C:/Users/oQaris/Desktop/ggg.txt").bufferedReader().use { it.readLines() }
        val shift = 23
        var i = 0
        while (i + shift < list.size) {
            println(list[i].split(";").zip(list[i + shift].split(";"))
                .joinToString(";") { ((it.first.toInt() + it.second.toInt()) / 2).toString() })
            i++
        }
    }

    @Test
    fun ggg2() {
        File("C:/Users/oQaris/Desktop/ggg.txt").bufferedReader().use { it.readLines() }
            .filter { it.contains(";") }
            .forEach { println(it) }
    }

    @Test
    fun correctTest() {
        val sfg = SetFileGraph()
        val res = findSpanningKConnectedSubgraph(sfg["mega"]!!, 2)
        println(res.answer)
        println(res.answer.numEdg)
    }
}
