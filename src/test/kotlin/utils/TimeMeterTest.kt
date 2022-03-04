package utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class TimeMeterTest {

    private val time0 = newTimeMeter()
    private val time2 = newTimeMeter(-5, -5, -5)
    private val time3 = newTimeMeter(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    private val time4 = newTimeMeter(90, 0, 1, -31, 1, -16, 7, 1, 68, 17, -31)

    @Test
    fun getMeanTest() {
        assertEquals(0, time0.getMean())
        assertEquals(-5, time2.getMean())
        assertEquals(45 / 10, time3.getMean())
        assertEquals((90 + 0 + 1 - 31 + 1 - 16 + 7 + 1 + 68 + 17 - 31) / 11, time4.getMean())
    }

    @Test
    fun getMedianTest() {
        assertEquals(0, time0.getMedian())
        assertEquals(-5, time2.getMedian())
        assertEquals((4 + 5) / 2, time3.getMedian())
        assertEquals(1, time4.getMedian())
    }

    @Test
    fun getModeTest() {
        assertEquals(0, time0.getMode())
        assertEquals(-5, time2.getMode())
        assertEquals(0, time3.getMode())
        assertEquals(1, time4.getMode())
    }

    @Test
    fun getMaxTest() {
        assertEquals(0, time0.getMax())
        assertEquals(-5, time2.getMax())
        assertEquals(9, time3.getMax())
        assertEquals(90, time4.getMax())
    }

    @Test
    fun getMinTest() {
        assertEquals(0, time0.getMin())
        assertEquals(-5, time2.getMin())
        assertEquals(0, time3.getMin())
        assertEquals(-31, time4.getMin())
    }

    @Test
    fun printGraphTest() {
        time0.printGraph(3)
        println("----------------")
        time2.printGraph(10)
        println("----------------")
        time3.printGraph(66)
        println("----------------")
        time4.printGraph(10)
    }

    private fun newTimeMeter(vararg times: Long) = TimeMeter(times.toList())
}
