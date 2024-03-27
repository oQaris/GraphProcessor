package algorithm.clustering

import algorithm.distance
import algorithm.findComponents
import algorithm.isClustering
import algorithm.thesis.Event
import console.algorithm.clustering.*
import graphs.Graph
import graphs.edg
import graphs.impl.AdjacencyMatrixGraph
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import storage.SetFileGraph
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ClusteringTest {

    private val gamsModel = GamsModel().apply { connect() }

    class Clusterizer(private val name: String, val start: (Graph, Int, (Event) -> Unit) -> Graph) {
        override fun toString() = name
    }

    private fun testModels(): List<Clusterizer> {
        return listOf(
            Clusterizer("BranchAndBound", ::clustering),
            Clusterizer("GamsModel", gamsModel::clustering)
        )
    }

    @Test
    fun comparatorTest() {
        val g1 = Subgraph(AdjacencyMatrixGraph("1", 1), 0, mutableListOf())
        val g2 = Subgraph(AdjacencyMatrixGraph("2", 1), 1, mutableListOf())
        val g3 = Subgraph(AdjacencyMatrixGraph("3", 1), 0, mutableListOf())

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
        val cmp2Edge = findComponents(noClusteringApriori)
        assertFalse(correctCriterionOfClustering(cmp2Edge, noClusteringApriori))
        noClusteringApriori.addEdg(4 edg 6)
        assertTrue(correctCriterionOfClustering(cmp2Edge, noClusteringApriori))
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
        val node = Subgraph(testG, 0, testG.getEdges().map { it.toPair() }.toMutableList())
        assertEquals(mutableListOf(0 to 2, 1 to 4, 2 to 3, 2 to 5, 3 to 5), node.copyRawDetails())
        node.resortDetails(0 to 5)
        assertEquals(mutableListOf(0 to 2, 2 to 5, 3 to 5, 1 to 4, 2 to 3), node.copyRawDetails())
    }

    @Test
    fun onFixingEdgePostprocessTest() {
        val testG = AdjacencyMatrixGraph("cmp34", 7).apply {
            addEdg(0 edg 1)
            addEdg(0 edg 2)
            addEdg(0 edg 3)
            addEdg(1 edg 2)
            addEdg(1 edg 3)
            addEdg(2 edg 3)

            addEdg(4 edg 5)
            addEdg(4 edg 6)
            addEdg(5 edg 6)

            addEdg(1 edg 5)
            addEdg(3 edg 4)
        }
        val node = Subgraph(
            testG,
            0,
            // фиксируются песочные часы из 1-2-3-4 и треугольник 5-6-7
            (testG.getPairVer() - setOf(0 to 1, 0 to 3, 1 to 2, 2 to 3, 4 to 5, 5 to 6, 4 to 6)).toMutableList()
        )
        onFixingEdgePostprocess(node, 4)
        assertEquals(0, node.score)
        assertEquals(11, node.graph.numEdg)

        node.fixDetails(setOf(0 to 2, 1 to 3))
        onFixingEdgePostprocess(node, 4)
        assertEquals(2, node.score)
        assertEquals(9, node.graph.numEdg)
        val exceptPairs = setOf(1 to 5, 3 to 4)
        assertTrue(node.copyRawDetails().intersect(exceptPairs).isEmpty())
        assertTrue(exceptPairs.all { !node.graph.isCom(it) })
    }

    @Test
    fun onFixingEdgePostprocessFakeTest() {
        val testG = AdjacencyMatrixGraph("cmp34", 7).apply {
            addEdg(0 edg 1)
            addEdg(1 edg 2)
            addEdg(2 edg 3)
            addEdg(3 edg 0)
        }
        val node = Subgraph(
            testG,
            0,
            (testG.getPairVer() - setOf(0 to 1, 1 to 2, 0 to 2)).toMutableList()
        )
        trimCluster(node, 3)
        assertEquals(0, node.score)
        fillByCriterion(node)
        assertEquals(0, node.score)
    }

    @ParameterizedTest
    @MethodSource("testModels")
    fun clustering3FullTest(clusterizer: Clusterizer) {
        val cntProvider = createDriver()
        val answer = clusterizer.start(SetFileGraph()["star"], 3, cntProvider.driver)

        assertEquals(4, answer.numEdg)
        assertEquals(2, answer.getVertices().filter { answer.deg(it) == 1 }.size)
        assertEquals(3, answer.getVertices().filter { answer.deg(it) == 2 }.size)

        assetCounterProvider(cntProvider)
    }

    @ParameterizedTest
    @MethodSource("testModels")
    fun clusteringTreeTest(clusterizer: Clusterizer) {
        val cntProvider = createDriver()
        val input = SetFileGraph()["tree6"]
        val answer3 = clusterizer.start(input, 3, cntProvider.driver)
        val answer4 = clusterizer.start(input, 4) {}

        assertEquals(3, distance(input, answer3))
        assertEquals(3, distance(input, answer4))

        assetCounterProvider(cntProvider)
    }

    @ParameterizedTest
    @MethodSource("testModels")
    fun clusteringBigTest(clusterizer: Clusterizer) {
        // Выполняется 4 секунды
        val cntProvider = createDriver()
        val input = SetFileGraph()["Undir_17-34"] // Undir_17x34_1..1
        val answer = clusterizer.start(input, 3, cntProvider.driver)
        assertEquals(22, distance(input, answer), answer.toString())
    }

    @ParameterizedTest
    @MethodSource("testModels")
    fun clusteringClippingTest(clusterizer: Clusterizer) {
        val cntProvider = createDriver()
        val input = SetFileGraph()["34_13-10"]

        val answer = clusterizer.start(input, 4, cntProvider.driver)
        assertEquals(10, distance(input, answer), answer.toString())
    }

    private val cl3 = AdjacencyMatrixGraph("cl3", 4).apply {
        addEdg(0 edg 1)
        addEdg(1 edg 2)
        addEdg(2 edg 3)
        addEdg(3 edg 0)
        addEdg(3 edg 1)
    }

    @ParameterizedTest
    @MethodSource("testModels")
    fun clustering4Test(clusterizer: Clusterizer) {
        val cntProvider = createDriver()
        val answer = clusterizer.start(cl3, 4, cntProvider.driver)

        assertEquals(6, answer.numEdg)
        assetCounterProvider(cntProvider)
    }

    @ParameterizedTest
    @MethodSource("testModels")
    fun clustering3Test(clusterizer: Clusterizer) {
        val cntProvider = createDriver()
        val answer = clusterizer.start(cl3, 3, cntProvider.driver)

        assertEquals(3, answer.numEdg)
        assertEquals(1, answer.getVertices().filter { answer.deg(it) == 0 }.size)
        assertEquals(3, answer.getVertices().filter { answer.deg(it) == 2 }.size)

        assetCounterProvider(cntProvider)
    }

    @ParameterizedTest
    @MethodSource("testModels")
    fun clustering2Test(clusterizer: Clusterizer) {
        val cntProvider = createDriver()
        val answer = clusterizer.start(cl3, 2, cntProvider.driver)

        assertEquals(2, answer.numEdg)
        assertTrue(answer.getVertices().all { answer.deg(it) != 0 })

        assetCounterProvider(cntProvider)
    }

    @ParameterizedTest
    @MethodSource("testModels")
    fun clustering1Test(clusterizer: Clusterizer) {
        val cntProvider = createDriver()
        val answer = clusterizer.start(cl3, 1, cntProvider.driver)

        assertTrue(answer.getVertices().all { answer.deg(it) == 0 })

        assetCounterProvider(cntProvider)
    }

    @ParameterizedTest
    @MethodSource("testModels")
    fun clusteringModelTest(clusterizer: Clusterizer) {
        val graph = SetFileGraph()["6_0"]
        val answer = clusterizer.start(graph, 3) {}

        assertEquals(1, distance(answer, graph))
    }

    @Test
    fun minScoreComparatorTest() {
        val comparator = minScoreComparator()
        val filler = cl3.getPairVer().toMutableList()
        val s1 = Subgraph(cl3, 0, filler, 99)
        val s2 = Subgraph(cl3, 1, filler, 99)
        val s3 = Subgraph(cl3, 1, filler, 98)
        val s4 = Subgraph(AdjacencyMatrixGraph("", 1), 1, mutableListOf(), 99)

        assertTrue(comparator.compare(s1, s2) < 0)
        assertTrue(comparator.compare(s1, s2) < 0)
        assertTrue(comparator.compare(s2, s3) < 0)
        assertTrue(comparator.compare(s2, s4) == 0)
    }

    private fun assetCounterProvider(cntProvider: CounterProvider, exe: Int = 999, rec: Int = 999) {
        assertAll(
            { assertTrue(cntProvider.countExe < exe) },
            { assertTrue(cntProvider.countRec <= rec) }
        )
    }

    private interface CounterProvider {
        var countExe: Int
        var countRec: Int
    }

    private fun createDriver() = object : CounterProvider {
        override var countExe: Int = 0
        override var countRec: Int = 0
        val driver: (Event) -> Unit = {
            if (it == Event.EXE)
                countExe++
            else if (it == Event.REC)
                countRec++
        }
    }
}
