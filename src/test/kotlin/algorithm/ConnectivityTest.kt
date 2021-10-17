package algorithm

import mu.KotlinLogging
import org.junit.jupiter.api.Test
import storage.SetFileGraph
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

    val NUM_EX = 25

    @Test
    fun p2Test() {
        val n = 15
        var p = 0
        while (p <= NUM_EX) {
            for (i in 1..10) {
                val graph = genGraphWithGC(n, p.toFloat() / 10)
                val res = findSpanningKConnectedSubgraph(graph, 2)
                print(res.second.second)
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

                    rp += res.second.first.first
                    r += res.second.first.second
                    all += res.second.second

                    assertEquals(n, res.first.numEdg)
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
    fun connected3Test() {
        val sfg = SetFileGraph()
        val res = findSpanningKConnectedSubgraph(sfg["mega"]!!, 3)
        println(res.first)
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
}
