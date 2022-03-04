package utils

import java.util.*

class Timestamps {

    // System.currentTimeMillis() не подходит, поскольку нужна монотонно возрастающая последовательность
    private val start = System.nanoTime()

    // LinkedList, поскольку добавление в конец должно происходить всегда за одно время
    private var times = LinkedList<Long>()


    fun make() = times.add(System.nanoTime())

    fun get() = times.map { it - start }

    fun getLast() = times.last - start

    fun toTimeMeter() = TimeMeter(get())
}