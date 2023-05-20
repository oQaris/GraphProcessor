package utils

import java.util.*
import java.util.concurrent.TimeUnit

class Timestamps(private val timeUnit: TimeUnit = TimeUnit.SECONDS) {

    // System.currentTimeMillis() не подходит, поскольку нужна монотонно возрастающая последовательность
    private var start = System.nanoTime()

    // LinkedList, поскольку добавление в конец должно происходить всегда за одно время
    val times = LinkedList<Long>()

    fun make() {
        val elapsedTime = System.nanoTime() - start
        val convertedTime = timeUnit.convert(elapsedTime, TimeUnit.NANOSECONDS)
        times.add(convertedTime)
    }
}
