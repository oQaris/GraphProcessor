package algorithm

import org.junit.jupiter.api.Test
import storage.Generator
import utils.TimeMeter

internal class CourseworkDemo {

    @Test
    fun `Зависимость времени работы алгоритма от количества рёбер в графе с 20 вершинами (k=2)`() {
        val k = 2
        val expCount = 100
        println("№;ср.арифм;мода;медиана;макс.;мин.;ср.арифм.рек.;мода.рек.;медиана.рек.;макс.рек.;мин.рек.;")
        for (numEdg in 20..190) {
            val timeMeter = TimeMeter()
            val timeMeterRec = TimeMeter()
            repeat(expCount) {
                val graph = Generator(20, numEdg, conn = k, withGC = true).build()
                val res = findSpanningKConnectedSubgraph(graph, k)
                timeMeter.addTimestamp(res.timestamps.get().last())
                timeMeterRec.addTimestamp(res.timestamps.get().let { it[it.size - 2] })
            }
            println(
                "$numEdg;${timeMeter.getMean()};${timeMeter.getMode()};${timeMeter.getMedian()};${timeMeter.getMax()};${timeMeter.getMin()};" +
                        "${timeMeterRec.getMean()};${timeMeterRec.getMode()};${timeMeterRec.getMedian()};${timeMeterRec.getMax()};${timeMeterRec.getMin()};"
            )
        }
    }

    @Test
    fun `Зависимость времени работы алгоритма от количества вершин графа`() {

    }

    @Test
    fun `Зависимость времени работы алгоритма от значения k в полном графе на 30 вершинах`() {

    }

    @Test
    fun `Сравнение скорости алгоритмов с рёберной и вершинной связностью`() {

    }
}
