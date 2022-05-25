package utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class TimeMeterTest {

    private val time0 = emptyList<Long>()
    private val time2 = listOf<Long>(-5, -5, -5)
    private val time3 = listOf<Long>(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    private val time4 = listOf<Long>(90, 0, 1, -31, 1, -16, 7, 1, 68, 17, -31)

    @Test
    fun meanTest() {
        assertEquals(0, time0.mean())
        assertEquals(-5, time2.mean())
        assertEquals(45 / 10, time3.mean())
        assertEquals((90 + 0 + 1 - 31 + 1 - 16 + 7 + 1 + 68 + 17 - 31) / 11, time4.mean())
    }

    @Test
    fun medianTest() {
        assertEquals(0, time0.median())
        assertEquals(-5, time2.median())
        assertEquals((4 + 5) / 2, time3.median())
        assertEquals(1, time4.median())
    }

    @Test
    fun modesTest() {
        //TODO
    }

    @Test
    fun modeTest() {
        assertEquals(0, time0.mode())
        assertEquals(-5, time2.mode())
        assertEquals(0, time3.mode())
        assertEquals(1, time4.mode())
    }

    @Test
    fun modeDifTest() {
        //TODO
    }

    @Test
    fun maxTest() {
        assertEquals(0, time0.max())
        assertEquals(-5, time2.max())
        assertEquals(9, time3.max())
        assertEquals(90, time4.max())
    }

    @Test
    fun minTest() {
        assertEquals(0, time0.min())
        assertEquals(-5, time2.min())
        assertEquals(0, time3.min())
        assertEquals(-31, time4.min())
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
}
