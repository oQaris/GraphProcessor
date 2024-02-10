package algorithm.clustering

import console.algorithm.clustering.GamsModel
import graphs.edg
import graphs.impl.AdjacencyMatrixGraph
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import storage.SetFileGraph

class GamsModelTest {

    @Test
    fun graphToTableGmsTest() {
        val graph = SetFileGraph()["6_0"]
        val actual = GamsModel().graphToTableGms(graph)

        val expected = "      1  2  3  4  5  6  \n" +
                       "   1  0  0  0  0  0  0  \n" +
                       "   2  0  0  1  0  0  0  \n" +
                       "   3  0  1  0  0  0  0  \n" +
                       "   4  0  0  0  0  1  1  \n" +
                       "   5  0  0  0  1  0  0  \n" +
                       "   6  0  0  0  1  0  0  \n"
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun parseResultGraphTest() {
        val out = """
            G e n e r a l   A l g e b r a i c   M o d e l i n g   S y s t e m
            E x e c u t i o n


            ----     44 Result graph

            ----     44 VARIABLE x.L  result

                        1           3           4

            1                       1           1
            3           1                       1
            4           1           1


            ----     44 Objective value
                        MODEL clustering.ObjVal        =        4.000  


            EXECUTION TIME       =        0.059 SECONDS      4 MB  33.2.0 r4f23b21 LEX-LEG
        """.trimIndent()

        val model = GamsModel()
        val actual = model.parseResultGraph(out, 4)

        val expected = AdjacencyMatrixGraph("result", 4).apply {
            addEdg(0 edg 2)
            addEdg(0 edg 3)
            addEdg(2 edg 3)
        }

        Assertions.assertEquals(expected, actual)
    }
}
