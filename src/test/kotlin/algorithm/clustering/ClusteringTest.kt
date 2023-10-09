package algorithm.clustering

import algorithm.findComponents
import algorithm.isClustering
import algorithm.thesis.Event
import console.algorithm.clustering.*
import graphs.edg
import graphs.impl.AdjacencyMatrixGraph
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import storage.SetFileGraph
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ClusteringTest {

    @Test
    fun comparatorTest() {
        val g1 = Subgraph(AdjacencyMatrixGraph("1"), 0, mutableListOf())
        val g2 = Subgraph(AdjacencyMatrixGraph("2"), 1, mutableListOf())
        val g3 = Subgraph(AdjacencyMatrixGraph("3"), 0, mutableListOf())

        val leaves = PriorityQueue(ascLastComparator)
        leaves.add(g1)
        leaves.add(g3)
        leaves.add(g2)

        val expectedOrder = listOf(g3, g1, g2)
        repeat(leaves.size) {
            assertEquals(expectedOrder[it], leaves.poll())
        }
    }

    private val testG = AdjacencyMatrixGraph("cmp", 7).apply {
        addEdg(1 edg 4)

        addEdg(0 edg 2)
        addEdg(2 edg 5)
        addEdg(2 edg 3)
        addEdg(3 edg 5)
    }

    @Test
    fun findComponentsTest() {
        assertArrayEquals(
            intArrayOf(1, 2, 1, 1, 2, 1, 3),
            findComponents(testG)
        )
    }

    @Test
    fun sliceTest() {
        assertArrayEquals(
            intArrayOf(1, 2, 3, 3, 4, 3, 5),
            findComponents(slice(testG, listOf(3 edg 5, 2 edg 5)))
        )
    }

    @Test
    fun correctCriterionOfClusteringTest() {
        assertTrue(correctCriterionOfClustering(findComponents(testG), testG))
        val noClusteringApriori = testG.clone().apply {
            addEdg(1 edg 6)
        }
        assertFalse(correctCriterionOfClustering(findComponents(noClusteringApriori), noClusteringApriori))
    }

    @Test
    fun isClusterTest() {
        assertFalse(isClustering(testG))
        val cluster = testG.clone().apply {
            addEdg(0 edg 5)
            addEdg(0 edg 3)
        }
        assertTrue(isClustering(cluster))
        assertTrue(isClustering(AdjacencyMatrixGraph("empty", 10)))
    }

    @Test
    fun preSortEdgesTest() {
        val node = Subgraph(testG, 0, testG.getEdges().map { it.first to it.second }.toMutableList())
        assertEquals(mutableListOf(0 to 2, 1 to 4, 2 to 3, 2 to 5, 3 to 5), node.rawDetails)
        resortDetails(node, 0 to 5)
        assertEquals(mutableListOf(0 to 2, 2 to 5, 3 to 5, 1 to 4, 2 to 3), node.rawDetails)
    }

    @Test
    fun clustering3FullTest() {
        val cntProvider = createDriver()
        val answer = clustering(SetFileGraph()["star"], 3, cntProvider.driver)!!

        assertEquals(4, answer.numEdg)
        assertEquals(2, answer.getVertices().filter { answer.deg(it) == 1 }.size)
        assertEquals(3, answer.getVertices().filter { answer.deg(it) == 2 }.size)

        assertAll(
            { assertEquals(149, cntProvider.countExe) },
            { assertEquals(1, cntProvider.countRec) }
        )
    }

    private val cl3 = AdjacencyMatrixGraph("cl3", 4).apply {
        addEdg(0 edg 1)
        addEdg(1 edg 2)
        addEdg(2 edg 3)
        addEdg(3 edg 0)
        addEdg(3 edg 1)
    }

    @Test
    fun clustering4Test() {
        val cntProvider = createDriver()
        val answer = clustering(cl3, 4, cntProvider.driver)!!

        assertEquals(6, answer.numEdg)
        assertAll(
            { assertEquals(6, cntProvider.countExe) },
            { assertEquals(1, cntProvider.countRec) }
        )
    }

    @Test
    fun clustering3Test() {
        val cntProvider = createDriver()
        val answer = clustering(cl3, 3, cntProvider.driver)!!

        assertEquals(3, answer.numEdg)
        assertEquals(1, answer.getVertices().filter { answer.deg(it) == 0 }.size)
        assertEquals(3, answer.getVertices().filter { answer.deg(it) == 2 }.size)

        assertAll(
            { assertEquals(12, cntProvider.countExe) },
            { assertEquals(1, cntProvider.countRec) }
        )
    }

    @Test
    fun clustering2Test() {
        val cntProvider = createDriver()
        val answer = clustering(cl3, 2, cntProvider.driver)!!

        assertEquals(2, answer.numEdg)
        assertTrue(answer.getVertices().all { answer.deg(it) != 0 })

        assertAll(
            { assertEquals(13, cntProvider.countExe) },
            { assertEquals(1, cntProvider.countRec) }
        )
    }

    @Test
    fun clustering1Test() {
        val cntProvider = createDriver()
        val answer = clustering(cl3, 1, cntProvider.driver)!!

        assertTrue(answer.getVertices().all { answer.deg(it) == 0 })

        assertAll(
            { assertEquals(5, cntProvider.countExe) },
            { assertEquals(1, cntProvider.countRec) }
        )
    }

    private fun createDriver() = object {
        var countExe: Int = 0
        var countRec: Int = 0
        val driver: (Event) -> Unit = {
            if (it == Event.EXE)
                countExe++
            else if (it == Event.REC)
                countRec++
        }
    }
}
