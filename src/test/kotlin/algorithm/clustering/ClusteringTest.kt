package algorithm.clustering

import algorithm.findComponents
import algorithm.isCluster
import console.algorithm.clustering.Subgraph
import console.algorithm.clustering.ascLastComparator
import console.algorithm.clustering.clustering
import console.algorithm.clustering.slice
import graphs.edg
import graphs.impl.AdjacencyMatrixGraph
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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
    fun isClusterTest() {
        assertFalse(isCluster(testG))
        val cluster = testG.clone().apply {
            addEdg(0 edg 5)
            addEdg(0 edg 3)
        }
        assertTrue(isCluster(cluster))
        assertTrue(isCluster(AdjacencyMatrixGraph("empty", 10)))
    }

    @Test
    fun clusteringTest() {
        val cl3 = AdjacencyMatrixGraph("cl3", 4).apply {
            addEdg(0 edg 1)
            addEdg(1 edg 2)
            addEdg(2 edg 3)
            addEdg(3 edg 0)
            addEdg(3 edg 1)
        }
        val answer3 = clustering(cl3, 3)!!
        assertEquals(3, answer3.numEdg)
        assertEquals(1, answer3.getVertices().filter { answer3.deg(it) == 0 }.size)

        val answer2 = clustering(cl3, 2)!!
        assertEquals(2, answer2.numEdg)
        assertTrue(answer2.getVertices().all { answer2.deg(it) != 0 })

        val answer1 = clustering(cl3, 1)!!
        assertTrue(answer1.getVertices().all { answer1.deg(it) == 0 })
    }
}
